const admin = require("firebase-admin");
const {onDocumentCreated, onDocumentUpdated} = require("firebase-functions/v2/firestore");

admin.initializeApp();


/**
 * Handles sending FCM notifications and saving them to Firestore history.
 * This helper function consolidates common logic for notification handling.
 *
 * @param {string} recipientId - The user ID of the notification recipient.
 * @param {string} type - The specific type of the notification (e.g., "new_travel_review", "last_minute_trip").
 * @param {string} title - The main title of the notification message.
 * @param {string} message - The body text of the notification.
 * @param {object} dataPayload - Additional data to include in the FCM payload and Firestore record.
 * @param {Array<string>} fcmTokens - An array of Firebase Cloud Messaging tokens for the recipient's devices.
 * @return {Promise<void>} A promise that resolves when the notification has been processed (sent and saved).
 */
async function deliverNotification(
    recipientId,
    type,
    title,
    message,
    dataPayload,
    fcmTokens,
) {
  if (!fcmTokens || fcmTokens.length === 0) {
    console.log(`No tokens for ${recipientId}. Skipping push.`);
    return;
  }

  const payload = {
    // notification: {
    //   title: title,
    //   body: message,
    // },
    data: {
      recipientId: recipientId,
      notificationType: type,
      title: title,
      body: message,
      ...dataPayload,
    },
    tokens: fcmTokens,
    android: {
      priority: "high",
    },
  };

  console.log(`Sending FCM to ${recipientId}.`);

  try {
    const response = await admin.messaging().sendEachForMulticast(payload);
    console.log(`${response.successCount} messages sent to ${recipientId}.`);

    // Clean up invalid tokens
    if (response.failureCount > 0) {
      const tokensToRemove = [];
      response.responses.forEach((resp, idx) => {
        if (!resp.success) {
          const token = fcmTokens[idx];
          console.error(`Failed token ${token}:`, resp.error);
          if (resp.error.code === "messaging/invalid-registration-token" ||
                        resp.error.code === "messaging/registration-token-not-registered") {
            tokensToRemove.push(token);
          }
        }
      });

      if (tokensToRemove.length > 0) {
        console.log(`Removing ${tokensToRemove.length} invalid tokens for user ${recipientId}.`);
        await admin.firestore().collection("users").doc(recipientId).update({
          fcmTokens: admin.firestore.FieldValue.arrayRemove(...tokensToRemove),
        });
      }
    }

    // Save notification for user's history
    const notificationForHistory = {
      recipientId: recipientId,
      type: type,
      title: title,
      message: message,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      read: false,
      ...dataPayload,
    };

    const userNotificationsCollectionRef = admin.firestore()
        .collection("users").doc(recipientId)
        .collection("user_notifications");

    const newNotificationDocRef = userNotificationsCollectionRef.doc();

    await newNotificationDocRef.set({
      ...notificationForHistory,
      notificationId: newNotificationDocRef.id,
    });
    console.log(`Notification of type "${type}" saved for ${recipientId}.`);
  } catch (error) {
    console.error(`Error sending/saving notification to history for user ${recipientId}:`, error);
  }
}


/**
 * Notifies travel organizer about new reviews.
 */
exports.notifyOnNewTravelReview = onDocumentCreated("travel_reviews/{reviewId}", async (event) => {
  if (!event.data) {
    console.log("No review data.");
    return null;
  }

  const reviewData = event.data.data();
  const travelId = reviewData.proposalId;
  const reviewId = event.params.reviewId;

  const travelDoc = await admin.firestore().collection("travelProposals").doc(travelId).get();
  if (!travelDoc.exists) {
    console.log("Travel not found:", travelId);
    return null;
  }
  const travelData = travelDoc.data();
  const organizerId = travelData.organizerId;

  if (!organizerId) {
    console.log("Organizer ID missing for travel:", travelId);
    return null;
  }

  const userDoc = await admin.firestore().collection("users").doc(organizerId).get();
  if (!userDoc.exists || !userDoc.data().fcmTokens || userDoc.data().fcmTokens.length === 0) {
    console.log("No FCM tokens for organizer:", organizerId);
    return null;
  }
  const tokens = userDoc.data().fcmTokens;

  const notificationTitle = "New travel review";
  const notificationMessage = `${reviewData.reviewerFirstName || "Someone"} has reviewed ${travelData.name || "your travel"}.`;

  const dataPayload = {
    proposalId: travelId,
    reviewId: reviewId,
    relatedUserId: reviewData.reviewerId,
  };

  await deliverNotification(
      organizerId,
      "new_travel_review",
      notificationTitle,
      notificationMessage,
      dataPayload,
      tokens,
  );

  return null;
});

