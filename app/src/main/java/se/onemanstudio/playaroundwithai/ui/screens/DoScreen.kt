package se.onemanstudio.playaroundwithai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import se.onemanstudio.playaroundwithai.viewmodels.DoViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DoScreen(viewModel: DoViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var expanded by rememberSaveable { mutableStateOf(true) }

//    Scaffold(
//        content = { innerPadding ->
//            Box(Modifier.padding(innerPadding)) {
//                LazyColumn(
//                    // Apply a floatingToolbarVerticalNestedScroll Modifier toggle the expanded
//                    // state of the HorizontalFloatingToolbar.
//                    modifier =
//                        Modifier.floatingToolbarVerticalNestedScroll(
//                            expanded = expanded,
//                            onExpand = { expanded = true },
//                            onCollapse = { expanded = false },
//                        ),
//                    state = rememberLazyListState(),
//                    contentPadding = innerPadding,
//                    verticalArrangement = Arrangement.spacedBy(8.dp),
//                ) {
//                    val list = (0..75).map { it.toString() }
//                    items(count = list.size) {
//                        Text(
//                            text = list[it],
//                            style = MaterialTheme.typography.bodyLarge,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(horizontal = 16.dp),
//                        )
//                    }
//                }
//
//                HorizontalFloatingToolbar(
//                    modifier = Modifier
//                        .align(Alignment.BottomCenter)
//                        .offset(y = -ScreenOffset),
//                    expanded = expanded,
//                    leadingContent = {  },
//                    trailingContent = {  },
//                    content = {
//                        FilledIconButton(
//                            modifier = Modifier.width(64.dp),
//                            onClick = { /* doSomething() */ },
//                        ) {
//                            Icon(Icons.Filled.Add, contentDescription = "Localized description")
//                        }
//                    },
//                )
//            }
//        }
//    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("DO SCREEN", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.performDoAction() }) {
            Text("Get Activity Suggestion")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (state.isLoading) CircularProgressIndicator() else state.result?.let { Text(it) }
    }
}