package se.onemanstudio.playaroundwithai.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Dark color scheme with high contrast
private val NeoBrutalistDarkScheme = darkColorScheme(
    primary = OrangeAccent,
    onPrimary = PureBlack,
    secondary = GreenAccent,
    onSecondary = PureBlack,
    background = PureBlack,
    onBackground = PureWhite,
    surface = DarkGray,
    onSurface = PureWhite,
    tertiary = BlueAccent,
    onTertiary = PureBlack,
    error = PinkAccent,
    onError = PureBlack
)

// Light color scheme with high contrast
private val NeoBrutalistLightScheme = lightColorScheme(
    primary = OrangeAccent,
    onPrimary = PureBlack,
    secondary = GreenAccent,
    onSecondary = PureBlack,
    background = LightGray,
    onBackground = PureBlack,
    surface = PureWhite,
    onSurface = PureBlack,
    tertiary = BlueAccent,
    onTertiary = PureBlack,
    error = PinkAccent,
    onError = PureBlack
)

@Composable
fun AIAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is less common in brutalist design, so we default it to false.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
//    val colorScheme = when {
//        dynamicColor -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//        darkTheme -> NeoBrutalistDarkScheme
//        else -> NeoBrutalistLightScheme
//    }
    val context = LocalContext.current
    val colorScheme = dynamicLightColorScheme(context)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes, // Apply our new sharp shapes
        content = content
    )
}