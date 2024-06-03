package com.example.signup.profile

import android.net.Uri
import android.util.Log
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.signup.dataclasses.Message
import com.example.signup.loginscreens.getImageUrlFromPrefs
import com.example.signup.loginscreens.saveImageUrlToPrefs
import com.example.signup.navigation.Screens
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PREFS_NAME = "prefs"
private const val PREF_UPLOADED_IMAGE_URL = "uploaded_image_url"

@Composable
fun ProfileScreen(navController: NavController) {
    var isUploading by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(listOf<Message>()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    var currentUser by remember { mutableStateOf<Message?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val dbRef = Firebase.database.getReference("User")
        val userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<Message>()
                val user = Firebase.auth.currentUser
                var currentUserEmail: String? = null

                if (user != null) {
                    currentUserEmail = user.email
                }

                for (userSnapshot in snapshot.children) {
                    val message = userSnapshot.getValue(Message::class.java)
                    if (message != null) {
                        userList.add(message)
                        if (message.email == currentUserEmail) {
                            currentUser = message
                        }
                        Log.d("SignUp", "Fetched message: $message")
                    } else {
                        Log.d("SignUp", "Fetched null message")
                    }
                }
                messages = userList
                loading = false
                Log.d("SignUp", "Data fetched: $userList")
            }

            override fun onCancelled(error: DatabaseError) {
                loading = false
                Log.e("SignUp", "Data fetch cancelled or failed", error.toException())
            }
        }
        dbRef.addValueEventListener(userListener)
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0XFF1B202D)), contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp), colors = CardDefaults.cardColors(Color(0XFF1B202D))
        ) {
            Column(
                modifier = Modifier
                    .size(100.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(45.dp)
                        .background(Color.Gray.copy(alpha = 0.50f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Profile Icon",
                        tint = Color.White,
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                currentUser?.let {


                    LaunchedEffect(key1 = Unit) {
                        it.profileImageUrl = getImageUrlFromPrefs(context)
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
                                val uploadTask = imageRef.putFile(uri)
                                uploadTask.continueWithTask { task ->
                                    if (!task.isSuccessful) {
                                        task.exception?.let { throw it }
                                    }
                                    imageRef.downloadUrl
                                }.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val downloadUrl = task.result
                                        currentUser?.let { user ->
                                            user.profileImageUrl = downloadUrl.toString()
                                            val userId = Firebase.auth.currentUser?.uid
                                            userId?.let { uid ->
                                                Firebase.database.getReference("User").child(uid)
                                                    .setValue(user)
                                                    .addOnSuccessListener {
                                                        saveImageUrlToPrefs(
                                                            context,
                                                            user.profileImageUrl!!
                                                        )
                                                        Toast.makeText(
                                                            context,
                                                            "Profile com.example.signup.Image Uploaded Successfully",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }.addOnFailureListener { e ->
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to update profile image URL: ${e.message}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        Log.e(
                                                            "ProfileScreen",
                                                            "Failed to update profile image URL",
                                                            e
                                                        )
                                                    }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Failed to upload image: ${task.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.e(
                                            "ProfileScreen",
                                            "Failed to upload image",
                                            task.exception
                                        )
                                    }
                                    isUploading = false
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                }
                                isUploading = false
                            }
                        }

                    }

                    if (it.profileImageUrl != null) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(200.dp)
                                .background(Color.Cyan),
                            contentAlignment = Alignment.Center
                        ) {

                            it.profileImageUrl?.let {
                                AsyncImage(
                                    model = it,
                                    contentDescription = "",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            val encodedImageUrl = Uri.encode(it)
                                            navController.navigate(Screens.ProfileDetail.route + "/$encodedImageUrl")
                                        },
                                    contentScale = ContentScale.Crop
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

                    Text(
                        text = it.name.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold, color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileItem(title = "Email", value = it.email.toString())
                    ProfileItem(title = "Password", value = it.password.toString())
                    ProfileItem(title = "ID", value = it.userId.toString())
                }
            }
        }
    }
}


@Composable
fun ProfileItem(title: String, value: String) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth().padding(10.dp)
                .background(Color(0XFF1B202D)),
            horizontalAlignment = Alignment.Start
        ) {

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal, color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

    }
}


