package com.example.doggitoapp.android.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.doggitoapp.android.core.theme.*

enum class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Inicio", Icons.Filled.Home, Icons.Outlined.Home),
    TASKS("tasks", "Tareas", Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt),
    RUNNING("running_direct", "Running", Icons.Filled.DirectionsRun, Icons.Outlined.DirectionsRun),
    PROFILE("profile", "Perfil", Icons.Filled.Pets, Icons.Outlined.Pets)
}

@Composable
fun DoggitoBottomBar(
    currentRoute: String?,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = DoggitoGreenDark.copy(alpha = 0.15f),
                spotColor = DoggitoGreenDark.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(28.dp),
        color = CardSurface.copy(alpha = 0.95f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.entries.forEach { item ->
                val isSelected = when (item) {
                    BottomNavItem.HOME -> currentRoute == "home"
                    BottomNavItem.TASKS -> currentRoute == "tasks"
                    BottomNavItem.RUNNING -> currentRoute?.startsWith("running") == true
                    BottomNavItem.PROFILE -> currentRoute == "profile" || currentRoute?.startsWith("profile/") == true
                }

                BottomNavItemView(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onItemSelected(item) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItemView(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) DoggitoGreen.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "navBg"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) DoggitoGreenDark else TextSecondary.copy(alpha = 0.5f),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "navIcon"
    )

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .background(bgColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        if (isSelected) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = DoggitoGreenDark
            )
        }
    }
}
