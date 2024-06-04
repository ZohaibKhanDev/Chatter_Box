package com.example.signup.navigation

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.signup.ChatScreens.ChatDetailScreen
import com.example.signup.ChatScreens.ChatScreen
import com.example.signup.loginscreens.HomeScreen
import com.example.signup.loginscreens.LoginScreen
import com.example.signup.loginscreens.SignUp
import com.example.signup.picDetail.PictureDetail
import com.example.signup.profile.ProfileDetail
import com.example.signup.profile.ProfileScreen
import com.example.signup.status.StatusDetail

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation() {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("SignUp", Context.MODE_PRIVATE)
    val sharedPreferencesId = sharedPreferences.getString("userId", null)
    val navController = rememberNavController()
    val destination = remember {
        if (sharedPreferencesId != null) {
            Screens.Chat.route
        } else {
            Screens.Home.route
        }
    }

    NavHost(navController = navController, startDestination = destination) {
        composable(Screens.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screens.SignUp.route) {
            SignUp(navController = navController)
        }
        composable(Screens.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screens.Chat.route) {
            ChatScreen(navController = navController)
        }
        composable(Screens.Profile.route) {
            ProfileScreen(navController = navController)
        }


        composable(
            Screens.PicTureDetail.route + "/{pic}", // Make sure route name is correct
            arguments = listOf(
                navArgument("pic") { type = NavType.StringType }
            )
        ) {
            val pic = it.arguments?.getString("pic")
            PictureDetail(navController, pic)
        }

        composable(
            route = Screens.ChatDetail.route + "/{name}/{receiverId}/{image}",
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("receiverId") { type = NavType.StringType },
                navArgument("image") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name")
            val receiverId = backStackEntry.arguments?.getString("receiverId")
            val image = backStackEntry.arguments?.getString("image")
            ChatDetailScreen(navController, name, receiverId, image)
        }
        composable(Screens.ProfileDetail.route + "/{profileId}",
            arguments = listOf(
                navArgument("profileId") {
                    type = NavType.StringType
                }
            )) {
            val profileId = it.arguments?.getString("profileId")
            ProfileDetail(navController = navController, profileId)
        }
        composable(Screens.StatusDetail.route + "/{status}") { navBackStackEntry ->
            val status = navBackStackEntry.arguments?.getString("status")
            StatusDetail(navController = navController, status = status) {
                navController.popBackStack()
            }
        }



    }
}

sealed class Screens(val route: String) {
    object Home : Screens("Home")
    object SignUp : Screens("SignUp")
    object Login : Screens("Login")
    object Chat : Screens("Chat")
    object Profile : Screens("Profile")
    object ProfileDetail : Screens("ProfileDetail")
    object ChatDetail : Screens("ChatDetail")
    object StatusDetail : Screens("StatusDetail")
    object PicTureDetail : Screens("PicDetail")
}
