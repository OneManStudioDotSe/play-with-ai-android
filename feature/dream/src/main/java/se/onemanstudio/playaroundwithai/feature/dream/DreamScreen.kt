package se.onemanstudio.playaroundwithai.feature.dream

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButtonSmall
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTopAppBar
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamUiState
import se.onemanstudio.playaroundwithai.feature.dream.views.states.DreamErrorContent
import se.onemanstudio.playaroundwithai.feature.dream.views.states.DreamInitialContent
import se.onemanstudio.playaroundwithai.feature.dream.views.states.DreamResultContent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@Composable
fun DreamScreen(
    viewModel: DreamViewModel = hiltViewModel(),
    settingsContent: @Composable (() -> Unit) -> Unit = { _ -> },
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val uiState = screenState.dreamState
    val history = screenState.dreamHistory
    val imageState = screenState.imageState

    var textState by remember { mutableStateOf(TextFieldValue("")) }
    var showSettings by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current

    LaunchedEffect(uiState) {
        when (uiState) {
            is DreamUiState.Result -> view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            is DreamUiState.Error -> view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            else -> {}
        }
    }

    if (showSettings) {
        settingsContent { showSettings = false }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NeoBrutalTopAppBar(
                title = stringResource(R.string.dream_title),
                actions = {
                    NeoBrutalIconButtonSmall(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(
                            se.onemanstudio.playaroundwithai.core.ui.views.R.string.settings_icon_description
                        ),
                        onClick = { showSettings = true },
                    )
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
            contentAlignment = Alignment.Center,
        ) {
            when (uiState) {
                is DreamUiState.Initial -> DreamInitialContent(
                    textState = textState,
                    onTextChanged = { textState = it },
                    onInterpretClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        viewModel.interpretDream(textState.text)
                        keyboardController?.hide()
                    },
                    history = history,
                    onDreamClick = { dream -> viewModel.restoreDream(dream) },
                )

                is DreamUiState.Interpreting, is DreamUiState.Result -> DreamResultContent(
                    state = uiState as? DreamUiState.Result,
                    imageState = imageState,
                    onNewDream = {
                        textState = TextFieldValue("")
                        viewModel.clearResult()
                    },
                )

                is DreamUiState.Error -> DreamErrorContent(
                    state = uiState,
                    onClearError = { viewModel.clearResult() },
                )
            }
        }
    }
}
