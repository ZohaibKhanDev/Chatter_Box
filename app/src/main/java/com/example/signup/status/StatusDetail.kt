package com.example.signup.status

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.signup.realtimedatabase.RealTimeRepositoryImpl
import com.example.signup.realtimedatabase.RealTimeViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun StatusDetail(
    navController: NavController,
    status: String?,
    onStoryViewed: () -> Unit
) {
    val context = LocalContext.current
    val timestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }
    val myRef = Firebase.database.getReference("User")
    val repository = RealTimeRepositoryImpl(myRef)

    val viewModel= remember {
        RealTimeViewModel(repository)
    }
    fun isStatusExpired(): Boolean {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - timestamp
        val hoursInMillis = 24 * 60 * 60 * 1000
        return elapsedTime >= hoursInMillis
    }

    fun encodeEmail(email: String): String {
        return email.replace(".", ",")
    }

    fun encodePath(path: String): String {
        return path.replace(".", ",")
            .replace("#", "%23")
            .replace("$", "%24")
            .replace("[", "%5B")
            .replace("]", "%5D")
    }

    fun deleteStatus(statusKey: String) {
        val user = Firebase.auth.currentUser
        val currentUserEmail = user?.email

        if (currentUserEmail != null) {
            val encodedEmail = encodeEmail(currentUserEmail)
            val encodedStatusKey = encodePath(statusKey)
            val dbRef = Firebase.database.getReference("User").child(encodedEmail).child(encodedStatusKey)

            dbRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    dbRef.removeValue()
                        .addOnSuccessListener {
                            Log.d("StatusDetail", "Status deleted from database successfully")
                            Toast.makeText(context, "Story deleted successfully", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("StatusDetail", "Error deleting status from database", exception)
                            Toast.makeText(context, "Failed to delete status: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e("StatusDetail", "Status does not exist in database")
                    Toast.makeText(context, "Status does not exist in database", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }.addOnFailureListener { exception ->
                Log.e("StatusDetail", "Error fetching status from database", exception)
                Toast.makeText(context, "Failed to fetch status: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("StatusDetail", "Current user email is null")
            Toast.makeText(context, "Failed to delete status: User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        if (isStatusExpired()) {
            status?.let {
                deleteStatus(it)
            } ?: run {
                Log.e("StatusDetail", "Status is null")
                Toast.makeText(context, "Status is null", Toast.LENGTH_SHORT).show()
            }
            navController.popBackStack()
            return@LaunchedEffect
        }
        progress = 0f
        val animationDuration = 10000L
        val startTime = System.currentTimeMillis()

        while (progress < 1f) {
            val elapsedTime = System.currentTimeMillis() - startTime
            progress = elapsedTime.toFloat() / animationDuration.toFloat()
            delay(16)
        }

        onStoryViewed()
    }

    val animatedProgress by animateFloatAsState(targetValue = progress)

    Scaffold(topBar = {
        TopAppBar(title = {
            Row(
                modifier = Modifier
                    .width(300.dp)
                    .padding(top = 26.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 10) {
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .weight(1f)
                            .background(if (i < animatedProgress * 10) Color.Red else Color.Black)
                            .padding(horizontal = 1.dp)
                    )
                }
            }
        }, actions = {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "",
                modifier = Modifier
                    .clickable { dropdownMenuExpanded = true }
                    .padding(top = 11.dp))

            DropdownMenu(
                expanded = dropdownMenuExpanded,
                onDismissRequest = { dropdownMenuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(text = "Delete") },
                    onClick = { deleteDialog = true })
                DropdownMenuItem(text = { Text(text = "Setting") }, onClick = { })
                DropdownMenuItem(text = { Text(text = "Info") }, onClick = { })
            }
        }, colors = TopAppBarDefaults.topAppBarColors(Color.LightGray.copy(alpha = 0.20f)))
    }) {

        if (deleteDialog) {
            AlertDialog(onDismissRequest = { deleteDialog = !deleteDialog }, confirmButton = {
                Text(
                    text = "Ok", modifier = Modifier.clickable {
                        status?.let {
                            deleteStatus(it)
                        } ?: run {
                            Toast.makeText(context, "Status is null", Toast.LENGTH_SHORT).show()
                        }
                        deleteDialog = false
                    }
                )
            }, dismissButton = {
                Text(text = "Cancel", modifier = Modifier.clickable { deleteDialog = false })
            }, title = {
                Text(text = "Confirm Deletion")
            }, text = {
                Text(text = "Are you sure you want to delete this item? This action cannot be undone.")
            })
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding()),
            contentAlignment = Alignment.TopCenter
        ) {
            AsyncImage(
                model = status,
                contentDescription = "",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { navController.popBackStack() },
                contentScale = ContentScale.Crop
            )
        }
    }
}
