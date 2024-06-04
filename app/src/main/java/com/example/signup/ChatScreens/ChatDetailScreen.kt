package com.example.signup.ChatScreens

import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.signup.MainActivity
import com.example.signup.navigation.Screens
import com.example.signup.realtimedatabase.MainViewModel1
import com.example.signup.realtimedatabase.Repository1
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val PREFS_NAME = "prefs"
private const val PREF_UPLOADED_IMAGE_URL = "uploaded_image_url"

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatDetailScreen(
    navController: NavController,
    name: String?,
    receiverId: String?,
    image: String?,
) {
    var chate by remember { mutableStateOf("") }
    val user = Firebase.auth.currentUser
    val context = LocalContext.current
    val senderId = user?.uid ?: return
    val databaseReference = Firebase.database.reference.child("Messages")
    val repository = Repository1(databaseReference)

    val viewModel = remember { MainViewModel1(repository) }

    val activity = context as MainActivity

    LaunchedEffect(key1 = receiverId) {
        if (receiverId != null) {
            viewModel.loadMessages(senderId, receiverId)
        }
    }

    var alert by remember {
        mutableStateOf(false)
    }

    val messages by viewModel.messages.collectAsState()

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var isUploading by remember {
        mutableStateOf(false)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
                isUploading = true
                uploadImageToFirebase(context, uri) { downloadUrl ->
                    isLoading = false
                    scope.launch {
                        val newChat = Chats(
                            message = chate,
                            currentTimeOrDate = LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            receiverId = receiverId ?: "",
                            senderId = senderId,
                            photoUri = downloadUrl
                        )
                        viewModel.addMessage(newChat)
                        chate = ""
                        delay(1000)
                        isUploading = false
                    }
                }
            }
        }
    )

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                text = "$name",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White, modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable { }
            )
        }, navigationIcon = {
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable { navController.navigateUp() },
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "",
                    modifier = Modifier.clickable {
                    },
                    tint = Color.White
                )
                Box(
                    modifier = Modifier
                        .padding(start = 14.dp)
                        .clip(CircleShape)
                        .size(45.dp)
                        .background(Color.Green), contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = image,
                        contentDescription = "",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }, actions = {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = "",
                tint = Color.White,
                modifier = Modifier.padding(end = 16.dp)
            )
        }, colors = TopAppBarDefaults.topAppBarColors(
            Color(0XFF1B202D)
        )
        )
    }, bottomBar = {
        BottomAppBar(containerColor = Color(0XFF1B202D), contentColor = Color(0XFF3D4354)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = chate,
                    onValueChange = {
                        chate = it
                    },
                    modifier = Modifier
                        .width(300.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(25.dp),
                    placeholder = {
                        Text(
                            text = "Message",
                            color = Color(0XFF9398A7),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0XFF1B202D),
                        unfocusedIndicatorColor = Color(0XFF1B202D),
                        focusedContainerColor = Color(0XFF3D4354),
                        unfocusedContainerColor = Color(0XFF3D4354),
                        focusedTextColor = Color(0XFF9398A7),
                        unfocusedTextColor = Color(0XFF9398A7)
                    ), trailingIcon = {
                        if (chate.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "",
                                tint = Color(0XFF9398A7),
                                modifier = Modifier.clickable {
                                    val newChat = Chats(
                                        message = chate,
                                        currentTimeOrDate = LocalDateTime.now()
                                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                        receiverId = receiverId ?: "",
                                        senderId = senderId,
                                        photoUri = null
                                    )
                                    viewModel.addMessage(newChat)
                                    chate = ""
                                }
                            )
                        }
                    }, leadingIcon = {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    context.navigateToCameraPreview()
                                }
                                .width(33.dp)
                                .height(33.dp)
                                .background(Color(0xFF9398A7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Outlined.CameraAlt, contentDescription = "")
                        }
                    }, singleLine = true
                )
                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .size(45.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                launcher.launch("image/*")
                            }
                            .size(45.dp)
                            .background(Color.White.copy(alpha = 0.50f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Photo, contentDescription = "")
                    }
                }
            }
        }
    }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding(), bottom = it.calculateBottomPadding())
                .background(Color(0XFF1B202D)),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            var previousDate: LocalDate? = null
            itemsIndexed(messages) { index, chat ->
                val messageDateTime = LocalDateTime.parse(
                    chat.currentTimeOrDate,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                val messageDate = messageDateTime.toLocalDate()
                val showDateHeader = messageDate != previousDate

                if (showDateHeader) {
                    previousDate = messageDate
                    Text(
                        text = when (messageDate) {
                            LocalDate.now() -> "Today"
                            LocalDate.now().minusDays(1) -> "Yesterday"
                            else -> messageDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                        },
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                if (chat.senderId == senderId) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Card(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .widthIn(min = 100.dp, max = 150.dp)
                                    .wrapContentHeight(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0XFF7A8194)
                                ),
                                elevation = CardDefaults.cardElevation(2.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    if (chat.photoUri != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Gray)
                                                .clickable {
                                                    navController.navigate(Screens.PicTureDetail.route + "/${Uri.encode(chat.photoUri)}")

                                                }
                                        ) {
                                            SubcomposeAsyncImage(
                                                model = chat.photoUri,
                                                contentDescription = "",
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                            )
                                        }
                                    }
                                    Text(
                                        text = chat.message,
                                        color = Color.White,
                                        modifier = Modifier
                                            .padding(4.dp)
                                    )
                                }
                            }

                            Text(
                                text = messageDateTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .padding(5.dp)
                            )
                        }
                    }
                } else {

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Card(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .widthIn(min = 100.dp, max = 150.dp)
                                    .wrapContentHeight(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0XFF373E4E)
                                ),
                                elevation = CardDefaults.cardElevation(2.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    if (chat.photoUri != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Gray)
                                                .clickable {
                                                    Screens.PicTureDetail.route + "/${
                                                        Uri.encode(chat.photoUri)
                                                    }"

                                                }
                                        ) {
                                            SubcomposeAsyncImage(
                                                model = chat.photoUri,
                                                contentDescription = "",
                                                modifier = Modifier
                                                    .fillMaxSize(),
                                                contentScale = ContentScale.Crop,
                                            )
                                        }
                                    }
                                    Text(
                                        text = chat.message,
                                        color = Color.Gray,
                                        modifier = Modifier
                                            .padding(4.dp)
                                    )
                                }
                            }

                            Text(
                                text = messageDateTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .padding(5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun uploadImageToFirebase(context: Context, uri: Uri, onUploadSuccess: (String) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("images/${uri.lastPathSegment}")
    val uploadTask = imageRef.putFile(uri)

    uploadTask.continueWithTask { task ->
        if (!task.isSuccessful) {
            task.exception?.let { throw it }
        }
        imageRef.downloadUrl
    }.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val downloadUrl = task.result.toString()
            onUploadSuccess(downloadUrl)
        } else {
            Toast.makeText(
                context,
                "Failed to upload image: ${task.exception?.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

data class Chats(
    val message: String = "",
    val currentTimeOrDate: String = "",
    val receiverId: String = "",
    val senderId: String? = "",
    var photoUri: String? = ""
)


@RequiresApi(Build.VERSION_CODES.O)
fun getFormattedDateTime(dateTime: LocalDateTime): String {
    val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return dateTime.format(timeFormatter)
}

fun MainActivity.navigateToCameraPreview() {
    navigateToCameraPreview = true
}

fun saveImageUrlToPrefs1(context: Context, url: String) {
    val prefs = context.getSharedPreferences(
        com.example.signup.loginscreens.PREFS_NAME,
        Context.MODE_PRIVATE
    )
    prefs.edit().putString(com.example.signup.loginscreens.PREF_UPLOADED_IMAGE_URL, url).apply()
}

fun getImageUrlFromPrefs1(context: Context): String? {
    val prefs = context.getSharedPreferences(
        com.example.signup.loginscreens.PREFS_NAME,
        Context.MODE_PRIVATE
    )
    return prefs.getString(com.example.signup.loginscreens.PREF_UPLOADED_IMAGE_URL, null)
}
