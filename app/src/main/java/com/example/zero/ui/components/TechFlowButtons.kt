package com.example.zero.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zero.ui.theme.Dimens
import com.example.zero.ui.theme.TechFlowTheme

// ============================================================================
// TechFlowButtons
// Reusable button composables matching the industrial design system.
//
// From the design spec:
//   - Primary: Solid Steel Blue fill, white text, min 48dp height
//   - Secondary: Slate Grey outline, no fill, min 48dp height
//   - Both use label-lg typography and rounded-lg (8dp) shape
//
// HTML patterns:
//   Primary  → bg-primary text-on-primary font-label-lg rounded-lg h-touch-target
//   Secondary→ border border-outline text-on-surface-variant font-label-md rounded-lg h-touch-target
// ============================================================================

/**
 * Primary filled button — Steel Blue background with white text.
 *
 * @param text The button label.
 * @param onClick Callback when the button is clicked.
 * @param modifier Modifier for the button.
 * @param enabled Whether the button is enabled.
 * @param leadingIcon Optional icon displayed before the text.
 * @param trailingIcon Optional icon displayed after the text.
 */
@Composable
fun TechFlowPrimaryButton(
    text: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Button(
        onClick = onClick,
        modifier = modifier
            .scaleOnPress(interactionSource)
            .defaultMinSize(minHeight = Dimens.touchTarget),
        enabled = enabled,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.medium, // 8dp rounded-lg
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        ),
        contentPadding = PaddingValues(horizontal = Dimens.lg, vertical = 12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 1.dp,
            pressedElevation = 0.dp,
        ),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(Dimens.sm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(Dimens.sm))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Secondary outlined button — Slate Grey border with no fill.
 *
 * @param text The button label.
 * @param onClick Callback when the button is clicked.
 * @param modifier Modifier for the button.
 * @param enabled Whether the button is enabled.
 * @param leadingIcon Optional icon displayed before the text.
 * @param trailingIcon Optional icon displayed after the text.
 */
@Composable
fun TechFlowSecondaryButton(
    text: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .scaleOnPress(interactionSource)
            .defaultMinSize(minHeight = Dimens.touchTarget),
        enabled = enabled,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.medium, // 8dp rounded-lg
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            },
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.secondary,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        ),
        contentPadding = PaddingValues(horizontal = Dimens.lg, vertical = 12.dp),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(Dimens.sm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(Dimens.sm))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// region Previews
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun TechFlowButtonsPreview() {
    TechFlowTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            // Full-width primary button with trailing icon (login style)
            TechFlowPrimaryButton(
                text = "Iniciar Sesión",
                trailingIcon = Icons.AutoMirrored.Outlined.Login,
                modifier = Modifier.fillMaxWidth(),
            )

            // Full-width secondary button (request access style)
            TechFlowSecondaryButton(
                text = "Solicitar acceso de técnico",
                modifier = Modifier.fillMaxWidth(),
            )

            // Side-by-side buttons (alert card style from 5.html)
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TechFlowPrimaryButton(
                    text = "ATENDER",
                    modifier = Modifier.weight(1f),
                )
                TechFlowSecondaryButton(
                    text = "VER DETALLES",
                    modifier = Modifier.weight(1f),
                )
            }

            // Primary button with leading icon (details style)
            TechFlowPrimaryButton(
                text = "Detalles",
                trailingIcon = Icons.Outlined.Visibility,
            )

            // Disabled state
            TechFlowPrimaryButton(
                text = "Disabled",
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
// endregion
