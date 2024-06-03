package com.example.signup.ChatScreens

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.TextStyle
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

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChatScreen(navController: NavController) {
    var messages by remember { mutableStateOf(listOf<Message>()) }
    var filteredMessages by remember { mutableStateOf(listOf<Message>()) }
    var searchQuery by remember { mutableStateOf("") }
    var search by remember { mutableStateOf(false) }
    var uploadImageUrl by remember {
        mutableStateOf<String?>(null)
    }
    var isDataLoaded by remember { mutableStateOf(false) }
    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var isUploading by remember {
        mutableStateOf(false)
    }
    var currentUser by remember { mutableStateOf<Message?>(null) }
    var loading by remember { mutableStateOf(true) }
    var logout by remember { mutableStateOf(false) }
    var openPhoto by remember { mutableStateOf(false) }


    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("SignUp", Context.MODE_PRIVATE)
    val sharedPreferencesId = sharedPreferences.getString("userId", null)

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
                filteredMessages = userList
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

    LaunchedEffect(key1 = Unit) {
        uploadImageUrl = getImageUrlFromPrefs(context)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = {
            selectedImageUri = it
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
                            user.status = downloadUrl.toString()
                            val userId = Firebase.auth.currentUser?.uid
                            userId?.let { uid ->
                                Firebase.database.getReference("User").child(uid)
                                    .setValue(user)
                                    .addOnSuccessListener {
                                        saveImageUrlToPrefs(
                                            context,
                                            user.status!!
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
    if (openPhoto) {
        launcher.launch("image/*")
    }

    Scaffold(topBar = {
        LargeTopAppBar(
            title = {
                if (search) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query
                            filteredMessages = if (query.isEmpty()) {
                                messages
                            } else {
                                messages.filter {
                                    it.name!!.contains(query, ignoreCase = true)
                                }
                            }
                        },
                        placeholder = { Text(text = "Search users", color = Color.White) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0XFF1B202D))
                            .padding(8.dp), textStyle = TextStyle(
                            fontSize = 18.sp,
                        ), colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.White.copy(alpha = 0.50f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.50f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ), shape = RoundedCornerShape(15.dp), trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "",
                                tint = Color.White,
                                modifier = Modifier.clickable { search = false })
                        }
                    )
                } else {


                    LazyRow(
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(bottom = 17.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            if (loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    trackColor = Color.Red,
                                    modifier = Modifier.size(50.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .clickable { launcher.launch("image/*") }
                                        .clip(CircleShape)
                                        .size(60.dp)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircleOutline,
                                        contentDescription = "Add com.example.signup.Image"
                                    )
                                }
                            }

                        }

                        items(messages) { message ->
                            Row(
                                modifier = Modifier.wrapContentWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                message.status?.let { status ->
                                    Box(
                                        modifier = Modifier.size(70.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = status,
                                            contentDescription = "Status com.example.signup.Image",
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .size(60.dp)
                                                .clickable {
                                                    navController.navigate(
                                                        Screens.StatusDetail.route + "/${
                                                            Uri.encode(status)
                                                        }"
                                                    )
                                                }
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        if (isUploading && message.email == currentUser?.email) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(62.dp),
                                                color = Color.White,
                                                trackColor = Color.Red
                                            )
                                        }
                                    }
                                }


                            }


                        }

                    }
                }


            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0XFF1B202D)
            ),
            actions = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "",
                    tint = Color.White,
                    modifier = Modifier
                        .size(25.dp)
                        .clickable { search = !search }
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "",
                    tint = Color.White,
                    modifier = Modifier
                        .size(25.dp)
                        .clickable { logout = !logout }
                )

                DropdownMenu(expanded = logout, onDismissRequest = { logout = false }) {
                    DropdownMenuItem(text = { Text(text = "Sign Out") }, onClick = {
                        Firebase.auth.signOut()
                        sharedPreferences.edit().remove("userId").apply()
                        navController.navigate(Screens.Home.route)
                    })

                    DropdownMenuItem(text = { Text(text = "Profile") }, onClick = {
                        navController.navigate(Screens.Profile.route)
                    })

                    DropdownMenuItem(text = { Text(text = "Setting") }, onClick = {})
                }
            },
            navigationIcon = {
                Text(
                    text = "Chatter Box",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            })
    }) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0XFF1B202D))
                .padding(top = it.calculateTopPadding()),
            shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp),
            colors = CardDefaults.cardColors(
                Color(0XFF292F3F)
            )
        ) {
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        trackColor = Color.Red, modifier = Modifier.size(60.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 26.dp, start = 5.dp, end = 5.dp)
                        .background(Color(0XFF292F3F)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMessages) {
                        UserList(message = it, navController)
                    }
                }
            }


        }
    }
}

@Composable
fun UserList(message: Message, navController: NavController) {
    var alert by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val encodedImageUrl = Uri.encode(message.profileImageUrl)
                navController.navigate(Screens.ChatDetail.route + "/${message.name}/${message.userId}/$encodedImageUrl")
            }
            .padding(7.dp)
            .background(Color(0XFF292F3F)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DropdownMenu(
            expanded = alert,
            onDismissRequest = { alert = false },
            modifier = Modifier
                .width(210.dp)
                .height(260.dp)
                .background(Color(0XFF1B202D))
        ) {
            AnimatedVisibility(
                visible = alert,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                DropdownMenuItem(
                    text = { Text(text = "") },
                    leadingIcon = {
                        AsyncImage(
                            model = message.profileImageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .width(210.dp)
                                .height(260.dp),
                            contentScale = ContentScale.Crop
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 220.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "",
                                tint = Color.White,
                                modifier = Modifier.clickable {
                                    val encodedImageUrl =
                                        Uri.encode(message.profileImageUrl)
                                    navController.navigate(Screens.ChatDetail.route + "/${message.name}/${message.userId}/$encodedImageUrl")
                                })
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "",
                                tint = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "",
                                tint = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "",
                                tint = Color.White
                            )
                        }
                        Text(text = "${message.name}", color = Color.White)
                    },
                    onClick = {
                        val encodedImageUrl = Uri.encode(message.profileImageUrl)
                        navController.navigate(Screens.ProfileDetail.route + "/$encodedImageUrl")
                    }
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { alert = true }
                .size(45.dp)
                .background(Color.Green), contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = message.profileImageUrl,
                contentDescription = "",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.name.toString(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold, color = Color.White
                )
                Text(text = "7:00", color = Color(0XFFB3B9C9))
            }
            Text(
                text = "No Problem Ok ",
                fontSize = 14.sp,
                color = Color(0XFFB3B9C9), modifier = Modifier.align(Alignment.Start)
            )
        }
    }
}

@Composable
fun UserItem(message: Message, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val encodedImageUrl = Uri.encode(message.profileImageUrl)
                navController.navigate(Screens.ChatDetail.route + "/${message.name}/${message.userId}/$encodedImageUrl")
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(45.dp)
                .background(Color.Green), contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = message.profileImageUrl,
                contentDescription = "",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Text(text = "${message.name}", color = Color.White, fontSize = 13.sp)
    }
}