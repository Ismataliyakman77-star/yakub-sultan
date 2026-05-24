package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.MessageEntity
import com.example.data.repository.JarvisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = JarvisRepository(application)

    val chatHistory: StateFlow<List<MessageEntity>> = repository.getChatHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _preferredLanguage = MutableStateFlow("AUTO") // "AUTO", "EN", "AM"
    val preferredLanguage: StateFlow<String> = _preferredLanguage.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    init {
        // Initialize with default greeting if history is empty
        viewModelScope.launch {
            repository.getChatHistory().collect { list ->
                if (list.isEmpty()) {
                    repository.clearHistory()
                    repository.addMessage("Online and ready, sir. Secure holographic display activated. Fluent English and Amharic cognitive services loaded. How may I be of assistance today?", "jarvis")
                }
            }
        }
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun setPreferredLanguage(lang: String) {
        _preferredLanguage.value = lang
    }

    fun sendMessage() {
        val query = _inputText.value.trim()
        if (query.isEmpty() || _isThinking.value) return

        _inputText.value = ""
        viewModelScope.launch {
            // Add user message to DB
            repository.addMessage(query, "user")
            
            _isThinking.value = true
            
            // Query Gemini REST service
            val responseText = repository.getJarvisResponse(
                prompt = query,
                conversationHistory = chatHistory.value,
                preferredLanguage = _preferredLanguage.value
            )
            
            // Add J.A.R.V.I.S response to DB
            repository.addMessage(responseText, "jarvis")
            
            _isThinking.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearHistory()
            repository.addMessage("Online and ready, sir. Secure holographic display activated. Fluent English and Amharic cognitive services loaded. How may I be of assistance today?", "jarvis")
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JarvisViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return JarvisViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
