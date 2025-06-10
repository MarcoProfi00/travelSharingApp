package com.example.travelsharingapp.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.travelsharingapp.data.model.ChatMessage
import com.example.travelsharingapp.data.model.UserProfile
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_application.ProfileAvatar
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ChatRoomScreen(
    modifier: Modifier,
    proposalId: String,
    userId: String,
    chatViewModel: ChatViewModel,
    topBarViewModel: TopBarViewModel,
    userProfileViewModel: UserProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val messages by chatViewModel.messages.collectAsState()
    val isLoadingMessages by chatViewModel.isLoading.collectAsState()

    var newMessage by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    var selectedMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var replyTarget by remember { mutableStateOf<ChatMessage?>(null) }

    val listState = rememberLazyListState()

    LaunchedEffect(proposalId) {
        chatViewModel.startListeningMessagesByProposalId(proposalId)
    }

    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "Group Chat",
            navigationIcon = {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = null
        )
    }

    if (isLoadingMessages) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Text("Loading messages...")
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        if (messages.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No messages yet. Start the conversation!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            LaunchedEffect(Unit, messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.lastIndex)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(
                    count = messages.size,
                    key = { index -> messages[index].messageId },
                    contentType = { "MessageCard" },
                    itemContent = { index ->
                        val message = messages[index]
                        val previousMessage = messages.getOrNull(index - 1)
                        val isNewSender = previousMessage?.senderId != message.senderId
                        val paddingTop = if (isNewSender) 12.dp else 2.dp

                        val isOwnMessage = message.senderId == userId
                        var expanded by remember { mutableStateOf(false) }
                        var offsetX by remember { mutableFloatStateOf(0f) }

                        val onQuoteClicked = { replyMessage: ChatMessage ->
                            val targetIndex = messages.indexOfFirst { it.messageId == replyMessage.messageId }
                            if (targetIndex != -1) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(targetIndex)
                                }
                            }
                        }

                        val user by userProfileViewModel.observeUserProfileById(message.senderId).collectAsState()
                        user?.let {
                            MessageCard(
                                modifier = Modifier
                                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                                    .padding(top = paddingTop)
                                    .pointerInput(message) {
                                        detectHorizontalDragGestures(
                                            onDragStart = {
                                                offsetX = 0f
                                            },
                                            onHorizontalDrag = { pointerInputChange, dragAmount ->
                                                pointerInputChange.consume()
                                                offsetX += dragAmount
                                            },
                                            onDragEnd = {
                                                if (offsetX > 80f) {
                                                    replyTarget = message
                                                }
                                                offsetX = 0f
                                            },
                                            onDragCancel = {
                                                offsetX = 0f
                                            }
                                        )
                                },
                                message = message,
                                isOwnMessage = isOwnMessage,
                                isNewSender = isNewSender,
                                user = user,
                                userProfileViewModel = userProfileViewModel,
                                onLongClick = {
                                    if (isOwnMessage) {
                                        selectedMessage = message
                                        expanded = true
                                    }
                                },
                                onEdit = {
                                    newMessage = message.message
                                    chatViewModel.setMessageToEdit(message)
                                    expanded = false
                                },
                                onDelete = {
                                    chatViewModel.deleteMessage(
                                        proposalId = proposalId,
                                        message = message
                                    )
                                    expanded = false
                                },
                                expanded = expanded,
                                onDismiss = { expanded = false },
                                onQuoteClicked = onQuoteClicked
                            )
                        }
                    }
                )
            }
        }

        if (selectedImageUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected Image Preview",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .sizeIn(maxWidth = 100.dp, maxHeight = 100.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { selectedImageUri = null }) {
                    Icon(Icons.Default.Close, contentDescription = "Remove Image")
                }
            }
        }

        if (replyTarget != null) {
            val user by userProfileViewModel.observeUserProfileById(replyTarget!!.senderId).collectAsState()
            user?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "Reply",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Reply to " + user!!.firstName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = replyTarget!!.message.take(80),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { replyTarget = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel reply")
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        MessageInput(
            newMessage = newMessage,
            onValueChange = { newMessage = it },
            onImageClick = { imagePickerLauncher.launch("image/*") },
            onSendClick = {
                if (newMessage.isNotBlank() || selectedImageUri != null) {
                    val tempMessage = ChatMessage(
                        proposalId = proposalId,
                        senderId   = userId,
                        message    = newMessage.trim(),
                        replyToMessage = replyTarget
                    )
                    newMessage = ""

                    coroutineScope.launch {
                        if (chatViewModel.messageToEdit.value != null) {
                            chatViewModel.updateMessage(
                                proposalId = proposalId,
                                messageId = chatViewModel.messageToEdit.value!!.messageId,
                                newText = tempMessage.message
                            )
                            chatViewModel.setMessageToEdit(null)
                        } else {
                            chatViewModel.sendMessageWithImage(
                                proposalId = proposalId,
                                message = tempMessage,
                                imageUri = selectedImageUri
                            )
                            selectedImageUri = null
                        }
                        replyTarget = null
                    }
                }
            }
        )
    }
}

