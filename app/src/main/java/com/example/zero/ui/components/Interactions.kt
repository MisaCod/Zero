package com.example.zero.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

// ============================================================================
// Interactions.kt — Reusable interaction modifiers
// Provides the press-scale animation equivalent to Tailwind's `active:scale-95`.
// ============================================================================

/**
 * Applies a scale-down animation when the element is pressed.
 * Mirrors the `active:scale-95` / `active:scale-98` Tailwind utilities
 * from the HTML mockups.
 *
 * @param interactionSource The interaction source to observe press state from.
 *   Must be the same instance passed to the clickable/button composable.
 * @param targetScale The scale factor when pressed. 0.95f = 5% shrink.
 *
 * Usage:
 * ```
 * val interactionSource = remember { MutableInteractionSource() }
 * Box(
 *     modifier = Modifier
 *         .scaleOnPress(interactionSource)
 *         .clickable(interactionSource = interactionSource, indication = ripple()) { ... }
 * )
 * ```
 */
fun Modifier.scaleOnPress(
    interactionSource: InteractionSource,
    targetScale: Float = 0.96f,
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "scaleOnPress",
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
