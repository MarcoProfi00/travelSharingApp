package com.example.travelsharingapp.ui.screens.chat

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.model.ChatMessage
import com.example.travelsharingapp.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    application: Application,
    private val chatRepository: ChatRepository
) : AndroidViewModel(application) {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _messageToEdit: MutableState<ChatMessage?> = mutableStateOf(null)
    val messageToEdit: MutableState<ChatMessage?> = _messageToEdit


    fun setMessageToEdit(message: ChatMessage?) {
        _messageToEdit.value = message
    }

    fun observeMessages(proposalId: String) {
        chatRepository.getMessages(proposalId) { newMessages ->
            _messages.value = newMessages
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
}

class ChatViewModelFactory(
    private val application: Application,
    private val chatRepository: ChatRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(application, chatRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
