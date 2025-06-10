import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.example.travelsharingapp.data.model.ChatMessage
import com.example.travelsharingapp.ui.screens.chat.ChatViewModel
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_application.ProfileAvatar
import kotlinx.coroutines.launch

@Composable
fun ChatRoomScreen(
    modifier: Modifier,
    proposalId: String,
    userId: String,
    userName: String,
    chatViewModel: ChatViewModel,
    topBarViewModel: TopBarViewModel,
    onNavigateBack: () -> Unit
) {
    val messages by chatViewModel.messages.collectAsState()
    var newMessage by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val showMenu = remember { mutableStateOf(false) }
    val selectedMessage = remember { mutableStateOf<ChatMessage?>(null) }
    val replyTarget = remember { mutableStateOf<ChatMessage?>(null) }

    val ownMessageColor   = MaterialTheme.colorScheme.primaryContainer
    val otherMessageColor = MaterialTheme.colorScheme.surfaceVariant
    val ownTextColor   = MaterialTheme.colorScheme.onPrimaryContainer
    val otherTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        chatViewModel.observeMessages(proposalId)
    }

    LaunchedEffect(proposalId) {
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
            LaunchedEffect(messages.size) {
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
                        val isOwnMessage = message.senderId == userId
                        val expanded = remember { mutableStateOf(false) }

                        // Track previousSenderId for consecutive sender logic
                        var previousSenderId by remember { mutableStateOf<String?>(null) }
                        val showProfileImage = previousSenderId != message.senderId
                        previousSenderId = message.senderId

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            if (!isOwnMessage) {
                                if (showProfileImage) {
                                    ProfileAvatar(
                                        imageSize = 28.dp,
                                        imageUrl = message.senderProfileImage
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(28.dp))
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isOwnMessage) ownMessageColor else otherMessageColor)
                                    .padding(12.dp)
                                    .widthIn(max = 280.dp)
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = {
                                            if (isOwnMessage) {
                                                selectedMessage.value = message
                                                expanded.value = true
                                            }
                                        }
                                    )
                                    .pointerInput(message) {
                                        detectHorizontalDragGestures(
                                            onDragEnd = {  }
                                        ) { change, dragAmount ->
                                            if (dragAmount > 120 && !isOwnMessage) {
                                                replyTarget.value = message
                                            }
                                        }
                                    }
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (!isOwnMessage) {
                                        Text(
                                            text = message.senderName,
                                            color = otherTextColor,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }

                                    message.replyToMessageId?.let { quotedId ->
                                        val quoted = messages.firstOrNull { it.messageId == quotedId }
                                        val previewSender = quoted?.senderName
                                        val previewText   = (quoted?.message ?: message.replyPreview).orEmpty()

                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 4.dp)
                                        ) {
                                            Column(Modifier.padding(6.dp)) {
                                                if (!previewSender.isNullOrBlank()) {
                                                    Text(
                                                        text = previewSender,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Text(
                                                    text = previewText.take(80),
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }

                                    if (message.imageUrl != null) {
                                        AsyncImage(
                                            model = message.imageUrl,
                                            contentDescription = "Immagine inviata",
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .sizeIn(maxWidth = 250.dp, maxHeight = 250.dp)
                                        )
                                    }

                                    if (message.message.isNotEmpty() || message.message == "__deleted__") {
                                        Text(
                                            text = if (message.message == "__deleted__") "This message was deleted." else message.message,
                                            color = if (isOwnMessage) ownTextColor else otherTextColor,
                                            style = if (message.message == "__deleted__")
                                                MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
                                            else
                                                MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                if (isOwnMessage && message.message != "__deleted__") {
                                    DropdownMenu(
                                        expanded = expanded.value,
                                        onDismissRequest = { expanded.value = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Edit") },
                                            onClick = {
                                                newMessage = message.message
                                                chatViewModel.setMessageToEdit(message)
                                                expanded.value = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete") },
                                            onClick = {
                                                chatViewModel.deleteMessage(
                                                    proposalId = proposalId,
                                                    message = message
                                                )
                                                expanded.value = false
                                            }
                                        )
                                    }
                                }
                            }

                            if (isOwnMessage) {
                                if (showProfileImage) {
                                    ProfileAvatar(
                                        imageSize = 28.dp,
                                        imageUrl = message.senderProfileImage
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(28.dp))
                                }
                            }
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

        if (replyTarget.value != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = replyTarget.value!!.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = replyTarget.value!!.message.take(80),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { replyTarget.value = null }) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel reply")
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        Row(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Image")
            }
            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
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

            IconButton(
                onClick = {
                    if (newMessage.isNotBlank() || selectedImageUri != null) {
                        val tempMessage = ChatMessage(
                            proposalId = proposalId,
                            senderId   = userId,
                            senderName = userName,
                            message    = newMessage.trim(),
                            replyToMessageId = replyTarget.value?.messageId,
                            replyPreview     = replyTarget.value?.message?.take(80)
                        )
                        val imageToSend = selectedImageUri

                        newMessage = ""
                        selectedImageUri = null

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
                                    imageUri = imageToSend
                                )
                            }
                            replyTarget.value = null
                        }
                    }
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}