/**
 * Notifies user about new reviews.
 */
exports.notifyOnNewUserReview = onDocumentCreated("user_reviews/{reviewId}", async (event) => {
  if (!event.data) {
    console.log("No review data.");
    return null;
  }

  const reviewData = event.data.data();
  const reviewedUserId = reviewData.reviewedUserId;
  const reviewId = event.params.reviewId;

  const userDoc = await admin.firestore().collection("users").doc(reviewedUserId).get();
  if (!userDoc.exists) {
    console.log("User not found:", reviewedUserId);
    return null;
  }

  const userData = userDoc.data();
  const userId = userData.userId;

  if (!userId) {
    console.log("User ID missing for user:", userId);
    return null;
  }

  if (!userData.fcmTokens || userData.fcmTokens.length === 0) {
    console.log("No FCM tokens for user:", reviewedUserId);
    return null;
  }
  const tokens = userData.fcmTokens;

  const notificationTitle = "New profile review";
  const notificationMessage = `${reviewData.reviewerFirstName || "Someone"} has reviewed your profile"}.`;

  const dataPayload = {
    userId: reviewedUserId,
    reviewId: reviewId,
    relatedUserId: reviewData.reviewerId,
  };

  await deliverNotification(
      reviewedUserId,
      "new_user_review",
      notificationTitle,
      notificationMessage,
      dataPayload,
      tokens,
  );

  return null;
});


/**
 * Checks if a travel is "last-minute" (within 2 days).
 * A travel is last-minute if its start date is within 2 days from now
 * and the start date is still in the future.
 *
 * @param {admin.firestore.Timestamp} startDateTimestamp - The Firestore Timestamp of the travel's start date.
 * @return {boolean} True if the travel is last-minute, false otherwise.
 */
function isLastMinute(startDateTimestamp) {
  if (!startDateTimestamp) return false;
  const now = admin.firestore.Timestamp.now().seconds;
  const tripStart = startDateTimestamp.seconds;
  const twoDays = 2 * 24 * 60 * 60;

  return (tripStart - now) <= twoDays && (tripStart - now) > 0;
}


/**
 * Notifies all users about new last-minute travel proposals.
 */
exports.notifyOnNewLastMinuteTravel = onDocumentCreated("travelProposals/{proposalId}", async (event) => {
  if (!event.data) {
    console.log("No data for new travel.");
    return null;
  }

  const travelData = event.data.data();
  const proposalId = event.params.proposalId;

  if (!isLastMinute(travelData.startDate)) {
    console.log(`Travel "${travelData.name}" (${proposalId}) isn't last-minute.`);
    return null;
  }

  console.log(`New last-minute travel found: "${travelData.name}" (${proposalId}).`);

  const usersSnapshot = await admin.firestore().collection("users").get();

  for (const userDoc of usersSnapshot.docs) {
    const userId = userDoc.id;
    const fcmTokens = userDoc.data().fcmTokens;

    const notificationTitle = "Last Minute! Travel Offer!";
    const notificationMessage = `"${travelData.name || "A trip"}" is about to start! Check it out!`;

    const dataPayload = {
      proposalId: proposalId,
    };

    await deliverNotification(
        userId,
        "last_minute_trip",
        notificationTitle,
        notificationMessage,
        dataPayload,
        fcmTokens,
    );
  }

  return null;
});


/**
 * Notifies users when an existing travel proposal becomes "last-minute" after an update.
 */
