package com.example.zero.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// 0 Grados — Glacial Design System
// Paleta premium inspirada en refrigeración industrial:
//   • Azul glacial profundo como color primario
//   • Cian brillante como acento de energía
//   • Blancos fríos para superficies limpias
// ============================================================================

// region Surface Colors — Blancos fríos y neutros helados
val Surface = Color(0xFFF4F8FC)
val SurfaceDim = Color(0xFFD0D8E4)
val SurfaceBright = Color(0xFFF4F8FC)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFECF2F8)
val SurfaceContainer = Color(0xFFE4EDF6)
val SurfaceContainerHigh = Color(0xFFD8E5F0)
val SurfaceContainerHighest = Color(0xFFCCDCEB)
val SurfaceVariant = Color(0xFFD5E3EF)
val SurfaceTint = Color(0xFF0B4F7A)
// endregion

// region On-Surface Colors
val OnSurface = Color(0xFF0D1B2A)
val OnSurfaceVariant = Color(0xFF3A5068)
val InverseSurface = Color(0xFF0D1B2A)
val InverseOnSurface = Color(0xFFE8F2FB)
// endregion

// region Outline Colors
val Outline = Color(0xFF5E7D99)
val OutlineVariant = Color(0xFFADC4D8)
// endregion

// region Primary Colors — Azul glacial profundo
val Primary = Color(0xFF0B4F7A)           // Azul glacial principal
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFF1A6FA0)  // Azul medio
val OnPrimaryContainer = Color(0xFFF0F8FF)
val InversePrimary = Color(0xFF7DD4FF)
// endregion

// region Secondary Colors — Cian helado
val Secondary = Color(0xFF006B8F)         // Cian oscuro industrial
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFB3E8FF) // Cian muy claro
val OnSecondaryContainer = Color(0xFF003F57)
// endregion

// region Tertiary Colors — Acento cian brillante (para destacar)
val Tertiary = Color(0xFF00A8CC)          // Cian brillante
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFF00C8F0) // Cian energético
val OnTertiaryContainer = Color(0xFFFFFFFF)
// endregion

// region Error Colors
val Error = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF93000A)
// endregion

// region Primary Fixed Colors
val PrimaryFixed = Color(0xFFCDE8FF)
val PrimaryFixedDim = Color(0xFF7DD4FF)
val OnPrimaryFixed = Color(0xFF001E30)
val OnPrimaryFixedVariant = Color(0xFF00497C)
// endregion

// region Secondary Fixed Colors
val SecondaryFixed = Color(0xFFB3E8FF)
val SecondaryFixedDim = Color(0xFF5DCEF7)
val OnSecondaryFixed = Color(0xFF001F2B)
val OnSecondaryFixedVariant = Color(0xFF004F6A)
// endregion

// region Tertiary Fixed Colors
val TertiaryFixed = Color(0xFFB3F0FF)
val TertiaryFixedDim = Color(0xFF4ADCF7)
val OnTertiaryFixed = Color(0xFF001F26)
val OnTertiaryFixedVariant = Color(0xFF005068)
// endregion

// region Background Colors
val Background = Color(0xFFF4F8FC)        // Blanco frío ligeramente azulado
val OnBackground = Color(0xFF0D1B2A)
// endregion

// region Status Indicator Colors — 0 Grados industrial
val StatusOperativo = Color(0xFF10B981)      // Verde esmeralda — Operativo
val StatusReparacion = Color(0xFFF59E0B)     // Ámbar — En Reparación
val StatusCritico = Color(0xFFEF4444)        // Rojo — Crítico
val StatusOperativoText = Color(0xFF047857)
val StatusReparacionText = Color(0xFFB45309)
val StatusCompletedBg = Color(0xFFDCFCE7)
val StatusCompletedText = Color(0xFF166534)
val StatusArchivedBg = Color(0xFFE2E8F0)
val StatusArchivedText = Color(0xFF334155)
// endregion

// region Legacy aliases (para compatibilidad con código existente)
val StatusOperative = StatusOperativo        // ← alias para código existente
val StatusRepair = StatusReparacion
val StatusOperativeText = StatusOperativoText
val StatusRepairText = StatusReparacionText
// endregion