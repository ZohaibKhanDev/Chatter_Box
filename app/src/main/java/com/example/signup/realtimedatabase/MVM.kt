package com.example.signup.realtimedatabase


import android.media.Image
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.signup.ChatScreens.Chats
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainViewModel1(private val repository: Repository1) : ViewModel() {
    private val _messages = MutableStateFlow<List<Chats>>(emptyList())
    val messages: StateFlow<List<Chats>> get() = _messages

    fun loadMessages(senderId: String, receiverId: String) {
        viewModelScope.launch {
            repository.getMessages(senderId, receiverId).collect {
                Log.d("MainViewModel1", "Messages retrieved: $it")
                _messages.value = it
            }
        }
    }

    fun addMessage(message: Chats) {
        viewModelScope.launch {
            repository.addMessage(message)
        }
    }
}



class Repository1(private val databaseReference: DatabaseReference) {

    fun getMessages(senderId: String, receiverId: String): Flow<List<Chats>> = callbackFlow {
        val query = databaseReference.orderByChild("timestamp")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chats = snapshot.children.mapNotNull { it.getValue<Chats>() }
                    .filter { (it.senderId == senderId && it.receiverId == receiverId) ||
                            (it.senderId == receiverId && it.receiverId == senderId) }
                trySend(chats)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun addMessage(message: Chats) {
        databaseReference.push().setValue(message)
    }
}

class profileMainViewModel(private val repository: ProfileRepository) : ViewModel() {
    private val _profile = MutableStateFlow<Image?>(null)
    val profile: StateFlow<Image?> get() = _profile

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> get() = _statusMessage

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            repository.getProfile(userId).collect {
                _profile.value = it
            }
        }
    }

    fun writeProfile(image: Image, userId: String) {
        viewModelScope.launch {
            val result = repository.writeProfile(image, userId)
            _statusMessage.value = result
        }
    }
}

class ProfileRepository(private val databaseReference: DatabaseReference) {

    fun getProfile(userId: String): Flow<Image?> = callbackFlow {
        val profileRef = databaseReference.child("profile").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue(Image::class.java)
                trySend(profile)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        profileRef.addValueEventListener(listener)
        awaitClose { profileRef.removeEventListener(listener) }
    }

    suspend fun writeProfile(image: Image, userId: String): String {
        return try {
            val profileRef = databaseReference.child("profile").child(userId)
            profileRef.setValue(image).await()
            "User Data Stored Successfully"
        } catch (e: Exception) {
            e.message ?: "Error storing user data"
        }
    }
}