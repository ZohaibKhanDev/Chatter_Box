package com.example.signup.picDetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

@Composable
fun PictureDetail(navController: NavController, pic: String?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        AsyncImage(model = pic, contentDescription = "", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
}