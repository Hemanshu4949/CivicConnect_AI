package com.example.civicconnectai.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Define the Color Scheme based on your images
private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    primaryContainer = StatusBlueBg,
    onPrimaryContainer = RoyalBlueDark,

    secondary = AmberYellow, // Your "Submit" button color
    onSecondary = Color.Black, // Black text on Yellow button looks best
    secondaryContainer = StatusOrangeBg,
    onSecondaryContainer = AmberDark,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceWhite,
    onSurface = TextPrimary,

    // Input fields often use a slightly gray surface
    surfaceVariant = SurfaceGray,
    onSurfaceVariant = TextSecondary,

    error = StatusRed,
    onError = Color.White
)

// Define Shapes (16dp rounded corners are consistent in your design)
val CivicShapes = Shapes(
    small = RoundedCornerShape(8.dp),   // Small tags
    medium = RoundedCornerShape(16.dp), // Cards, Input Fields
    large = RoundedCornerShape(24.dp)   // Bottom Sheets, Large Dialogs
)

@Composable
fun CivicConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ but we default to false
    // to keep your specific Blue/Yellow branding consistent.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // We force LightColorScheme mostly because your design is Light Mode only.
        // If you add Dark Mode later, provide a DarkColorScheme here.
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar color matches the background or primary depending on screen
            window.statusBarColor = colorScheme.background.toArgb()
            // Use dark icons on the status bar since background is light
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = CivicShapes, // Applying the 16dp rounded look
        content = content
    )
}