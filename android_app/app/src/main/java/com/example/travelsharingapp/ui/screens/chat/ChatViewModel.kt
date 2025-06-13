package com.example.travelsharingapp.ui.screens.chat

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.model.ChatMessage
import com.example.travelsharingapp.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _messageToEdit: MutableState<ChatMessage?> = mutableStateOf(null)
    val messageToEdit: MutableState<ChatMessage?> = _messageToEdit

    private val _unreadMessagesCount = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadMessagesCount: StateFlow<Map<String, Int>> = _unreadMessagesCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var messagesListenerJob: Job? = null
    private var unreadCountJobs = mutableMapOf<String, Job>()

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

    fun listenForUnreadCounts(proposalIds: List<String>, userId: String) {
        (proposalIds.toSet()).forEach {
            unreadCountJobs[it]?.cancel()
            unreadCountJobs.remove(it)
        }

        proposalIds.forEach { proposalId ->
            if (!unreadCountJobs.containsKey(proposalId)) {
                unreadCountJobs[proposalId] = viewModelScope.launch {
                    chatRepository.getUnreadMessagesCount(proposalId, userId).collect { count ->
                        _unreadMessagesCount.value = _unreadMessagesCount.value.toMutableMap().apply {
                            this[proposalId] = count
                        }
                    }
                }
            }
        }
    }

    fun markMessagesAsRead(proposalId: String, userId: String) {
        viewModelScope.launch {
            chatRepository.updateLastReadTimestamp(proposalId, userId)
        }
    }

    fun clearMessagesData() {
        messagesListenerJob?.cancel()
        unreadCountJobs.forEach {
            it.value.cancel()
        }

        _messages.value = emptyList()
        _unreadMessagesCount.value = emptyMap<String, Int>()
        _messageToEdit.value = null
        _isLoading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        clearMessagesData()
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
