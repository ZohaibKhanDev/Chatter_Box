package com.example.signup.realtimedatabase

import androidx.compose.runtime.mutableStateOf
import com.example.signup.ChatScreens.Chats
import com.example.signup.dataclasses.Message
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.getValue

class RealTimeRepositoryImpl(private val db:DatabaseReference):RealTimeService {
    override suspend fun storeData(message: Message, userId: String): String {
        val isSuccessful = mutableStateOf("")
        val myRef= db.database.getReference("User").child(userId)
        myRef.child(userId).setValue(message).addOnSuccessListener {
            isSuccessful.value = "Success"
        }.addOnFailureListener {
            isSuccessful.value="Error"
        }
       return isSuccessful.value
    }

    override suspend fun getData(userId: String): Message? {
        val isSuccessful = mutableStateOf<Message?>(null)
        val myRef=db.database.getReference("User")
        myRef.child(userId).get().addOnSuccessListener {
            val message = it.getValue<Message>()
            isSuccessful.value=message
        }
        return isSuccessful.value
    }


    override suspend fun writeUserMessage(chats: Chats, userId: String): String {
        val isSuccessful = mutableStateOf("")
        val messRef = db.database.getReference().child("Message").child("chat").child(userId)
        messRef.setValue(chats).addOnSuccessListener {
            isSuccessful.value = "User Data Stored Successfully"
        }.addOnFailureListener {
            isSuccessful.value = "${it.message}"
        }
        return isSuccessful.value
    }


    override suspend fun deleteMessage(userId: String, messageId: String): String {
        val isSuccessful = mutableStateOf("")
        val messageRef = db.database.getReference("User").child(userId).child(messageId)

        // Remove the message from the database
        messageRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                isSuccessful.value = "Message deleted successfully"
            } else {
                isSuccessful.value = "Error deleting message: ${task.exception?.message}"
            }
        }

        return isSuccessful.value
    }

}