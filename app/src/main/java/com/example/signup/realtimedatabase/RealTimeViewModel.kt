package com.example.signup.realtimedatabase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.signup.ChatScreens.Chats
import com.example.signup.dataclasses.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RealTimeViewModel(private val repository: RealTimeRepositoryImpl) : ViewModel() {
    private val _storeDataResult = MutableStateFlow<String?>(null)
    val storeDataResult: StateFlow<String?> get() = _storeDataResult

    private val _getDataResult = MutableStateFlow<Message?>(null)
    val getDataResult: StateFlow<Message?> get() = _getDataResult

    private val _writeUserMessageResult = MutableStateFlow<String?>(null)
    val writeUserMessageResult: StateFlow<String?> get() = _writeUserMessageResult

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    private val _currentUser = MutableLiveData<Message?>()
    val currentUser: LiveData<Message?> get() = _currentUser

    fun deleteProfile(deletedUser: Message) {
        _messages.value = _messages.value?.filter { it.userId != deletedUser.userId }
        if (_currentUser.value?.userId == deletedUser.userId) {
            _currentUser.value = null
        }
    }

    fun storeData(message: Message, userId: String) {
        viewModelScope.launch {
            val result = repository.storeData(message, userId)
            _storeDataResult.value = result
        }
    }

    fun getData(userId: String) {
        viewModelScope.launch {
            val result = repository.getData(userId)
            _getDataResult.value = result
        }
    }

    fun writeUserMessage(chats: Chats, userId: String) {
        viewModelScope.launch {
            val result = repository.writeUserMessage(chats, userId)
            _writeUserMessageResult.value = result
        }
    }



    fun clearResults() {
        _storeDataResult.value = null
        _getDataResult.value = null
        _writeUserMessageResult.value = null

    }
}
