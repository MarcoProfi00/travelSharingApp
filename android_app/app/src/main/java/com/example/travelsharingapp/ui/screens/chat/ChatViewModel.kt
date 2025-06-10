package com.example.travelsharingapp.ui.screens.chat

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.model.ChatMessage
import com.example.travelsharingapp.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel(
    private val chatRepository: ChatRepository,
) : ViewModel() {


    private val _unreadMessagesCount = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadMessagesCount: StateFlow<Map<String, Int>> = _unreadMessagesCount

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _messageToEdit: MutableState<ChatMessage?> = mutableStateOf(null)
    val messageToEdit: MutableState<ChatMessage?> = _messageToEdit

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var messagesListenerJob: Job? = null

    fun startListeningMessagesByProposalId(proposalId: String) {
        messagesListenerJob?.cancel()
        _isLoading.value = true
        messagesListenerJob = viewModelScope.launch {
            chatRepository.observeMessagesByProposal(proposalId)
                .collect { messagesList ->
                    _messages.value = messagesList
                    _isLoading.value = false
                }
        }
    }

    fun setMessageToEdit(message: ChatMessage?) {
        _messageToEdit.value = message
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
    private val chatRepository: ChatRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass
            .getConstructor(ChatRepository::class.java)
            .newInstance(chatRepository)
    }
}
