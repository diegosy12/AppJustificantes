package com.example.appjustificantes

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(navController,
                onLoginClick = { email ->
                    navController.navigate("home/$email")
                }
            )
        }

        composable(
            route = "home/{email}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val email = backStackEntry.arguments?.getString("email") ?: ""

            HomeScreen(email = email,navController)
        }
    }
}
