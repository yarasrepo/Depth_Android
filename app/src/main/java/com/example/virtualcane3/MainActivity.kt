package com.example.virtualcane3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.virtualcane3.ui.theme.VirtualCane3Theme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContent {
            VirtualCane3Theme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "register") {
                    composable("register") { RegisterScreen(navController) }
                    composable("login") { LoginScreen(navController) }
                    composable("home") {HomeScreen(navController)}
                    composable("location") {LocationScreen(navController)}
                    composable("profile") { ProfileScreen(navController) }
                    composable("settings") { SettingsScreen(navController) }
                    composable("ViewLocationScreen/{userId}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")
                        ViewLocationScreen(userId = userId ?: "")
                    }

                }
            }
        }
    }
}


