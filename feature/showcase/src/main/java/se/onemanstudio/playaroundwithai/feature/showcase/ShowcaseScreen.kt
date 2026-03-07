package se.onemanstudio.playaroundwithai.feature.showcase

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTopAppBar
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.feature.showcase.sections.ColorSection
import se.onemanstudio.playaroundwithai.feature.showcase.sections.ComponentSection
import se.onemanstudio.playaroundwithai.feature.showcase.sections.TypographySection

@Composable
fun ShowcaseScreen() {
    Scaffold(
        topBar = {
            NeoBrutalTopAppBar(title = stringResource(R.string.showcase_title))
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimensions.paddingLarge)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

            TypographySection()

            Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

            ColorSection()

            Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

            ComponentSection()

            Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))
        }
    }
}
