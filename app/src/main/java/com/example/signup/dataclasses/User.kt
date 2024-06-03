package com.example.signup.dataclasses

data class User(
    val email: String? = "",
    val password: String? = ""
)


data class Message(
    val name:String?="",
    val email: String? ="",
    val password: String? ="",
    val userId:String?= "",
    var profileImageUrl: String? = null,
    var status:String? = null,
)

