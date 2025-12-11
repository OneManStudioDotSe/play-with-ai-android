package se.onemanstudio.playaroundwithai

import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)
