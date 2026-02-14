package se.onemanstudio.playaroundwithai.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Map
import kotlinx.serialization.Serializable

sealed interface Route

@Serializable
object Chat : Route

@Serializable
object Maps : Route

val navItems = listOf(
    NavItem(Chat, "Chat", Icons.Default.Chair),
    NavItem(Maps, "Explore", Icons.Default.Map),
)
