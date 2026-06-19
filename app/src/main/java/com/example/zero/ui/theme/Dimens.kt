package com.example.zero.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ============================================================================
// D&S Refrigerantes — Dimensions & Shapes
// Spacing uses the 8px grid system from the design spec.
// Shapes follow the "Soft (0.25rem)" industrial language.
// ============================================================================

/**
 * Centralized spacing tokens following the 8px grid system.
 *
 * Usage:
 * ```
 * Modifier.padding(Dimens.md)
 * Spacer(modifier = Modifier.height(Dimens.lg))
 * ```
 */
object Dimens {
    /** 8px — base grid unit */
    val base = 8.dp

    /** 4px — extra-small spacing */
    val xs = 4.dp

    /** 8px — small spacing */
    val sm = 8.dp

    /** 16px — medium spacing (standard content padding) */
    val md = 16.dp

    /** 24px — large spacing (section separation) */
    val lg = 24.dp

    /** 32px — extra-large spacing (major section gaps) */
    val xl = 32.dp

    /** 16px — gutter between grid columns */
    val gutter = 16.dp

    /** 16px — horizontal page margin on mobile */
    val marginMobile = 16.dp

    /** 32px — horizontal page margin on tablet */
    val marginTablet = 32.dp

    /** 48dp — minimum touch target per MD3 accessibility guidelines */
    val touchTarget = 48.dp

    // ---- Component-specific dimensions ----

    /** Top app bar height */
    val topBarHeight = 64.dp

    /** Bottom navigation bar height */
    val bottomBarHeight = 80.dp

    /** Standard card icon container size */
    val iconContainerSmall = 40.dp

    /** Large card icon container size */
    val iconContainerLarge = 56.dp
}

// ============================================================================
// Border Radii — mapped from the design spec `rounded` tokens
//
// Token       | CSS rem   | dp
// sm          | 0.125rem  | 2dp
// DEFAULT     | 0.25rem   | 4dp
// md          | 0.375rem  | 6dp
// lg          | 0.5rem    | 8dp
// xl          | 0.75rem   | 12dp
// full        | 9999px    | 50% (CircleShape)
// ============================================================================

object Radii {
    val sm = 2.dp
    val default = 4.dp
    val md = 6.dp
    val lg = 8.dp
    val xl = 12.dp
    // For `full`, use CircleShape or RoundedCornerShape(50)
}

/**
 * MD3 Shapes configuration.
 *
 * - Buttons & Inputs → base 4dp radius (extraSmall)
 * - Cards & Modals → rounded-lg 8dp (medium)
 * - Chips → same 4dp radius
 * - Pill/full-round → large (used for FABs, chips if needed)
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(Radii.sm),       // 2dp
    small = RoundedCornerShape(Radii.default),        // 4dp — buttons, inputs, chips
    medium = RoundedCornerShape(Radii.lg),            // 8dp — cards, modals
    large = RoundedCornerShape(Radii.xl),             // 12dp — large containers, rounded cards
    extraLarge = RoundedCornerShape(28.dp),            // MD3 default extra-large (bottom sheets)
)
