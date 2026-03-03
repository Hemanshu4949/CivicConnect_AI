package com.example.civicconnectai

import CivicIssue
import IssueViewModel
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.civicconnectai.authentication.LoginScreen
import com.example.civicconnectai.authentication.SignUpScreen
import com.example.civicconnectai.bottomNavScreens.IssueDetailScreen
import com.example.civicconnectai.bottomNavScreens.MapScreen
import com.example.civicconnectai.addissue.ReportIssueScreen
import com.example.civicconnectai.splashScreen.VideoSplashScreen
import com.example.civicconnectai.ui.theme.CivicConnectTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {


//        val splashScreen = installSplashScreen()

//  HANDLE SYSTEM SPLASH (The "Handshake")
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

// transition function
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->

            // Create a fade-out effect on the entire splash view
            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenViewProvider.view,
                View.ALPHA,
                1f,
                0f
            )

            fadeOut.duration = 900L // Duration in milliseconds (1.5 seconds)
            fadeOut.interpolator = AnticipateInterpolator() // Smooth acceleration

            // CRITICAL: You must remove the splash view when the animation is done!
            fadeOut.doOnEnd {
                splashScreenViewProvider.remove()
            }

            // Start the animation
            fadeOut.start()
        }


//        var isSplashVisible = true
//
//        // 2. Tell the Splash Screen to stay ON while the flag is true
//        splashScreen.setKeepOnScreenCondition {
//            isSplashVisible
//        }
//
//        // 3. Start a timer to turn off the flag after X milliseconds
//        // (This runs on a background thread so it doesn't freeze the app)
//        lifecycleScope.launch {
//            delay(5000) // Wait for 3 seconds (3000 ms)
//            isSplashVisible = false
//        }


        setContent {
            CivicConnectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //  Get the current user status
                    val auth = FirebaseAuth.getInstance()

                    // --- 1. THE SHARED STATE --- this will contain the data fetched from firebase and will be used through out the app
                    var masterIssuesList by remember { mutableStateOf<List<CivicIssue>>(emptyList()) }
                    var isLoading by remember { mutableStateOf(true) }


                    // --- 2. THE FIREBASE FETCH (Shared by all screens) ---
                    LaunchedEffect(Unit) {
                        val databaseRef = FirebaseDatabase.getInstance().getReference("issues")
                        databaseRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val tempIssues = mutableListOf<CivicIssue>()
                                for (childSnapshot in snapshot.children) {
                                    childSnapshot.getValue(CivicIssue::class.java)?.let { tempIssues.add(it) }
                                }
                                masterIssuesList = tempIssues.sortedByDescending { it.timestamp }
                                isLoading = false
                            }
                            override fun onCancelled(error: DatabaseError) {
                                isLoading = false
                            }
                        })
                    }


                    // get the required page as per the logged in status
//                    var startRoute by remember { mutableStateOf("loading") }
//
//                    LaunchedEffect(Unit) {
//                        val user = auth.currentUser
//                        if (user != null) {
                            // User is logged in, but do they have a phone number?
//                            FirebaseDatabase.getInstance().getReference("users")
//                                .child(user.uid).child("contactNumber").get()
//                                .addOnSuccessListener { snapshot ->
//                                    startRoute = if (snapshot.exists() && snapshot.value != "") {
//                                        "home" // All good
//                                    } else {
//                                        // Logged in but no phone? Send them to finish signup
//                                        "signup/SignInWithGoogle"
//                                    }
//                                }
//                                .addOnFailureListener {
//                                    startRoute = "login" // Error? Safe fallback
//                                }
//                        } else {
//                            startRoute = "login"
//                        }
//                    }

                    // 1. Create the "Stage Manager" (Controller)
                    val navController = rememberNavController()
                    val sharedViewModel: IssueViewModel = viewModel()

                    // 2. Define the "Stage" (NavHost)
                    // startDestination tells it which screen to show first
//                    if (startRoute == "loading") {
//                        // Show a simple logo or spinner while checking DB
//                        Box(modifier = Modifier.fillMaxSize()) {
//                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//                        }
//                    } else {
                        NavHost(
                            navController = navController, startDestination = "splash",

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

                           //  Your Video & Logic
                            composable("splash" ,
                                // 2. EXIT (Back): Slide DOWN to the bottom (Vertical Slide Out)
                                popExitTransition = {
                                    slideOutVertically(
                                        targetOffsetY = { fullHeight -> fullHeight }, // Exits to the bottom
                                        animationSpec = tween(500)
                                    )
                                }) {
                                VideoSplashScreen(navController, auth, FirebaseDatabase.getInstance())
                            }

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
                                    onGoogleSignInClick = {
                                        navController.navigate("home")
                                        {
                                            popUpTo(navController.graph.startDestinationId)
                                            {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    onForgotPasswordClick = { /* Handle Forgot Password */ },
                                )
                            }

                            // SCREEN 2: Sign Up
                            composable(route = "signup")
                            {


                                SignUpScreen(
                                    onRegisterClick = { option ->

                                        Log.e("Routes", option.toString())
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
                            composable(
                                "home",

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
                            composable("map") {

                                MapScreen(
                                    reportIssueScreen = {
                                        navController.navigate("report_issue")
                                    },
                                    viewModel = sharedViewModel,
                                    onIssueClick = { issueId ->
                                        // Navigate to Report Issue (Editable = False)
                                        // In a real app, you would pass the ID too, e.g., "report_issue/false/$issueId"
                                        navController.navigate("issue_detail/$issueId")} ,
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
                                arguments = listOf(navArgument("issueId") {
                                    type = NavType.StringType
                                })
                            ) { backStackEntry ->
                                // You can retrieve the ID if needed:
                                val issueId = backStackEntry.arguments?.getString("issueId")

                                // INSTANTLY find the issue from the list we already have
                                val selectedIssue = masterIssuesList.find { it.issueId == issueId }

                                IssueDetailScreen(
                                    issue = selectedIssue,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                        }
                }
            }
        }
    }
}