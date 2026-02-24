package se.onemanstudio.playaroundwithai.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NeoBrutalistDarkColorScheme =
    darkColorScheme(
        primary = electricBlue,
        onPrimary = offWhite,
        primaryContainer = electricBlueContainer,
        onPrimaryContainer = offWhite,
        secondary = vividPink,
        onSecondary = primaryBlack,
        secondaryContainer = vividPinkContainer,
        onSecondaryContainer = offWhite,
        tertiary = zestyLime,
        onTertiary = primaryBlack,
        tertiaryContainer = zestyLimeContainer,
        onTertiaryContainer = offWhite,
        background = primaryBlack,
        onBackground = offWhite,
        surface = primaryBlack,
        onSurface = offWhite,
        error = errorRed,
        onError = primaryBlack,
        errorContainer = errorRedContainer,
        onErrorContainer = offWhite
    )

private val NeoBrutalistLightColorScheme =
    lightColorScheme(
        primary = electricBlue,
        onPrimary = offWhite,
        primaryContainer = electricBlueContainer,
        onPrimaryContainer = offWhite,
        secondary = vividPink,
        onSecondary = primaryBlack,
        secondaryContainer = vividPinkContainer,
        onSecondaryContainer = offWhite,
        tertiary = zestyLime,
        onTertiary = primaryBlack,
        tertiaryContainer = zestyLimeContainer,
        onTertiaryContainer = offWhite,
        background = offWhite,
        onBackground = primaryBlack,
        surface = offWhite,
        onSurface = primaryBlack,
        error = errorRed,
        onError = offWhite,
        errorContainer = errorRedContainer,
        onErrorContainer = offWhite
    )

@Suppress("FunctionNaming")
@Composable
fun SofaAiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        NeoBrutalistDarkColorScheme
    } else {
        NeoBrutalistLightColorScheme
    }

    // Neo-brutalism uses sharp corners. We will override the shapes here.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes(
            extraSmall = ZeroCornerSize,
            small = ZeroCornerSize,
            medium = ZeroCornerSize,
            large = ZeroCornerSize,
            extraLarge = ZeroCornerSize
        ),
        content = content
    )
}
