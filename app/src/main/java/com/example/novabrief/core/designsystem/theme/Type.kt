package com.example.novabrief.core.designsystem.theme

import androidx.compose.material3.Typography as MaterialTypography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.unit.sp
import com.example.novabrief.R

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val displayFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Space Grotesk"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Bold
    ),
    Font(
        googleFont = GoogleFont("Space Grotesk"),
        fontProvider = googleFontProvider,
        weight = FontWeight.ExtraBold
    )
)

private val bodyFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Plus Jakarta Sans"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Normal
    ),
    Font(
        googleFont = GoogleFont("Plus Jakarta Sans"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Medium
    ),
    Font(
        googleFont = GoogleFont("Plus Jakarta Sans"),
        fontProvider = googleFontProvider,
        weight = FontWeight.SemiBold
    ),
    Font(
        googleFont = GoogleFont("Plus Jakarta Sans"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Bold
    )
)

val Typography = MaterialTypography().run {
    copy(
        headlineLarge = headlineLarge.copy(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 38.sp,
            lineHeight = 44.sp
        ),
        headlineMedium = headlineMedium.copy(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 32.sp
        ),
        titleLarge = titleLarge.copy(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp
        ),
        bodyLarge = bodyLarge.copy(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 28.sp
        ),
        bodyMedium = bodyMedium.copy(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        labelLarge = labelLarge.copy(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 22.sp
        ),
        labelMedium = labelMedium.copy(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Medium
        ),
        labelSmall = labelSmall.copy(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Medium
        )
    )
}
