package com.example.signup.profile

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.signup.dataclasses.Message
import com.example.signup.signuprepo.MainViewModel
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.koin.compose.koinInject
import java.util.AbstractMap

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProfileDetail(navController: NavController, profileId: String?) {
    val viewModel: MainViewModel = koinInject()
    var isUploading by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(listOf<Message>()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentUser by remember { mutableStateOf<Message?>(null) }
    var loading by remember { mutableStateOf(true) }
    val context = LocalContext.current

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
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val maxOffset = 100.dp

    val maxOffsetPx = maxOffset.value

    val anchors = mapOf(
        0f to 0,
        maxOffsetPx to 1
    )


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (profileId != null && currentUser != null) {
            AsyncImage(
                model = profileId,
                contentDescription = "",
                modifier = Modifier.run {
                    fillMaxSize()
                        .swipeable(
                            state = swipeableState,
                            anchors = anchors,
                            orientation = Orientation.Horizontal
                        )
                },
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Top
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            deleteProfilePicture(context, currentUser!!) {
                                currentUser = currentUser?.copy(profileImageUrl = null)
                            }
                        }
                        .size(50.dp)
                        .background(Color.Gray.copy(alpha = 0.50f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "")
                }
            }
        } else {
            Text(text = "No Profile")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { navController.popBackStack() }
                    .size(50.dp)
                    .background(Color.Gray.copy(alpha = 0.50f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "")
            }
        }
    }
}

fun deleteProfilePicture(context: Context, user: Message, onComplete: () -> Unit) {
    val dbRef = Firebase.database.getReference("User").child(user.userId.toString())
    val storageRef = Firebase.storage.getReferenceFromUrl(user.profileImageUrl.toString())

    storageRef.delete().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("SignUp", "Profile picture deleted from storage")
            dbRef.child("profilePictureUrl").removeValue().addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Profile picture URL removed from database",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("SignUp", "Profile picture URL removed from database")
                    onComplete()
                } else {
                    Toast.makeText(
                        context,
                        "Failed to remove profile picture URL",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("SignUp", "Failed to remove profile picture URL", dbTask.exception)
                }
            }
        } else {
            Toast.makeText(
                context,
                "Failed to delete profile picture from storage",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("SignUp", "Failed to delete profile picture from storage", task.exception)
        }
    }
}