exports.notifyOnUpdatedLastMinuteTravel = onDocumentUpdated("travelProposals/{proposalId}", async (event) => {
  if (!event.data) {
    console.log("No data for updated travel.");
    return null;
  }

  const oldData = event.data.before.data();
  const newData = event.data.after.data();
  const proposalId = event.params.proposalId;

  const wasLastMinute = isLastMinute(oldData.startDate);
  const isLastMinuteNow = isLastMinute(newData.startDate);

  if (!wasLastMinute && isLastMinuteNow) {
    console.log(`Travel "${newData.name}" (${proposalId}) just became last-minute!`);

    const usersSnapshot = await admin.firestore().collection("users").get();

    for (const userDoc of usersSnapshot.docs) {
      const userId = userDoc.id;
      const fcmTokens = userDoc.data().fcmTokens;

      const notificationTitle = "Last Minute!";
      const notificationMessage = `${newData.name || "A trip"} is now a last-minute opportunity!`;


      const dataPayload = {
        proposalId: proposalId,
      };

      await deliverNotification(
          userId,
          "last_minute_trip",
          notificationTitle,
          notificationMessage,
          dataPayload,
          fcmTokens,
      );
    }
  } else {
    console.log(`Travel "${newData.name}" (${proposalId}) update didn't trigger last-minute alert.`);
  }

  return null;
});

/**
 * Notifies user when their travel application status changes (accepted/rejected).
 */
exports.notifyOnApplicationStatusChange = onDocumentUpdated("travel_applications/{applicationId}", async (event) => {
  if (!event.data) {
    console.log("No data for application status change.");
    return null;
  }

  const oldApplicationData = event.data.before.data();
  const newApplicationData = event.data.after.data();
  const applicationId = event.params.applicationId;

  // Check if the status actually changed
  if (oldApplicationData.status === newApplicationData.status) {
    console.log(`Application ${applicationId} status did not change.`);
    return null;
  }

  const applicantId = newApplicationData.userId;
  const proposalId = newApplicationData.proposalId;
  const newStatus = newApplicationData.status;

  if (newStatus !== "Accepted" && newStatus !== "Rejected") {
    console.log(`Application ${applicationId} status changed to neither accepted nor rejected: ${newStatus}.`);
    return null;
  }

  const travelDoc = await admin.firestore().collection("travelProposals").doc(proposalId).get();
  if (!travelDoc.exists) {
    console.log("Related travel proposal not found for application:", proposalId);
    return null;
  }
  const travelName = travelDoc.data().name || "Your trip";

  const userDoc = await admin.firestore().collection("users").doc(applicantId).get();
  if (!userDoc.exists || !userDoc.data().fcmTokens || userDoc.data().fcmTokens.length === 0) {
    console.log("No FCM tokens for applicant:", applicantId);
    return null;
  }
  const tokens = userDoc.data().fcmTokens;

  let notificationTitle;
  let notificationMessage;
  let notificationType;

  if (newStatus === "Accepted") {
    notificationTitle = "Application accepted";
    notificationMessage = `Your application to "${travelName}" has been accepted!`;
    notificationType = "application_accepted";
  } else { // status === "Rejected"
    notificationTitle = "Application rejected";
    notificationMessage = `Your application to "${travelName}" has been rejected.`;
    notificationType = "application_rejected";
  }

  const dataPayload = {
    proposalId: proposalId,
    applicationId: applicationId,
    status: newStatus,
  };

  await deliverNotification(
      applicantId,
      notificationType,
      notificationTitle,
      notificationMessage,
      dataPayload,
      tokens,
  );

  return null;
});


/**
 * Notifies the organizer of a travel proposal when a new application is submitted for their trip.
 */
