package com.example.signup.loginscreens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.signup.R
import com.example.signup.navigation.Screens

@Composable
fun HomeScreen(navController: NavController) {
    val context= LocalContext.current
    var signUp by remember { mutableStateOf(false) }
    var logIn by remember { mutableStateOf(false) }
    val sharedPreferences = context.getSharedPreferences("SignUp", Context.MODE_PRIVATE)
    val sharedPreferencesId = sharedPreferences.getString("userId", null)
    LaunchedEffect(key1 = Unit) {
        if (sharedPreferencesId != null) {
            navController.navigate(Screens.Chat.route) {
                popUpTo(Screens.Home.route) { inclusive = true }
            }
        }
    }
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth().verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Welcome back", fontSize = 34.sp)
        
        Text(text = "Welcome back! Please enter your details.", color = Color(0XFF636364))
        Spacer(modifier = Modifier.height(14.dp))

        Image(
            painter = painterResource(id = R.drawable.applogo),
            contentDescription = "",
            contentScale = ContentScale.Crop, modifier = Modifier.size(500.dp)
        )

        Spacer(modifier = Modifier.height(15.dp))
        Button(
            onClick = {
                      navController.navigate(Screens.SignUp.route)
            },
            shape = RoundedCornerShape(9.dp),
            modifier = Modifier
                .width(300.dp)
                .height(45.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0XFF2148C0),
                contentColor = Color.White,
            )
        ) {
            Text(text = "Sign Up", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }


        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = {
                      navController.navigate(Screens.Login.route)
            },
            shape = RoundedCornerShape(9.dp),
            modifier = Modifier
                .width(300.dp)
                .height(45.dp),
        ) {
            Text(text = "Login", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
