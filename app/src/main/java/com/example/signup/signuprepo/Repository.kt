package com.example.signup.signuprepo

import com.example.signup.dataclasses.User
import kotlinx.coroutines.flow.Flow

interface Repository {

    fun signUp(user: User):Flow<ResultState<String>>

    fun login(user: User):Flow<ResultState<String>>
}