package com.example.signup.loginscreens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.signup.signuprepo.MainViewModel
import com.example.signup.messageviewmodel.MainViewModel2
import com.example.signup.R
import com.example.signup.messageviewmodel.Repository2
import com.example.signup.signuprepo.ResultState
import com.example.signup.navigation.Screens
import com.example.signup.dataclasses.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: MainViewModel = koinInject()
    val userId = Firebase.auth.currentUser?.uid
    val myRef = Firebase.database.getReference("User")
    val repository = remember {
        Repository2(myRef)
    }
    val viewModel1 = remember {
        MainViewModel2(repository)
    }
    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var isLogin by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()
    var eye by remember {
        mutableStateOf(false)
    }

    if (isLogin) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 25.dp, end = 25.dp, top = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.login),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(200.dp),
            alignment = Alignment.TopCenter
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = {
            email = it
        }, label = {
            Text(text = "EMAIL")
        }, modifier = Modifier
            .width(300.dp)
            .height(60.dp), leadingIcon = {
            Icon(imageVector = Icons.Outlined.Person, contentDescription = "")
        })

        Spacer(modifier = Modifier.height(16.dp))


        OutlinedTextField(value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text(text = "PASSWORD")
            },
            modifier = Modifier
                .width(300.dp)
                .height(60.dp),
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Lock, contentDescription = "")
            },
            trailingIcon = {
                if (password >= 1.toString()) {
                    Icon(
                        imageVector = if (eye) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "",
                        modifier = Modifier.clickable {
                            eye = !eye
                        }
                    )
                }
            },
            visualTransformation = if (eye) VisualTransformation.None else PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch(Dispatchers.Main) {
                    viewModel.login(
                        User(
                            email, password
                        )
                    ).collect {
                        when (it) {
                            is ResultState.Error -> {
                                isLogin = false
                                Toast.makeText(context, "${it.error}", Toast.LENGTH_SHORT).show()
                            }

                            ResultState.Loading -> {
                                isLogin = true
                            }

                            is ResultState.Success -> {
                                isLogin = false
                                Toast.makeText(context, it.response, Toast.LENGTH_SHORT).show()
                                navController.navigate(Screens.Chat.route)
                                val sharedPreferences = context.getSharedPreferences("SignUp", Context.MODE_PRIVATE)
                                sharedPreferences.edit().putString("userId", userId).apply()
                            }

                            else -> {}
                        }
                    }
                }
            },
            modifier = Modifier
                .width(300.dp)
                .height(45.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0XFF2148C0),
                contentColor = Color.White
            )
        ) {
            Text(text = "Login")
        }
    }
}