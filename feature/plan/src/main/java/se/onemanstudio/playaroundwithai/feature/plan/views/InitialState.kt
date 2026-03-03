package se.onemanstudio.playaroundwithai.feature.plan.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalChip
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTextField
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.plan.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InitialState(
    textState: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    onPlanClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.paddingExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NeoBrutalTextField(
            value = textState,
            onValueChange = onTextChanged,
            placeholder = stringResource(R.string.plan_input_placeholder),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

        NeoBrutalButton(
            text = stringResource(R.string.plan_plan_button),
            enabled = textState.text.isNotBlank(),
            backgroundColor = MaterialTheme.colorScheme.primary,
            onClick = onPlanClick,
        )

        Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

        Text(
            text = stringResource(R.string.plan_try_suggestions),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimensions.paddingMedium),
        )

        val coffeeText = stringResource(R.string.plan_example_coffee)
        val museumsText = stringResource(R.string.plan_example_museums)
        val parksText = stringResource(R.string.plan_example_parks)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium),
        ) {
            NeoBrutalChip(
                text = coffeeText,
                onClick = { onTextChanged(TextFieldValue(coffeeText)) },
            )
            NeoBrutalChip(
                text = museumsText,
                onClick = { onTextChanged(TextFieldValue(museumsText)) },
            )
            NeoBrutalChip(
                text = parksText,
                onClick = { onTextChanged(TextFieldValue(parksText)) },
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

        HowItWorksCard()
    }
}

@Preview(name = "Initial Light")
@Composable
private fun InitialStateLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            InitialState(
                textState = TextFieldValue(""),
                onTextChanged = {},
                onPlanClick = {},
            )
        }
    }
}

@Preview(name = "Initial Dark")
@Composable
private fun InitialStateDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            InitialState(
                textState = TextFieldValue("Coffee tour in Stockholm"),
                onTextChanged = {},
                onPlanClick = {},
            )
        }
    }
}
