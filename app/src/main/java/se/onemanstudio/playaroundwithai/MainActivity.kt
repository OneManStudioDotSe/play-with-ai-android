package se.onemanstudio.playaroundwithai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import se.onemanstudio.playaroundwithai.ui.screens.ChatScreen
import se.onemanstudio.playaroundwithai.ui.screens.unused.DoScreen
import se.onemanstudio.playaroundwithai.ui.screens.unused.EatScreen
import se.onemanstudio.playaroundwithai.ui.screens.unused.M3ComponentsShowcaseScreen
import se.onemanstudio.playaroundwithai.ui.screens.unused.SeeScreen
import se.onemanstudio.playaroundwithai.ui.theme.AIAITheme
import se.onemanstudio.playaroundwithai.viewmodels.ChatViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AIAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf("Do", "See", "eat", "hear")

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        selected = navController.currentBackStackEntryAsState().value?.destination?.route == screen,
                        onClick = { navController.navigate(screen) },
                        label = { Text(screen.uppercase()) },
                        icon = { Icon(Icons.Default.Info, contentDescription = null) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = "Do", Modifier.padding(innerPadding)) {
            composable("Do") { DoScreen() }
            composable("see") { SeeScreen() }
            composable("eat") { EatScreen() }
            composable("hear") { M3ComponentsShowcaseScreen() }
        }
    }
}
