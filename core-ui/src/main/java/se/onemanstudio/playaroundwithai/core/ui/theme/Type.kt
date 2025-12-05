package se.onemanstudio.playaroundwithai.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import se.onemanstudio.playaroundwithai.core.ui.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Poppins is a modern and stylish geometric sans-serif for headings
val displayFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Poppins"),
        fontProvider = provider,
        weight = FontWeight.Bold,
    )
)

// Inter is a highly-readable and clean sans-serif for body text
val bodyFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Inter"),
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