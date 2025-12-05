package se.onemanstudio.playaroundwithai.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val IbmPlexMono = FontFamily(
    androidx.compose.ui.text.font.Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
    androidx.compose.ui.text.font.Font(R.font.ibm_plex_mono_medium, FontWeight.Medium),
    androidx.compose.ui.text.font.Font(R.font.ibm_plex_mono_semibold, FontWeight.SemiBold),
    androidx.compose.ui.text.font.Font(R.font.ibm_plex_mono_bold, FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = IbmPlexMono,
        fontWeight = FontWeight.Bold,
        fontSize = 52.sp,
        lineHeight = 60.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle( // For screen titles
        fontFamily = IbmPlexMono,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle( // For card titles
        fontFamily = IbmPlexMono,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle( // Default text
        fontFamily = IbmPlexMono,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle( // For Buttons
        fontFamily = IbmPlexMono,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = IbmPlexMono,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,letterSpacing = 0.5.sp
    )
    /* Define other styles as needed */
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
