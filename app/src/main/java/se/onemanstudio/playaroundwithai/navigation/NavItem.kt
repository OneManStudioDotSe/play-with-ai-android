package se.onemanstudio.playaroundwithai.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val route: Route,
    val label: String,
    val icon: ImageVector,
)
