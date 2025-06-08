import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import coil.compose.AsyncImage
import com.example.travelsharingapp.ui.screens.chat.ChatViewModel
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.travelsharingapp.data.model.ChatMessage
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
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

    val ownMessageColor = Color(0xFFD1C4E9) // Lavanda chiaro
    val otherMessageColor = Color(0xFFE1F5FE) // Azzurro chiaro

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
        chatViewModel.observeMessages(proposalId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
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
            val listState = rememberLazyListState()
            val sortedMessages = messages.sortedBy { it.timestamp }
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.lastIndex)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                //reverseLayout = true
            ) {
                items(
                    count = sortedMessages.size,
                    key = { index -> sortedMessages[index].messageId },
                    contentType = { "MessageCard" },
                    itemContent = { index ->
                        val message = sortedMessages[index]
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
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (!isOwnMessage) {
                                        Text(
                                            text = message.senderName,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            style = MaterialTheme.typography.labelSmall
                                        )
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
                                            color = if (isOwnMessage) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                            style = if (message.message == "__deleted__")
                                                MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
                                            else
                                                MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                if (isOwnMessage) {
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

        // Chat Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .imePadding()
                .navigationBarsPadding(),
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
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
                placeholder = { Text("Type a message") },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
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
                            senderId = userId,
                            senderName = userName,
                            message = newMessage.trim()
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
                        }
                    }
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }

    }
}

@Composable
fun ProfileAvatar(imageSize: Dp, imageUrl: String?) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Profile Image",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(imageSize)
            .clip(CircleShape)
            .background(Color.Gray)
    )
}
