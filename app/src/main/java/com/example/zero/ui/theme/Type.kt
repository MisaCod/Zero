package com.example.zero.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// ============================================================================
// D&S Refrigerantes — Typography
// Fonts: Hanken Grotesk (headlines), Inter (body/labels), JetBrains Mono (code)
// Loaded via Google Fonts downloadable provider
// ============================================================================

private val GoogleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = emptyList() // No certs needed for GMS provider on-device
)

// region Font Families
private val HankenGroteskFont = GoogleFont("Hanken Grotesk")
val HankenGroteskFamily = FontFamily(
    Font(googleFont = HankenGroteskFont, fontProvider = GoogleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = HankenGroteskFont, fontProvider = GoogleFontProvider, weight = FontWeight.Bold),
)

private val InterFont = GoogleFont("Inter")
val InterFamily = FontFamily(
    Font(googleFont = InterFont, fontProvider = GoogleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = InterFont, fontProvider = GoogleFontProvider, weight = FontWeight.SemiBold),
)

private val JetBrainsMonoFont = GoogleFont("JetBrains Mono")
val JetBrainsMonoFamily = FontFamily(
    Font(googleFont = JetBrainsMonoFont, fontProvider = GoogleFontProvider, weight = FontWeight.Normal),
)
// endregion

// ============================================================================
// MD3 Typography — Mapped from 00.md design spec
//
// Design Token         → MD3 Slot
// headline-lg          → headlineLarge
// headline-md          → headlineMedium
// headline-sm          → headlineSmall
// body-lg              → bodyLarge
// body-md              → bodyMedium
// body-sm              → bodySmall
// label-lg             → labelLarge
// label-md             → labelMedium
// code-md              → (custom extension, see below)
// ============================================================================

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = HankenGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.02).em,
    ),
    headlineMedium = TextStyle(
        fontFamily = HankenGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.01).em,
    ),
    headlineSmall = TextStyle(
        fontFamily = HankenGroteskFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.05.em,
    ),
    labelMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.05.em,
    ),
)

/**
 * Custom text style for monospaced code/technical data (serial numbers, IDs).
 * Not part of MD3 Typography slots — use directly via `CodeTextStyle`.
 */
val CodeTextStyle = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
)