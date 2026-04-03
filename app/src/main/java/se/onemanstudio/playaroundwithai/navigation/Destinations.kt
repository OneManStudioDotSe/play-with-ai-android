package se.onemanstudio.playaroundwithai.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import kotlinx.serialization.Serializable
import se.onemanstudio.playaroundwithai.R

sealed interface Route

@Serializable
object Chat : Route

@Serializable
object Maps : Route

@Serializable
object Dreams : Route

@Serializable
object Agents : Route

@Serializable
object Showcase : Route

val navItems = listOf(
    NavItem(Chat, R.string.nav_label_chat, Icons.Default.Chair),
    NavItem(Dreams, R.string.nav_label_explain, Icons.Default.NightsStay),
    NavItem(Agents, R.string.nav_label_plan, Icons.Default.AutoAwesome),
    NavItem(Maps, R.string.nav_label_explore, Icons.Default.Map),
    NavItem(Showcase, R.string.nav_label_sofa, Icons.Default.Palette),
)
