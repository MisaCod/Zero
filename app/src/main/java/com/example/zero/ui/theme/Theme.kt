package com.example.zero.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================================================================
// D&S Refrigerantes — Theme
// Light-only by default (per the design spec's high-contrast white background).
// Dark scheme is provided as a reasonable inverse mapping for future use.
// Dynamic color is DISABLED to enforce the branded industrial palette.
// ============================================================================

private val LightColorScheme = lightColorScheme(
    // Primary
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    inversePrimary = InversePrimary,

    // Secondary
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    // Tertiary
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,

    // Error
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,

    // Background & Surface
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = SurfaceTint,

    // Surface Containers (MD3 tonal elevation)
    surfaceDim = SurfaceDim,
    surfaceBright = SurfaceBright,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,

    // Inverse
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,

    // Outline
    outline = Outline,
    outlineVariant = OutlineVariant,
)

private val DarkColorScheme = darkColorScheme(
    // Primary — inverse mapping
    primary = PrimaryFixedDim,                   // #9ECAFF
    onPrimary = OnPrimaryFixed,                  // #001D36
    primaryContainer = OnPrimaryFixedVariant,     // #00497C
    onPrimaryContainer = PrimaryFixed,            // #D1E4FF
    inversePrimary = Primary,                    // #1E5F97

    // Secondary
    secondary = SecondaryFixedDim,               // #B4C8E3
    onSecondary = OnSecondaryFixed,              // #061D31
    secondaryContainer = OnSecondaryFixedVariant, // #35485E
    onSecondaryContainer = SecondaryFixed,        // #D0E4FF

    // Tertiary
    tertiary = TertiaryFixedDim,                 // #F5BD5A
    onTertiary = OnTertiaryFixed,                // #271900
    tertiaryContainer = OnTertiaryFixedVariant,   // #5F4100
    onTertiaryContainer = TertiaryFixed,          // #FFDEAB

    // Error
    error = ErrorContainer,                       // #FFDAD6
    onError = OnErrorContainer,                  // #93000A
    errorContainer = Error,                      // #BA1A1A
    onErrorContainer = ErrorContainer,           // #FFDAD6

    // Background & Surface — dark equivalents
    background = InverseSurface,                 // #2E3035
    onBackground = InverseOnSurface,             // #F0F0F6
    surface = InverseSurface,                    // #2E3035
    onSurface = InverseOnSurface,                // #F0F0F6
    surfaceVariant = OnSurfaceVariant,           // #414750
    onSurfaceVariant = SurfaceVariant,           // #E1E2E8
    surfaceTint = PrimaryFixedDim,               // #9ECAFF

    // Inverse
    inverseSurface = Surface,                    // #F8F9FF
    inverseOnSurface = OnSurface,                // #191C20

    // Outline
    outline = OutlineVariant,                    // #C1C7D1
    outlineVariant = Outline,                    // #727781
)

/**
 * TechFlow / D&S Refrigerantes application theme.
 *
 * @param darkTheme Whether to use the dark color scheme.
 *                  Default: follows system setting.
 * @param content   The composable content to theme.
 */
@Composable
fun TechFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Configure status bar icon appearance (light/dark) to match the theme.
    // The Activity should call enableEdgeToEdge() for transparent status bar.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}