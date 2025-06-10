package com.example.travelsharingapp.ui.screens.chat

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.model.ChatMessage
import com.example.travelsharingapp.data.repository.ChatRepository
import com.example.travelsharingapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    application: Application,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {


    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _messageToEdit: MutableState<ChatMessage?> = mutableStateOf(null)
    val messageToEdit: MutableState<ChatMessage?> = _messageToEdit

    private val profileImageCache = mutableMapOf<String, String?>()

    private val _unreadMessagesCount = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadMessagesCount: StateFlow<Map<String, Int>> = _unreadMessagesCount

    fun setMessageToEdit(message: ChatMessage?) {
        _messageToEdit.value = message
    }

    fun observeMessages(proposalId: String) {
        chatRepository.getMessages(proposalId) { newMessages ->
            viewModelScope.launch {
                _messages.value = enrichMessagesWithProfileImages(newMessages)
            }
        }
    }

    suspend fun sendMessage(proposalId: String, message: ChatMessage) {
        chatRepository.sendMessage(proposalId, message)
    }

    fun updateMessage(proposalId: String, messageId: String, newText: String) {
        viewModelScope.launch {
            chatRepository.updateMessage(proposalId, messageId, newText)
        }
    }

    fun deleteMessage(proposalId: String, message: ChatMessage) {
        viewModelScope.launch {
            chatRepository.deleteMessage(proposalId, message)
        }
    }

    fun sendMessageWithImage(
        proposalId: String,
        message: ChatMessage,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            val finalMsg = if (imageUri != null) {
                val url = chatRepository.uploadChatImage(proposalId, imageUri)
                message.copy(imageUrl = url)
            } else {
                message
            }

            chatRepository.sendMessage(proposalId, finalMsg)
        }
    }

    suspend fun enrichMessagesWithProfileImages(messages: List<ChatMessage>): List<ChatMessage> {
        return messages.map { message ->
            val imageUrl = profileImageCache[message.senderId] ?: run {
                val profile = userRepository.getUserProfile(message.senderId)
                val url = profile?.profileImage
                profileImageCache[message.senderId] = url
                url
            }
            message.copy(senderProfileImage = imageUrl)
        }
    }

    fun updateUnreadMessages(proposalId: String, count: Int) {
        _unreadMessagesCount.value = _unreadMessagesCount.value.toMutableMap().apply {
            this[proposalId] = count
        }
    }
}

class ChatViewModelFactory(
    private val application: Application,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(application, chatRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
