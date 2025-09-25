package se.onemanstudio.playaroundwithai.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import se.onemanstudio.playaroundwithai.R

// Define the font provider (remains the same)
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Use Archivo Black for strong, impactful display text
val displayFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Archivo Black"),
        fontProvider = provider,
    )
)

// Use Roboto Mono for a clean, functional, and slightly techy body text
val bodyFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Roboto Mono"),
        fontProvider = provider,
    )
)

// Create the final Typography object
val AppTypography = Typography(
    displayLarge = Typography().displayLarge.copy(fontFamily = displayFontFamily),
    displayMedium = Typography().displayMedium.copy(fontFamily = displayFontFamily),
    displaySmall = Typography().displaySmall.copy(fontFamily = displayFontFamily),
    headlineLarge = Typography().headlineLarge.copy(fontFamily = displayFontFamily),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = displayFontFamily),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = displayFontFamily),
    titleLarge = Typography().titleLarge.copy(fontFamily = displayFontFamily),
    titleMedium = Typography().titleMedium.copy(fontFamily = displayFontFamily),
    titleSmall = Typography().titleSmall.copy(fontFamily = displayFontFamily),
    bodyLarge = Typography().bodyLarge.copy(fontFamily = bodyFontFamily),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = bodyFontFamily),
    bodySmall = Typography().bodySmall.copy(fontFamily = bodyFontFamily),
    labelLarge = Typography().labelLarge.copy(fontFamily = bodyFontFamily),
    labelMedium = Typography().labelMedium.copy(fontFamily = bodyFontFamily),
    labelSmall = Typography().labelSmall.copy(fontFamily = bodyFontFamily),
)