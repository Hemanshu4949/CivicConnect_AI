package com.example.civicconnectai

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.civicconnectai.authentication.LoginScreen
import com.example.civicconnectai.authentication.SignUpScreen
import com.example.civicconnectai.bottomNavScreens.IssueDetailScreen
import com.example.civicconnectai.bottomNavScreens.MapScreen
import com.example.civicconnectai.bottomNavScreens.ReportIssueScreen
import com.example.civicconnectai.ui.theme.CivicConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CivicConnectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 1. Create the "Stage Manager" (Controller)
                    val navController = rememberNavController()

                    // 2. Define the "Stage" (NavHost)
                    // startDestination tells it which screen to show first
                    NavHost(
                        navController = navController, startDestination = "login",

                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(400)
                            )
                        },
                        // Global Exit Transition (Slide out to Left)
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(400)
                            )
                        },
                        // Global Back Enter (Slide in from Left)
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(400)
                            )
                        },
                        // Global Back Exit (Slide out to Right)
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(400)
                            )
                        }
                    )
                    {

                        // SCREEN 1: Login
                        composable("login") {
                            LoginScreen(
                                onLoginClick = {
                                    // Navigate to Home (we will build Home later)
                                    navController.navigate("home")
                                    {
                                        popUpTo(navController.graph.startDestinationId)
                                        {
                                            inclusive = true
                                        }
                                    }
                                },
                                onSignUpClick = {
                                    // Navigate to Sign Up Screen
                                    navController.navigate("signup")
                                    {
                                        popUpTo(navController.graph.startDestinationId)
                                        {
                                            inclusive = true
                                        }
                                    }
                                },
                                onGoogleSignInClick = { /* Handle Google Auth */ },
                                onForgotPasswordClick = { /* Handle Forgot Password */ }
                            )
                        }

                        // SCREEN 2: Sign Up
                        composable(route = "signup")
                        {



                            SignUpScreen(
                                onRegisterClick = { option ->

                                        Log.e("Routes" , option.toString())
                                        when (option) {
                                            "SignInWithGoogle" -> {
                                                navController.navigate("home")
                                                {
                                                    popUpTo(navController.graph.startDestinationId)
                                                    {
                                                        inclusive = true
                                                    }
                                                }

                                            }

                                            "registration" -> {
                                                navController.navigate("login") {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        inclusive = true
                                                    }
                                                }
                                            }

                                            else -> {
                                                navController.popBackStack()
                                            }
                                        }
                                                  },

                                onLoginClick = {
                                    // Go back to Login
                                    navController.popBackStack()
                                }

                            )
                        }

                        // 3. Home Screen
                        composable("home",

                            enterTransition = {
                                slideInVertically(
                                    initialOffsetY = { fullHeight -> fullHeight }, // Starts below the screen
                                    animationSpec = tween(500)
                                )
                            },

                            // 2. EXIT (Back): Slide DOWN to the bottom (Vertical Slide Out)
                            popExitTransition = {
                                slideOutVertically(
                                    targetOffsetY = { fullHeight -> fullHeight }, // Exits to the bottom
                                    animationSpec = tween(500)
                                )
                            }

                        ) {
                            MainScreen(
                                // --- THIS MAKES THE FAB WORK ---
                                reportIssueScreen = {
                                    // Navigate to Report Issue
                                    navController.navigate("report_issue")
                                },
                                // --- THIS MAKES THE LIST ITEMS WORK ---
                                onIssueClick = { issueId ->
                                    // Navigate to Report Issue (Editable = False)
                                    // In a real app, you would pass the ID too, e.g., "report_issue/false/$issueId"
                                    navController.navigate("issue_detail/$issueId")
                                }
                            )
                        }
                        composable ("map"){

                            MapScreen(
                                reportIssueScreen = {
                                    navController.navigate("report_issue")
                                }
                            )
                        }

                        // 4. Report Issue Screen (Handles both New & View modes)
                        // We define a route that accepts a parameter: "report_issue/{isEditable}"
//                        composable("report_issue/{isEditable}") { backStackEntry ->
//
//                            // Get the "true" or "false" string passed from Home
//                            val isEditableString =
//                                backStackEntry.arguments?.getString("isEditable") ?: "true"
//                            val isEditable = isEditableString == "true"
//
//                            ReportIssueScreen(
//                                isEditable = isEditable,
//                                // If view mode, you can pass hardcoded data here or fetch from DB based on ID
//                                existingTitle = if (isEditable) "" else "Pothole on 5th Ave",
//                                existingDescription = if (isEditable) "" else "Large pothole causing traffic issues.",
//                                existingLocation = if (isEditable) "123 Main St" else "Westside Avenue, NY",
//                                onBackClick = { navController.popBackStack() },
//                                onSubmitClick = {
//                                    // Handle logic, then go back
//                                    navController.popBackStack()
//                                }
//                            )
//                        }
                        // 4. Report Issue Screen (CREATE ONLY)
                        composable("report_issue") {
                            ReportIssueScreen(
                                onBackClick = { navController.popBackStack() },
                                onSubmitClick = { navController.popBackStack() }
                            )
                        }

                        // 5. Issue Detail Screen (VIEW ONLY)
                        composable(
                            route = "issue_detail/{issueId}",
                            arguments = listOf(navArgument("issueId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            // You can retrieve the ID if needed:
                             val issueId = backStackEntry.arguments?.getString("issueId")

                            IssueDetailScreen(
                                issueId = issueId,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}