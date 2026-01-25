package se.onemanstudio.playaroundwithai.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NeoBrutalistDarkColorScheme =
    darkColorScheme(
        primary = ElectricBlue,
        secondary = VividPink,
        tertiary = ZestyLime,
        background = PrimaryBlack,
        surface = PrimaryBlack,
        onPrimary = OffWhite,
        onSecondary = PrimaryBlack,
        onTertiary = PrimaryBlack,
        onBackground = OffWhite,
        onSurface = OffWhite,
        error = ErrorRed,
        onError = PrimaryBlack
    )

private val NeoBrutalistLightColorScheme =
    lightColorScheme(
        primary = ElectricBlue,
        secondary = VividPink,
        tertiary = ZestyLime,
        background = OffWhite,
        surface = OffWhite,
        onPrimary = OffWhite,
        onSecondary = PrimaryBlack,
        onTertiary = PrimaryBlack,
        onBackground = PrimaryBlack,
        onSurface = PrimaryBlack,
        error = ErrorRed,
        onError = OffWhite
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
