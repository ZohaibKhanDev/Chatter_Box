package com.example.signup.signuprepo

import android.content.Context
import com.example.signup.dataclasses.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class RepositoryImpl(private val db: FirebaseAuth, private val context: Context): Repository {
    override fun signUp(user: User): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)
        db.createUserWithEmailAndPassword(
            user.email!!,
            user.password!!
        ).addOnSuccessListener {
            trySend(ResultState.Success("Sign Up Success"))
        }.addOnFailureListener {
            trySend(ResultState.Error(it))
        }
        awaitClose { close() }
    }

    override fun login(user: User): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)
        db.signInWithEmailAndPassword(
            user.email!!,
            user.password!!
        ).addOnSuccessListener {
              /* val sharedPreferences = context.getSharedPreferences("SignUp", Context.MODE_PRIVATE)
               sharedPreferences.edit().putString("userId", it.user?.uid).apply()*/
            trySend(ResultState.Success("Login Success"))
        }.addOnFailureListener {
            trySend(ResultState.Error(it))
        }

        awaitClose { close() }
    }
}