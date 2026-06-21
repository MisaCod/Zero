package com.example.zero.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================================================================
// 0 Grados — Dark Glacial Theme
// Oscuro por defecto para maximizar el impacto visual de la paleta glacial.
// Los azules brillantes y cianes destacan sobre el fondo oscuro profundo.
// ============================================================================

// ── Paleta de colores oscuros personalizados ──────────────────────────────
private val GlacialDarkBg = Color(0xFF050E17)          // Casi negro azulado
private val GlacialDarkSurface = Color(0xFF081624)      // Azul noche
private val GlacialDarkSurfaceHigh = Color(0xFF0D2035)  // Superficie elevada
private val GlacialDarkSurfaceHighest = Color(0xFF122640)
private val GlacialCyan = Color(0xFF00C8F0)            // Cian brillante (acento)
private val GlacialBlue = Color(0xFF1A8FBF)            // Azul glacial principal
private val GlacialBlueDim = Color(0xFF0B6A9A)         // Azul oscuro
private val TextPrimary = Color(0xFFF0F8FF)            // Blanco frío
private val TextSecondary = Color(0xFF7BA9C4)          // Azul grisáceo

private val DarkGlacialColorScheme = darkColorScheme(
    // Primary — azul glacial vibrante
    primary = GlacialBlue,
    onPrimary = Color.White,
    primaryContainer = GlacialBlueDim,
    onPrimaryContainer = Color(0xFFCDE8FF),
    inversePrimary = Primary,

    // Secondary — cian helado
    secondary = GlacialCyan,
    onSecondary = Color(0xFF001F28),
    secondaryContainer = Color(0xFF003B4D),
    onSecondaryContainer = Color(0xFF97EDFF),

    // Tertiary — verde esmeralda (estados OK)
    tertiary = Color(0xFF10B981),
    onTertiary = Color(0xFF003822),
    tertiaryContainer = Color(0xFF005233),
    onTertiaryContainer = Color(0xFF85F5C4),

    // Error
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // Background & Surface — oscuro glacial
    background = GlacialDarkBg,
    onBackground = TextPrimary,
    surface = GlacialDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = GlacialDarkSurfaceHigh,
    onSurfaceVariant = TextSecondary,
    surfaceTint = GlacialBlue,

    // Surface containers (tonal elevation)
    surfaceDim = Color(0xFF030C14),
    surfaceBright = GlacialDarkSurfaceHighest,
    surfaceContainerLowest = Color(0xFF030B12),
    surfaceContainerLow = GlacialDarkSurface,
    surfaceContainer = GlacialDarkSurfaceHigh,
    surfaceContainerHigh = GlacialDarkSurfaceHighest,
    surfaceContainerHighest = Color(0xFF193252),

    // Inverse
    inverseSurface = Color(0xFFD6E4F0),
    inverseOnSurface = Color(0xFF0D2035),

    // Outline
    outline = Color(0xFF2A4F6D),
    outlineVariant = Color(0xFF1A3550),
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    inversePrimary = InversePrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = SurfaceTint,
    surfaceDim = SurfaceDim,
    surfaceBright = SurfaceBright,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    outline = Outline,
    outlineVariant = OutlineVariant,
)

/**
 * Tema principal de 0 Grados.
 * Usa modo OSCURO por defecto — el glacial profundo hace que los colores
 * cian y azul brillen y generen un impacto visual premium.
 */
@Composable
fun TechFlowTheme(
    darkTheme: Boolean = true, // ← SIEMPRE oscuro por defecto
    content: @Composable () -> Unit,
) {
    // Siempre dark glacial en esta app
    val colorScheme = DarkGlacialColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar con iconos claros (fondo oscuro)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}