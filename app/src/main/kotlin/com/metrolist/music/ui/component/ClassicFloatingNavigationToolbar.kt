/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.metrolist.music.R
import com.metrolist.music.ui.screens.Screens

/** Edge-to-edge bottom navigation backed by Meld routes and actions. */
@Composable
fun ClassicFloatingNavigationToolbar(
    items: List<Screens>,
    pureBlack: Boolean,
    glassEnabled: Boolean,
    isSelected: (Screens) -> Boolean,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor =
        when {
            glassEnabled -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f)
            pureBlack -> Color.Black
            else -> MaterialTheme.colorScheme.surfaceContainer
        }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            shape = RectangleShape,
            color = containerColor,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
                        .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { screen ->
                    ClassicToolbarDestination(
                        screen = screen,
                        selected = isSelected(screen),
                        onClick = { onItemClick(screen, isSelected(screen)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassicToolbarDestination(
    screen: Screens,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val selectedColor = MaterialTheme.colorScheme.secondaryContainer
    val background by animateColorAsState(
        targetValue = if (selected) selectedColor else Color.Transparent,
        label = "classicNavBackground",
    )
    val contentColor by animateColorAsState(
        targetValue =
            if (selected) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "classicNavContent",
    )
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "classicNavScale",
    )

    Box(
        modifier =
            Modifier
                .scale(scale)
                .size(width = 68.dp, height = 40.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Tab,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.size(width = 68.dp, height = 40.dp),
            shape = RoundedCornerShape(14.dp),
            color = background,
            contentColor = contentColor,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    modifier = Modifier.size(22.dp),
                    painter = painterResource(if (selected) screen.iconIdActive else screen.iconIdInactive),
                    contentDescription = stringResource(screen.titleId),
                )
            }
        }
    }
}