@Composable
fun MessageCard(
    modifier: Modifier = Modifier,
    message: ChatMessage,
    isOwnMessage: Boolean,
    isNewSender: Boolean,
    user: UserProfile?,
    userProfileViewModel: UserProfileViewModel,
    onLongClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onQuoteClicked: (ChatMessage) -> Unit
) {
    val ownMessageColor   = MaterialTheme.colorScheme.primaryContainer
    val otherMessageColor = MaterialTheme.colorScheme.surfaceVariant
    val ownTextColor   = MaterialTheme.colorScheme.onPrimaryContainer
    val otherTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isOwnMessage) {
            if (isNewSender) {
                ProfileAvatar(
                    imageSize = 40.dp,
                    user = user
                )
            } else {
                Spacer(modifier = Modifier.width(40.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .widthIn(max = 280.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                ),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isOwnMessage) ownMessageColor else otherMessageColor
            ),
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                if (!isOwnMessage) {
                    Text(
                        text = user!!.firstName + " " + user.lastName,
                        color = otherTextColor,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                if (message.deleted) {
                    Text(
                        text = "This message was deleted.",
                        color = if (isOwnMessage) ownTextColor else otherTextColor,
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
                    )
                } else if (message.message.isNotBlank()) {
                    message.replyToMessage?.let { replyMessage ->
                        val user by userProfileViewModel.observeUserProfileById(replyMessage.senderId).collectAsState()
                        user?.let {
                            val quotedUsername = it.firstName + " " + it.lastName
                            val quotedText = replyMessage.message.take(80)
                            val quotedImageUrl = replyMessage.imageUrl

                            Row(
                                modifier = modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .clickable { onQuoteClicked(replyMessage) }
                                    .padding(bottom = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(4.dp)
                                        .background(MaterialTheme.colorScheme.primary)
                                )

                                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                    Text(
                                        text = quotedUsername,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    if (quotedImageUrl != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Photo,
                                                contentDescription = "Image reply",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "Photo",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else if (quotedText.isNotBlank()) {
                                        Text(
                                            text = quotedText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    message.imageUrl?.let {
                        AsyncImage(
                            model = message.imageUrl,
                            contentDescription = "Sent image",
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .sizeIn(maxWidth = 250.dp, maxHeight = 250.dp)
                                .padding(bottom = 4.dp)
                        )
                    }

                    Box {
                        Text(
                            text = message.message,
                            color = if (isOwnMessage) ownTextColor else otherTextColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 56.dp)
                        )

                        Text(
                            text = formatTimestamp(message.timestamp),
                            color = (if (isOwnMessage) ownTextColor else otherTextColor).copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                        )
                    }
                }

                if (isOwnMessage && !message.deleted) {
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = onDismiss
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = onEdit
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = onDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageInput (
    newMessage: String,
    onValueChange: (String) -> Unit,
    onImageClick: () -> Unit,
    onSendClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onImageClick,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Image")
        }

        TextField(
            value = newMessage,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp),
            singleLine = false,
            maxLines = 4,
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = onSendClick) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
        }
    }
}

fun formatTimestamp(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}