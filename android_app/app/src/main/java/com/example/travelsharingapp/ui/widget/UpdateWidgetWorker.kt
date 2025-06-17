package com.example.travelsharingapp.ui.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.travelsharingapp.data.model.ApplicationStatus
import com.example.travelsharingapp.data.model.TravelApplication
import com.example.travelsharingapp.data.model.TravelProposal
import com.example.travelsharingapp.data.repository.AuthPreferenceKeys
import com.example.travelsharingapp.data.repository.TravelProposalRepository
import com.example.travelsharingapp.data.repository.WidgetPreferenceKeys
import com.example.travelsharingapp.data.repository.dataStoreInstance
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class UserIdProvider(private val context: Context) {
    suspend fun getUserId(): String? {
        val preferences = context.dataStoreInstance.data.first()
        return preferences[AuthPreferenceKeys.LOGGED_IN_USER_ID]
    }
}

class UpdateWidgetWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val travelProposalRepository = TravelProposalRepository(context)

    companion object {

        private const val UNIQUE_WORK_NAME = "ImmediateUpdateTravelsWidgetWork"

        fun enqueueImmediateWidgetUpdate(context: Context) {
            val updateRequest = OneTimeWorkRequestBuilder<UpdateWidgetWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                updateRequest
            )
        }
    }

    override suspend fun doWork(): Result {

        val userId = UserIdProvider(context).getUserId()
        if (userId == null) {
            return Result.failure()
        }

        return try {
            val applicationsSnapshot = Firebase.firestore.collection("travel_applications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", ApplicationStatus.Accepted.name)
                .get().await()

            val acceptedApps = applicationsSnapshot.toObjects(TravelApplication::class.java)

            if (acceptedApps.isEmpty()) {
                saveTravelsAndUpdateWidget(emptyList())
                return Result.success()
            }

            val proposalIds = acceptedApps.map { it.proposalId }.distinct()

            if (proposalIds.isEmpty()) {
                saveTravelsAndUpdateWidget(emptyList())
                return Result.success()
            }

            val allProposals = travelProposalRepository.observeAllProposals().first()
            val joinedProposals = allProposals.filter { it.proposalId in proposalIds }

            val today = LocalDate.now()
            val futureProposals = joinedProposals.filter {
                val startDate = it.startDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                startDate?.isAfter(today) == true
            }.sortedBy { it.startDate }

            val widgetTravels = futureProposals.map { proposal ->
                WidgetTravelInfo(
                    proposalId = proposal.proposalId,
                    name = proposal.name,
                    countdownText = getCountdownText(proposal, today)
                )
            }

            saveTravelsAndUpdateWidget(widgetTravels)
            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
    }

    private suspend fun saveTravelsAndUpdateWidget(travels: List<WidgetTravelInfo>) {
        val jsonString = Gson().toJson(travels)

        context.dataStoreInstance.edit { settings ->
            settings[WidgetPreferenceKeys.UPCOMING_TRAVELS_JSON] = jsonString
        }

        val glanceIds = GlanceAppWidgetManager(context)
            .getGlanceIds(TravelJoinedWidget::class.java)

        if (glanceIds.isEmpty()) {
            return
        }

        glanceIds.forEach { glanceId ->
            try {
                TravelJoinedWidget().update(context, glanceId)
            } catch (_: Exception) {
            }
        }
    }

    private fun getCountdownText(proposal: TravelProposal, today: LocalDate): String {
        val startDate = proposal.startDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
        return if (startDate != null) {
            val daysUntil = ChronoUnit.DAYS.between(today, startDate)
            val weeksUntil = ChronoUnit.WEEKS.between(today, startDate)
            val monthsUntil = ChronoUnit.MONTHS.between(today, startDate)

            when {
                monthsUntil >= 1 -> "In $monthsUntil months"
                weeksUntil >= 1 -> "In $weeksUntil weeks"
                daysUntil > 1 -> "In $daysUntil days"
                daysUntil == 1L -> "Tomorrow"
                else -> "Upcoming"
            }
        } else "Date not set"
    }
}