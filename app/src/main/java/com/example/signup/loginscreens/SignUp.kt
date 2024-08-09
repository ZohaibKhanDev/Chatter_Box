package com.example.signup.loginscreens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.signup.signuprepo.MainViewModel
import com.example.signup.messageviewmodel.MainViewModel2
import com.example.signup.dataclasses.Message
import com.example.signup.messageviewmodel.Repository2
import com.example.signup.signuprepo.ResultState
import com.example.signup.navigation.Screens
import com.example.signup.dataclasses.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

const val PREFS_NAME = "prefs"
const val PREF_UPLOADED_IMAGE_URL = "uploaded_image_url"

@Composable
fun SignUp(navController: NavController) {
    val context = LocalContext.current
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val viewModel: MainViewModel = koinInject()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val myRef = Firebase.database.getReference("User")
    val repository = remember {
        Repository2(myRef)
    }
    val viewModel1 = remember {
        MainViewModel2(repository)
    }
    var dialog by remember { mutableStateOf(false) }
    var eye by remember { mutableStateOf(false) }




    LaunchedEffect(key1 = Unit) {
        uploadedImageUrl = getImageUrlFromPrefs(context)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            selectedImageUri = uri
        })

    selectedImageUri?.let { uri ->
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("image/${uri.lastPathSegment}")

        LaunchedEffect(key1 = uri) {
            try {
                isUploading = true
                imageRef.putFile(uri).await()
                val downloadUrl = imageRef.downloadUrl.await()
                uploadedImageUrl = downloadUrl.toString()
                saveImageUrlToPrefs(context, uploadedImageUrl!!)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "com.example.signup.Image Uploaded Successfully", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }

            } finally {
                isUploading = false
            }
        }
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(top = 50.dp, start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Text(
            text = "Create Account",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0XFF152CFE)
        )


        Spacer(modifier = Modifier.height(11.dp))

        if (uploadedImageUrl != null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(200.dp)
                    .background(Color.Cyan),
                contentAlignment = Alignment.Center
            ) {

                uploadedImageUrl?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = "",
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                    )
                }
                if (isUploading) {
                    Box(
                        modifier = Modifier.size(201.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.White,
                            trackColor = Color.Red
                        )
                    }

                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 37.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "",
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { launcher.launch("image/*") }

                        )
                    }
                }
            }
        } else {
            Box(

                modifier = Modifier
                    .clip(CircleShape)
                    .size(200.dp)
                    .background(Color.Cyan),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Icon",
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 37.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "",
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { launcher.launch("image/*") }

                        )
                    }
                }

            }

        }


        Spacer(modifier = Modifier.height(11.dp))

        TextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text(text = "USERNAME") },
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0XFFEBF3FF),
                unfocusedIndicatorColor = Color(0XFFEBF3FF),
                focusedContainerColor = Color(0XFFEBF3FF),
                unfocusedContainerColor = Color(0XFFEBF3FF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
        )
        Spacer(modifier = Modifier.height(11.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "EMAIL") },
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0XFFEBF3FF),
                unfocusedIndicatorColor = Color(0XFFEBF3FF),
                focusedContainerColor = Color(0XFFEBF3FF),
                unfocusedContainerColor = Color(0XFFEBF3FF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
        )
        Spacer(modifier = Modifier.height(11.dp))

        if (dialog) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }else{

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = "PASSWORD") },
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0XFFEBF3FF),
                    unfocusedIndicatorColor = Color(0XFFEBF3FF),
                    focusedContainerColor = Color(0XFFEBF3FF),
                    unfocusedContainerColor = Color(0XFFEBF3FF)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp),
                trailingIcon = {
                    if (password.isNotEmpty()) {
                        Icon(
                            imageVector = if (eye) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "",
                            modifier = Modifier.clickable { eye = !eye }
                        )
                    }
                },
                visualTransformation = if (eye) VisualTransformation.None else PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch(Dispatchers.Main) {
                        viewModel.signUp(User(email, password)).collect {
                            when (it) {
                                is ResultState.Error -> {
                                    dialog = false
                                    Toast.makeText(context, "${it.error}", Toast.LENGTH_SHORT).show()
                                }

                                ResultState.Loading -> {
                                    dialog = true
                                }

                                is ResultState.Success -> {
                                    dialog = false
                                    Toast.makeText(context, it.response, Toast.LENGTH_SHORT).show()
                                    navController.navigate(Screens.Login.route)

                                    val userId = Firebase.auth.currentUser?.uid
                                    if (userId != null) {
                                        val userProfile = Message(
                                            userName,
                                            email,
                                            password,
                                            userId,
                                            uploadedImageUrl ?: "",
                                            "" ?: ""
                                        )
                                        myRef.child(userId).setValue(userProfile)
                                    } else {
                                        Toast.makeText(context, "User ID is null", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0XFF1FB7E8),
                    contentColor = Color.White
                )
            ) {
                Text(text = "SignUp")
            }

        }
    }
}


fun saveImageUrlToPrefs(context: Context, url: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(PREF_UPLOADED_IMAGE_URL, url).apply()
}

fun getImageUrlFromPrefs(context: Context): String? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(PREF_UPLOADED_IMAGE_URL, null)
}
