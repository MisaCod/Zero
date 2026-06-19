package com.example.zero.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.zero.ui.theme.Dimens
import com.example.zero.ui.theme.Radii
import com.example.zero.ui.theme.StatusArchivedBg
import com.example.zero.ui.theme.StatusArchivedText
import com.example.zero.ui.theme.StatusCompletedBg
import com.example.zero.ui.theme.StatusCompletedText
import com.example.zero.ui.theme.StatusOperative
import com.example.zero.ui.theme.StatusOperativeText
import com.example.zero.ui.theme.StatusRepair
import com.example.zero.ui.theme.StatusRepairText
import com.example.zero.ui.theme.TechFlowTheme

// ============================================================================
// StatusChip
// Reusable status indicator component used throughout the application.
//
// Patterns observed in HTML mockups:
//
// Pill-shaped (rounded-full) — used on cards and overlays:
//   "Activo"         → bg-primary, text-on-primary
//   "Urgente"        → bg-error, text-on-error
//   "Operativo"      → bg-white/90, text-emerald-700, dot emerald-500
//   "Reparación"     → bg-white/90, text-amber-700, dot amber-500
//   "Falla Crítica"  → bg-white/90, text-error, dot error
//   "Completado"     → bg-green-100, text-green-800, icon check_circle
//   "Archivado"      → bg-slate-200, text-slate-700, icon archive
//
// Rectangular (rounded/4dp) — used in work order cards and alert headers:
//   "Alta Prioridad" → bg-error-container, text-error
//   "Media"          → bg-secondary-container, text-on-secondary-container
//   "CRÍTICA"        → bg-error, text-on-error
//   "ADVERTENCIA"    → bg-tertiary, text-on-tertiary
//   "Preventivo"     → bg-secondary-container, text-on-secondary-container
//
// This component unifies all variants via a flexible API.
// ============================================================================

/**
 * The shape style for the status chip.
 */
enum class ChipShape {
    /** Rounded rectangle (4dp corners) — industrial, professional look. */
    Rounded,
    /** Full pill shape — used for overlays and card badges. */
    Pill,
}

/**
 * Flexible status chip supporting all status indicator patterns.
 *
 * @param text The status label text.
 * @param backgroundColor The chip background color.
 * @param textColor The chip text (and icon) color.
 * @param modifier Modifier for the chip.
 * @param chipShape Whether to use [ChipShape.Rounded] (4dp) or [ChipShape.Pill] (full).
 * @param leadingDotColor Optional colored dot before the text (e.g., Operativo's green dot).
 * @param leadingIcon Optional icon before the text (e.g., Completado's check_circle).
 * @param iconSize Size of the leading icon. Defaults to 16dp.
 * @param uppercase Whether to uppercase the text. Defaults to true for labels.
 */
@Composable
fun StatusChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    chipShape: ChipShape = ChipShape.Rounded,
    leadingDotColor: Color? = null,
    leadingIcon: ImageVector? = null,
    iconSize: Dp = 16.dp,
    uppercase: Boolean = true,
) {
    val shape = when (chipShape) {
        ChipShape.Rounded -> RoundedCornerShape(Radii.default) // 4dp
        ChipShape.Pill -> RoundedCornerShape(50)                // full pill
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            // Leading dot indicator
            if (leadingDotColor != null) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(leadingDotColor),
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Leading icon
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = textColor,
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = if (uppercase) text.uppercase() else text,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
            )
        }
    }
}

// ============================================================================
// Convenience factory functions for common status chip variants.
// These match the exact color combinations from the HTML mockups.
// ============================================================================

/** "Operativo" — green dot + emerald text on white overlay (3.html inventory cards). */
@Composable
fun OperativeChip(modifier: Modifier = Modifier) {
    StatusChip(
        text = "Operativo",
        backgroundColor = Color.White.copy(alpha = 0.9f),
        textColor = StatusOperativeText,
        leadingDotColor = StatusOperative,
        chipShape = ChipShape.Pill,
        uppercase = false,
        modifier = modifier,
    )
}

/** "En Reparación" — amber dot + amber text on white overlay (3.html). */
@Composable
fun RepairChip(modifier: Modifier = Modifier) {
    StatusChip(
        text = "Reparación",
        backgroundColor = Color.White.copy(alpha = 0.9f),
        textColor = StatusRepairText,
        leadingDotColor = StatusRepair,
        chipShape = ChipShape.Pill,
        uppercase = false,
        modifier = modifier,
    )
}

