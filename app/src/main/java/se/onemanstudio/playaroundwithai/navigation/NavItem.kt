package se.onemanstudio.playaroundwithai.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val route: Route,
    @param: StringRes val labelRes: Int,
    val icon: ImageVector,
)
