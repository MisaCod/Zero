package com.example.zero.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zero.ui.theme.TechFlowTheme

// ============================================================================
// TechFlowTopBar
// Reusable top app bar matching the industrial design across all screens.
//
// Variants observed in HTML files:
//   1.html / 4.html: precision_manufacturing icon + "D&S Refrigerantes" + profile image
//   3.html / 5.html: menu icon + "TechFlow" / "D&S Refrigerantes" + account_circle
//
// This component unifies both patterns via parameters.
// ============================================================================

/**
 * Main top app bar for the TechFlow application.
 *
 * @param title The title text displayed next to the navigation icon.
 * @param onNavigationClick Callback for the navigation icon (menu or back).
 * @param onProfileClick Callback for the profile/account action.
 * @param navigationIcon Custom navigation icon composable. Defaults to a menu icon.
 * @param profileContent Custom trailing content. Defaults to an account_circle icon.
 * @param scrollBehavior Optional scroll behavior for collapsing/pinning.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechFlowTopBar(
    title: String = "D&S Refrigerantes",
    onNavigationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    navigationIcon: @Composable () -> Unit = {
        IconButton(onClick = onNavigationClick) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = "Menú",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    },
    profileContent: @Composable () -> Unit = {
        IconButton(onClick = onProfileClick) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Perfil",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape,
                    ),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    },
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        navigationIcon = navigationIcon,
        actions = { profileContent() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        scrollBehavior = scrollBehavior,
    )
}

/**
 * Variant with the precision_manufacturing brand icon instead of a menu.
 * Used on the Dashboard (1.html) and History (4.html) screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechFlowBrandTopBar(
    title: String = "D&S Refrigerantes",
    onProfileClick: () -> Unit = {},
    profileContent: @Composable () -> Unit = {
        IconButton(onClick = onProfileClick) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Perfil",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape,
                    ),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    },
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TechFlowTopBar(
        title = title,
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Outlined.PrecisionManufacturing,
                    contentDescription = "TechFlow",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        profileContent = profileContent,
        scrollBehavior = scrollBehavior,
    )
}

// region Previews
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun TechFlowTopBarPreview() {
    TechFlowTheme(darkTheme = false) {
        TechFlowTopBar(title = "TechFlow")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun TechFlowBrandTopBarPreview() {
    TechFlowTheme(darkTheme = false) {
        TechFlowBrandTopBar()
    }
}
// endregion
