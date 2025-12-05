package se.onemanstudio.playaroundwithai.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Dark Mode - High contrast with Yellow accents on Black/White
private val BrutalistDarkScheme = darkColorScheme(
    primary = PrimaryYellow,      // Yellow for key elements and accents
    onPrimary = PrimaryBlack,     // Black text on yellow
    secondary = AccentRed,        // Secondary accent
    onSecondary = PrimaryWhite,
    background = PrimaryBlack,    // Dark background for screens
    onBackground = PrimaryWhite,  // White text on dark background
    surface = PrimaryBlack,       // Surface elements like cards, dialogs
    onSurface = PrimaryWhite,     // White text on surface
    error = AccentRed,
    onError = PrimaryWhite,
    outline = PrimaryYellow       // Crucial for outlining components in yellow
)

// Light Mode - High contrast with Yellow accents on White/Black
private val BrutalistLightScheme = lightColorScheme(
    primary = PrimaryYellow,      // Yellow for key elements and accents
    onPrimary = PrimaryBlack,     // Black text on yellow
    secondary = AccentRed,        // Secondary accent
    onSecondary = PrimaryWhite,
    background = PrimaryWhite,    // White background for screens
    onBackground = PrimaryBlack,  // Black text on white background
    surface = PrimaryWhite,       // Surface elements like cards, dialogs
    onSurface = PrimaryBlack,     // Black text on surface
    error = AccentRed,
    onError = PrimaryWhite,
    outline = PrimaryBlack        // Crucial for outlining components in black
)

@Composable
fun AIAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Brutalism generally avoids dynamic colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> BrutalistDarkScheme
        else -> BrutalistLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
