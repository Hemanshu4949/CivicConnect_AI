package com.example.civicconnectai.splashScreen


import kotlinx.coroutines.launch
import com.example.civicconnectai.R
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import androidx.core.net.toUri
import androidx.media3.common.Player
import kotlinx.coroutines.async
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.apply
import kotlin.coroutines.resume


@OptIn(UnstableApi::class)
@Composable
fun VideoSplashScreen(
    navController: NavController,
    auth: FirebaseAuth,
    database: FirebaseDatabase
) {
    val context = LocalContext.current

    // 1. SETUP THE ANIMATION STATE
    // Start at scale 1.0 (normal size)
    val scale = remember { Animatable(1.5f) }
    val alpha = remember { Animatable(0f) }

    // 2. TRIGGER THE ZOOM ANIMATION
    LaunchedEffect(Unit) {
        // Animate from 1.0f to 1.5f over 5 seconds (Slow Zoom In)
launch {
    scale.animateTo(
        targetValue = 1.5f,
        animationSpec = tween(
            durationMillis = 6000,
            easing = FastOutLinearInEasing // or FastOutSlowInEasing for a smooth start
        )
    )
}
        launch {
            // Fade in quickly (over 1.5 seconds) so the user sees the video
            alpha.animateTo(
                targetValue = 1.7f,
                animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
            )
        }
    }


    // 1. Setup ExoPlayer (The Video)
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Ensure this file exists at res/raw/splash_intro.mp4
            val videoUri = "android.resource://${context.packageName}/${R.raw.civic_connect_ai_animation}".toUri()
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
        }
    }

    // Clean up player when screen closes
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    // 2. The UI (Video Player)
    Box(modifier = Modifier.fillMaxSize().background(Color.Black).graphicsLayer {
        // Apply the changing scale value to X and Y
        scaleX = scale.value
        scaleY = scale.value
    }) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    // 3. The Logic (Auth Check)
    LaunchedEffect(Unit) {
        // Optional: Wait 3 seconds so the user can see the video
        val timerJob = async {
            delay(5000)
        }
        val authJob = async {
            checkUserDestination(auth, database)
        }
        timerJob.await()
        val destination = authJob.await()
        // Navigate once both are ready
        navController.navigate(destination) {

            // ▼▼▼▼▼ THIS BLOCK REMOVES SPLASH FROM BACK STACK ▼▼▼▼▼
            popUpTo("splash") {
                inclusive = true
            }
        }
    }


}
//val user = auth.currentUser
//if (user != null) {
//    // Check if they have a phone number
//    database.getReference("users").child(user.uid).child("contactNumber").get()
//        .addOnSuccessListener { snapshot ->
//            if (snapshot.exists() && snapshot.value.toString().isNotEmpty()) {
//                navController.navigate("home") {
//                    popUpTo("splash") {
//                        inclusive = true
//                    }
//                }
//            } else {
//                navController.navigate("signup_google") {
//                    popUpTo("splash") {
//                        inclusive = true
//                    }
//                }
//            }
//        }
//        .addOnFailureListener {
//            navController.navigate("login") { popUpTo("splash") { inclusive = true } }
//        }
//} else {
//    navController.navigate("login") { popUpTo("splash") { inclusive = true } }
//}
// This function checks auth and returns the route name ("home", "login", etc.)
suspend fun checkUserDestination(auth: FirebaseAuth, database: FirebaseDatabase): String =
    suspendCancellableCoroutine { continuation ->
        val user = auth.currentUser
        if (user != null) {
            database.getReference("users").child(user.uid).child("contactNumber").get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists() && snapshot.value.toString().isNotEmpty()) {
                        continuation.resume("home")
                    } else {
                        continuation.resume("login")
                    }
                }
                .addOnFailureListener {
                    continuation.resume("login")
                }
        } else {
            continuation.resume("login")
        }
    }