package com.ait.songrating

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ait.songrating.ui.navigation.MainNavigation
import com.ait.songrating.ui.screen.mainscreen.MainScreen
import com.ait.songrating.ui.theme.SongRatingTheme
import com.ait.songrating.ui.screen.login.LoginScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SongRatingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost()
                }
            }
        }
    }
}

@Composable
@RequiresApi(Build.VERSION_CODES.P)
fun NavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = MainNavigation.LoginScreen.route
) {
    NavHost(
        navController = navController, startDestination = startDestination
    ) {
        composable(MainNavigation.LoginScreen.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(MainNavigation.MainScreen.route)
                })
        }

        composable(MainNavigation.MainScreen.route) {
            MainScreen(
                onNavigateToLogin = { navController.navigate(MainNavigation.LoginScreen.route) })
        }

    }


}