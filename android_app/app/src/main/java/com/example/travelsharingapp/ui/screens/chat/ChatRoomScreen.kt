package com.example.travelsharingapp.ui.screens.chat

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelsharingapp.data.model.ChatMessage
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatRoomScreen(
    proposalId: String,
    userId: String,
    userName: String,
    chatViewModel: ChatViewModel,
    topBarViewModel: TopBarViewModel,
    onNavigateBack: () -> Unit,
    showBottomBar: MutableState<Boolean>
) {
    val messages by chatViewModel.messages.collectAsState()
    var newMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "Group Chat",
            navigationIcon = {
                IconButton(onClick = {
                    showBottomBar.value = true  //Bottom bar di nuovo visibile
                    onNavigateBack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = null
        )
        showBottomBar.value = false
        chatViewModel.observeMessages(proposalId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true
            ) {
                items(messages.reversed(), key = { it.messageId }) { message ->
                    val isOwnMessage = message.senderId == userId
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isOwnMessage) MaterialTheme.colorScheme.primary else Color.Gray)
                                .padding(12.dp)
                                .widthIn(max = 280.dp)
                        ) {
                            if (!isOwnMessage) {
                                Text(
                                    text = message.senderName,
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 12.sp)
                                )
                            }
                            Text(
                                text = message.message,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                            )
                        }
                    }
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
                    if (newMessage.isNotBlank()) {
                        coroutineScope.launch {
                            chatViewModel.sendMessage(
                                proposalId = proposalId,
                                message = ChatMessage(
                                    proposalId = proposalId,
                                    senderId = userId,
                                    senderName = userName,
                                    message = newMessage.trim()
                                )
                            )
                            newMessage = ""
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}
