@file:Suppress("MagicNumber")

package se.onemanstudio.playaroundwithai.feature.nano

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTopAppBar
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.nano.models.DeviceInfo
import se.onemanstudio.playaroundwithai.feature.nano.models.NanoSupport
import se.onemanstudio.playaroundwithai.feature.nano.states.NanoPhase
import se.onemanstudio.playaroundwithai.feature.nano.states.NanoUiState
import se.onemanstudio.playaroundwithai.feature.nano.views.IntroCard
import se.onemanstudio.playaroundwithai.feature.nano.views.SupportedDevicesSection
import se.onemanstudio.playaroundwithai.feature.nano.views.ThisDeviceSection

@Composable
fun NanoScreen(
    viewModel: NanoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    NanoScreenContent(
        state = state,
        onDownload = viewModel::downloadModel,
        onRetry = viewModel::checkSupport,
    )
}

@Composable
private fun NanoScreenContent(
    state: NanoUiState,
    onDownload: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = { NeoBrutalTopAppBar(title = stringResource(R.string.nano_title)) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimensions.paddingLarge)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

            IntroCard()

            Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

            SupportedDevicesSection()

            Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

            ThisDeviceSection(state = state, onDownload = onDownload, onRetry = onRetry)

            Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

            Text(
                text = stringResource(R.string.nano_source),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))
        }
    }
}

@Preview(name = "Nano — Ready")
@Composable
private fun NanoReadyPreview() {
    SofaAiTheme {
        NanoScreenContent(
            state = NanoUiState(
                device = DeviceInfo("Google", "Pixel 9 Pro", 35),
                phase = NanoPhase.Evaluated(NanoSupport.READY),
            ),
            onDownload = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Nano — Downloadable")
@Composable
private fun NanoDownloadablePreview() {
    SofaAiTheme {
        NanoScreenContent(
            state = NanoUiState(
                device = DeviceInfo("Samsung", "SM-S921B", 34),
                phase = NanoPhase.Evaluated(NanoSupport.DOWNLOADABLE),
            ),
            onDownload = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Nano — Not available")
@Composable
private fun NanoUnavailablePreview() {
    SofaAiTheme(darkTheme = true) {
        NanoScreenContent(
            state = NanoUiState(
                device = DeviceInfo("Generic", "AOSP on emulator", 33),
                phase = NanoPhase.Evaluated(NanoSupport.NOT_AVAILABLE),
            ),
            onDownload = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Nano — Downloading")
@Composable
private fun NanoDownloadingPreview() {
    SofaAiTheme {
        NanoScreenContent(
            state = NanoUiState(
                device = DeviceInfo("Google", "Pixel 9 Pro", 35),
                phase = NanoPhase.Downloading(downloadedBytes = 600_000_000L, totalBytes = 1_024_000_000L),
            ),
            onDownload = {},
            onRetry = {},
        )
    }
}
