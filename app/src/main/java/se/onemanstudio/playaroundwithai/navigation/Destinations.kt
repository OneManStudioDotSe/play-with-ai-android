package se.onemanstudio.playaroundwithai.navigation

import kotlinx.serialization.Serializable

sealed interface Route

@Serializable
object Chat : Route

@Serializable
object Maps : Route
