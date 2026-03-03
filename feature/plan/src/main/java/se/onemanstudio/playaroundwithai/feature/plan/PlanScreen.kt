@file:Suppress("TooManyFunctions")

package se.onemanstudio.playaroundwithai.feature.plan

import android.Manifest
import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButtonSmall
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTopAppBar
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.DEFAULT_LAT
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.DEFAULT_LNG
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanUiState

@SuppressLint("MissingPermission")
@Composable
fun PlanScreen(
    viewModel: PlanViewModel = hiltViewModel(),
    settingsContent: @Composable (() -> Unit) -> Unit = { _ -> },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var textState by remember { mutableStateOf(TextFieldValue("")) }
    var showSettings by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current

    var userLatitude by remember { mutableDoubleStateOf(DEFAULT_LAT) }
    var userLongitude by remember { mutableDoubleStateOf(DEFAULT_LNG) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLatitude = it.latitude
                    userLongitude = it.longitude
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is PlanUiState.Result -> view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            is PlanUiState.Error -> view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
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
                title = stringResource(R.string.plan_title),
                actions = {
                    NeoBrutalIconButtonSmall(
                        imageVector = Icons.Default.Science,
                        contentDescription = stringResource(R.string.plan_load_sample_data),
                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                        onClick = { viewModel.loadSampleData() },
                    )
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
            when (val state = uiState) {
                is PlanUiState.Initial -> InitialState(
                    textState = textState,
                    onTextChanged = { textState = it },
                    onPlanClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        viewModel.planTrip(textState.text, userLatitude, userLongitude)
                        keyboardController?.hide()
                    },
                )

                is PlanUiState.Running -> RunningState(state = state)

                is PlanUiState.Result -> ResultState(
                    state = state,
                    onNewPlan = {
                        textState = TextFieldValue("")
                        viewModel.resetToInitial()
                    },
                )

                is PlanUiState.Error -> ErrorState(
                    state = state,
                    onClearError = { viewModel.resetToInitial() },
                )
            }
        }
    }
}
