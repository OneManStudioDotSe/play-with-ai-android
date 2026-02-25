package se.onemanstudio.playaroundwithai.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NightsStay
import kotlinx.serialization.Serializable

sealed interface Route

@Serializable
object Chat : Route

@Serializable
object Maps : Route

@Serializable
object Dreams : Route

@Serializable
object Agents : Route

val navItems = listOf(
    NavItem(Chat, "Chat", Icons.Default.Chair),
    NavItem(Dreams, "Explain", Icons.Default.NightsStay),
    NavItem(Agents, "Plan", Icons.Default.AutoAwesome),
    NavItem(Maps, "Explore", Icons.Default.Map),
)