/** "Falla Crítica" — red dot + error text on white overlay (3.html). */
@Composable
fun CriticalFailureChip(modifier: Modifier = Modifier) {
    StatusChip(
        text = "Falla Crítica",
        backgroundColor = Color.White.copy(alpha = 0.9f),
        textColor = MaterialTheme.colorScheme.error,
        leadingDotColor = MaterialTheme.colorScheme.error,
        chipShape = ChipShape.Pill,
        uppercase = false,
        modifier = modifier,
    )
}

/** "Alta Prioridad" — error container bg + error text (1.html work order cards). */
@Composable
fun HighPriorityChip(modifier: Modifier = Modifier) {
    StatusChip(
        text = "Alta Prioridad",
        backgroundColor = MaterialTheme.colorScheme.errorContainer,
        textColor = MaterialTheme.colorScheme.error,
        chipShape = ChipShape.Rounded,
        modifier = modifier,
    )
}

/** "Urgente" — solid error bg + on-error text (1.html dashboard bento card). */
@Composable
fun UrgentChip(modifier: Modifier = Modifier) {
    StatusChip(
        text = "Urgente",
        backgroundColor = MaterialTheme.colorScheme.error,
        textColor = MaterialTheme.colorScheme.onError,
        chipShape = ChipShape.Pill,
        modifier = modifier,
    )
}

/** "Activo" — solid primary bg + on-primary text (1.html dashboard bento card). */
@Composable
fun ActiveChip(modifier: Modifier = Modifier) {
    StatusChip(
        text = "Activo",
        backgroundColor = MaterialTheme.colorScheme.primary,
        textColor = MaterialTheme.colorScheme.onPrimary,
        chipShape = ChipShape.Pill,
        modifier = modifier,
    )
}

/** "Completado" — green bg + green text + check_circle icon (4.html history). */
@Composable
fun CompletedChip(modifier: Modifier = Modifier) {
    StatusChip(
        text = "Completado",
        backgroundColor = StatusCompletedBg,
        textColor = StatusCompletedText,
        leadingIcon = Icons.Filled.CheckCircle,
        chipShape = ChipShape.Pill,
        uppercase = false,
        modifier = modifier,
    )
}

/** "Archivado" — slate bg + slate text + archive icon (4.html history). */
@Composable
fun ArchivedChip(modifier: Modifier = Modifier) {
    StatusChip(
        text = "Archivado",
        backgroundColor = StatusArchivedBg,
        textColor = StatusArchivedText,
        leadingIcon = Icons.Filled.Archive,
        chipShape = ChipShape.Pill,
        uppercase = false,
        modifier = modifier,
    )
}

/** "CRÍTICA" — solid error bg + on-error text (5.html alerts). */
@Composable
fun CriticalAlertChip(modifier: Modifier = Modifier) {
    StatusChip(
        text = "CRÍTICA",
        backgroundColor = MaterialTheme.colorScheme.error,
        textColor = MaterialTheme.colorScheme.onError,
        chipShape = ChipShape.Rounded,
        modifier = modifier,
    )
}

/** "ADVERTENCIA" — solid tertiary bg + on-tertiary text (5.html alerts). */
@Composable
fun WarningChip(modifier: Modifier = Modifier) {
    StatusChip(
        text = "ADVERTENCIA",
        backgroundColor = MaterialTheme.colorScheme.tertiary,
        textColor = MaterialTheme.colorScheme.onTertiary,
        chipShape = ChipShape.Rounded,
        modifier = modifier,
    )
}

/** "Preventivo" — secondary-container bg + on-secondary-container text (4.html). */
@Composable
fun PreventiveChip(modifier: Modifier = Modifier) {
    StatusChip(
        text = "Preventivo",
        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
        chipShape = ChipShape.Rounded,
        uppercase = false,
        modifier = modifier,
    )
}

/** "Media" — secondary-container bg + on-secondary-container text (1.html). */
@Composable
fun MediumPriorityChip(modifier: Modifier = Modifier) {
    StatusChip(
        text = "Media",
        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
        chipShape = ChipShape.Rounded,
        modifier = modifier,
    )
}

// region Previews
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun StatusChipShowcasePreview() {
    TechFlowTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.sm),
        ) {
            // Pill-shaped chips
            Text("Pill Chips (Cards & Overlays)", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                ActiveChip()
                UrgentChip()
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                OperativeChip()
                RepairChip()
                CriticalFailureChip()
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                CompletedChip()
                ArchivedChip()
            }

            Spacer(modifier = Modifier.size(Dimens.sm))

            // Rectangular chips
            Text("Rounded Chips (Work Orders & Alerts)", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                HighPriorityChip()
                MediumPriorityChip()
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                CriticalAlertChip()
                WarningChip()
            }
            PreventiveChip()
        }
    }
}
// endregion
