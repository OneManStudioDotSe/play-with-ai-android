package se.onemanstudio.playaroundwithai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Normal
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import se.onemanstudio.playaroundwithai.core.ui.theme.Alphas
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.plan.PlanScreen
import se.onemanstudio.playaroundwithai.feature.chat.ChatScreen
import se.onemanstudio.playaroundwithai.feature.dream.DreamScreen
import se.onemanstudio.playaroundwithai.feature.explore.ExploreScreen
import se.onemanstudio.playaroundwithai.settings.SettingsBottomSheetContainer
import se.onemanstudio.playaroundwithai.navigation.Agents
import se.onemanstudio.playaroundwithai.navigation.Chat
import se.onemanstudio.playaroundwithai.navigation.Dreams
import se.onemanstudio.playaroundwithai.navigation.Maps
import se.onemanstudio.playaroundwithai.navigation.navItems

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent { SoFaApp() }
    }
}

@Composable
private fun SoFaApp() {
    val viewModel: MainViewModel = hiltViewModel()
    val authError by viewModel.authError.collectAsState()
    val authRetriesExhausted by viewModel.authRetriesExhausted.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val authErrorMessage = stringResource(R.string.auth_error_message)
    val retryLabel = stringResource(R.string.auth_error_retry)
    val retriesExhaustedMessage = stringResource(R.string.auth_error_retries_exhausted)
    val tokenUsageTemplate = stringResource(R.string.token_usage_snackbar)

    LaunchedEffect(Unit) {
        viewModel.tokenUsageMessage.collect { formattedTokens ->
            snackbarHostState.showSnackbar(
                message = String.format(tokenUsageTemplate, formattedTokens),
                duration = SnackbarDuration.Short,
            )
        }
    }

    LaunchedEffect(authError, authRetriesExhausted) {
        if (authRetriesExhausted) {
            snackbarHostState.showSnackbar(
                message = retriesExhaustedMessage,
                duration = SnackbarDuration.Indefinite
            )
        } else if (authError) {
            val result = snackbarHostState.showSnackbar(
                message = authErrorMessage,
                actionLabel = retryLabel,
                duration = SnackbarDuration.Long
            )

            if (result == SnackbarResult.ActionPerformed) {
                viewModel.retryAuth()
            }
        }
    }

    SofaAiTheme {
        val navController = rememberNavController()

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.Transparent,
                    modifier = Modifier.padding(horizontal = Dimensions.paddingLarge)
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    navItems.forEach { screen ->
                        val isSelected = currentDestination?.hasRoute(screen.route::class) == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            label = { Text(text = screen.label, fontWeight = if (isSelected) Bold else Normal) },
                            icon = { Icon(screen.icon, contentDescription = "${screen.label} Tab") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.surface,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = MaterialTheme.colorScheme.onSurface,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.medium),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.medium)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
                navController = navController,
                startDestination = Chat,
            ) {
                composable<Chat> {
                    ChatScreen(settingsContent = { onDismiss -> SettingsBottomSheetContainer(onDismiss = onDismiss) })
                }
                composable<Maps> {
                    ExploreScreen(settingsContent = { onDismiss -> SettingsBottomSheetContainer(onDismiss = onDismiss) })
                }
                composable<Dreams> {
                    DreamScreen(settingsContent = { onDismiss -> SettingsBottomSheetContainer(onDismiss = onDismiss) })
                }
                composable<Agents> {
                    PlanScreen(settingsContent = { onDismiss -> SettingsBottomSheetContainer(onDismiss = onDismiss) })
                }
            }
        }
    }
}
