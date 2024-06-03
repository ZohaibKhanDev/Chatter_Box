package com.example.signup.signuprepo

import java.lang.Exception

sealed class ResultState <out T> {
    object Loading: ResultState<Nothing>()
    data class Success<T>(val response: T): ResultState<T>()
    data class Error(val error: Exception): ResultState<Nothing>()
}