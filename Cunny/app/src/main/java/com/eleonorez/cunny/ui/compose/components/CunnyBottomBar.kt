package com.eleonorez.cunny.ui.compose.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.GameController
import com.adamglin.phosphoricons.fill.House
import com.adamglin.phosphoricons.fill.SquaresFour
import com.adamglin.phosphoricons.fill.User
import com.adamglin.phosphoricons.regular.GameController
import com.adamglin.phosphoricons.regular.House
import com.adamglin.phosphoricons.regular.SquaresFour
import com.adamglin.phosphoricons.regular.User
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleonorez.cunny.ui.compose.navigation.CunnyRoutes
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily

@Composable
fun CunnyBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val barShape = RoundedCornerShape(CunnyDimens.radiusFull)
    val shadowColor = CunnyColors.primaryShadow // Theme-aware 3D shadow base

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, bottom = 14.dp)
            .height(64.dp)
            .background(
                color = shadowColor,
                shape = barShape
            )
    ) {
        // Main bar container offset upwards by -4.dp to expose the 3D shadow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-4).dp)
                .clip(barShape)
                // Solid glass gradient — no separate background layer needed
                .background(Brush.linearGradient(CunnyColors.gradNavBar))
        ) {
            // Tab content row
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf(
                    NavigationTab(
                        route = CunnyRoutes.HOME,
                        label = "Beranda",
                        filledIcon = PhosphorIcons.Fill.House,
                        outlinedIcon = PhosphorIcons.Regular.House
                    ),
                    NavigationTab(
                        route = CunnyRoutes.COURSES,
                        label = "Materi",
                        filledIcon = PhosphorIcons.Fill.SquaresFour,
                        outlinedIcon = PhosphorIcons.Regular.SquaresFour
                    ),
                    NavigationTab(
                        route = CunnyRoutes.PLAYGROUND,
                        label = "Playground",
                        filledIcon = PhosphorIcons.Fill.GameController,
                        outlinedIcon = PhosphorIcons.Regular.GameController
                    ),
                    NavigationTab(
                        route = CunnyRoutes.SETTINGS,
                        label = "Profil",
                        filledIcon = PhosphorIcons.Fill.User,
                        outlinedIcon = PhosphorIcons.Regular.User
                    )
                )

                tabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    BottomNavItem(
                        tab = tab,
                        selected = selected,
                        onClick = { onTabSelected(tab.route) }
                    )
                }
            }
        }
    }
}

private data class NavigationTab(
    val route: String,
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
)

@Composable
private fun RowScope.BottomNavItem(
    tab: NavigationTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) CunnyColors.textOnDarkSurface else CunnyColors.textOnDarkSurface.copy(alpha = 0.58f),
        label = "contentColor"
    )

    val startPadding by animateDpAsState(
        targetValue = if (selected) 16.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "startPadding"
    )
    val endPadding by animateDpAsState(
        targetValue = if (selected) 18.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "endPadding"
    )

    Box(
        modifier = Modifier
            .height(52.dp)
            .let {
                if (selected) {
                    it
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(CunnyDimens.radiusFull))
                        .background(Brush.linearGradient(CunnyColors.gradNavActive))
                } else {
                    it
                        .width(52.dp)
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(start = startPadding, end = endPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (selected) tab.filledIcon else tab.outlinedIcon,
                contentDescription = tab.label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            if (selected) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tab.label,
                    color = contentColor,
                    fontSize = 14.sp,
                    fontFamily = DmSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.01).sp
                )
            }
        }
    }
}
