package com.example.signup.realtimedatabase

import com.example.signup.ChatScreens.Chats
import com.example.signup.dataclasses.Message

interface RealTimeService {
    suspend fun storeData(message: Message, userId: String):String

    suspend fun getData(userId: String): Message?

    suspend fun writeUserMessage(chats: Chats, userId: String):String

    suspend fun deleteMessage(userId: String, messageId: String): String


}