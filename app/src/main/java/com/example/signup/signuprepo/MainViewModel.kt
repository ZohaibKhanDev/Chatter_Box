package com.example.signup.signuprepo

import androidx.lifecycle.ViewModel
import com.example.signup.dataclasses.User

class MainViewModel(private val repo: Repository):ViewModel() {

    fun signUp(user: User)=repo.signUp(user)

    fun login(user: User)=repo.login(user)
}