exports.notifyOnNewApplication = onDocumentCreated("travel_applications/{applicationId}", async (event) => {
  if (!event.data) {
    console.log("No data for new application.");
    return null;
  }

  const newApplicationData = event.data.data();
  const applicationId = event.params.applicationId;
  const proposalId = newApplicationData.proposalId;
  const applicantId = newApplicationData.userId;


  // Get the travel proposal details to find the organizer
  const travelDoc = await admin.firestore().collection("travelProposals").doc(proposalId).get();
  if (!travelDoc.exists) {
    console.log("Travel proposal not found for new application:", proposalId);
    return null;
  }
  const travelData = travelDoc.data();
  const organizerId = travelData.organizerId;
  const travelName = travelData.name || "Your trip";

  if (!organizerId) {
    console.log("Organizer ID missing for travel proposal:", proposalId);
    return null;
  }

  // Get the applicant's name for the notification message
  const applicantDoc = await admin.firestore().collection("users").doc(applicantId).get();
  let applicantName = "Qualcuno";
  if (applicantDoc.exists && applicantDoc.data().firstName) {
    applicantName = applicantDoc.data().firstName + (applicantDoc.data().lastName ? " " + applicantDoc.data().lastName : "");
  }


  // Get FCM token(s) for the organizer
  const organizerUserDoc = await admin.firestore().collection("users").doc(organizerId).get();
  if (!organizerUserDoc.exists || !organizerUserDoc.data().fcmTokens || organizerUserDoc.data().fcmTokens.length === 0) {
    console.log("No FCM tokens for organizer:", organizerId);
    return null;
  }
  const tokens = organizerUserDoc.data().fcmTokens;

  const notificationTitle = "New travel application";
  const notificationMessage = `${applicantName} has applied to "${travelName}".`;

  const dataPayload = {
    proposalId: proposalId,
    applicationId: applicationId,
    applicantId: applicantId,
  };

  await deliverNotification(
      organizerId,
      "new_travel_application",
      notificationTitle,
      notificationMessage,
      dataPayload,
      tokens,
  );

  return null;
});

/**
 * Notifies users when their interests or preferred destinations are updated,
 * suggesting relevant published travel proposals that match their preferences.
 * Triggered on updates to the "users" collection.
 */
exports.notifyOnUserPreferencesChange = onDocumentUpdated("users/{userId}", async (event) => {
  if (!event.data) {
    console.log("No data for updated user preferences.");
    return null;
  }

  const oldData = event.data.before.data();
  const newData = event.data.after.data();
  const userId = event.params.userId;

  // Se interessi e destinazioni non sono cambiati, non fare nulla
  const interestsChanged = JSON.stringify(oldData.interests || []) !== JSON.stringify(newData.interests || []);
  const destinationsChanged = JSON.stringify(oldData.desiredDestinations || []) !== JSON.stringify(newData.desiredDestinations || []);

  if (!interestsChanged && !destinationsChanged) {
    console.log(`User ${userId}: interests and destinations unchanged.`);
    return null;
  }

  const userFcmTokens = newData.fcmTokens;
  if (!userFcmTokens || userFcmTokens.length === 0) {
    console.log(`User ${userId} has no FCM tokens.`);
    return null;
  }

  const interests = newData.interests || [];
  const destinations = newData.desiredDestinations || [];

  const matchingProposalsSnapshot = await admin.firestore().collection("travelProposals")
      .where("status", "==", "Published").get();

  const matches = [];

  matchingProposalsSnapshot.forEach((doc) => {
    const proposal = doc.data();

    const typologyMatch = interests.includes(proposal.typology);
    const itineraryPlaces = (proposal.itinerary || []).map((stop) => stop.place);
    const destinationMatch = destinations.some((dest) => itineraryPlaces.includes(dest));

    if (typologyMatch || destinationMatch) {
      matches.push({
        proposalId: doc.id,
        name: proposal.name || "A travel opportunity",
      });
    }
  });

  if (matches.length === 0) {
    console.log(`No matching proposals for user ${userId}.`);
    return null;
  }

  const proposal = matches[Math.floor(Math.random() * matches.length)];

  const notificationTitle = "A trip matches your interests!";
  const notificationMessage = `We found a travel to "${proposal.name}" that might interest you.`;

  const dataPayload = {
    proposalId: proposal.proposalId,
  };

  await deliverNotification(
      userId,
      "recommended_travel",
      notificationTitle,
      notificationMessage,
      dataPayload,
      userFcmTokens,
  );

  return null;
});
