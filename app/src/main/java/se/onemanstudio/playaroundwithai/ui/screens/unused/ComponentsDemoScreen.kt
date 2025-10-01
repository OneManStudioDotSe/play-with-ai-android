package se.onemanstudio.playaroundwithai.ui.screens.unused

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Label
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun M3ComponentsShowcaseScreen() {
    var isCheckboxChecked by remember { mutableStateOf(true) }
    var isChipEnabled by remember { mutableStateOf(true) }

    var isChipSelected by remember { mutableStateOf(false) }

    var progressForLoadingAnimation1 by remember { mutableFloatStateOf(0f) }
    val animatedProgressForLoadingAnimation1 by
    animateFloatAsState(
        targetValue = progressForLoadingAnimation1,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessVeryLow,
                visibilityThreshold = 1 / 1000f,
            ),
    )

    var progress by remember { mutableFloatStateOf(0.1f) }
    val animatedProgress by
    animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )
    val thickStrokeWidth = with(LocalDensity.current) { 8.dp.toPx() }
    val thickStroke = remember(thickStrokeWidth) { Stroke(width = thickStrokeWidth, cap = StrokeCap.Round) }

    var isDropdownMenuExpanded by remember { mutableStateOf(false) }

    val radioOptions = listOf("Calls", "Missed", "Friends")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            SectionTitle("Text field")
            val state = rememberTextFieldState()

            var alwaysMinimizeLabel by remember { mutableStateOf(false) }
            Column {
                Row {
                    Checkbox(checked = alwaysMinimizeLabel, onCheckedChange = { alwaysMinimizeLabel = it })
                    Text("Show placeholder even when unfocused")
                }
                Spacer(Modifier.height(16.dp))
                TextField(
                    state = state,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    label = { Text("Label") },
                    labelPosition = TextFieldLabelPosition.Attached(alwaysMinimize = alwaysMinimizeLabel),
                    prefix = { Text("www.") },
                    suffix = { Text(".com") },
                    placeholder = { Text("google") },
                    leadingIcon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { state.clearText() }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear text")
                        }
                    },
                )
            }
        }

        item {
            SectionTitle("Carousel #1")
            CarouselExample_MultiBrowse()
        }

        item {
            SectionTitle("Carousel #2")
            CarouselExample()
        }

        item {
            SectionTitle("Split buttons")
            var checked by remember { mutableStateOf(false) }

            Box(modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()) {
                SplitButtonLayout(
                    leadingButton = {
                        SplitButtonDefaults.LeadingButton(onClick = { /* Do Nothing */ }) {
                            Icon(
                                Icons.Filled.Edit,
                                modifier = Modifier.size(SplitButtonDefaults.LeadingIconSize),
                                contentDescription = "Localized description",
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("My Button")
                        }
                    },
                    trailingButton = {
                        SplitButtonDefaults.TrailingButton(
                            checked = checked,
                            onCheckedChange = { checked = it },
                            modifier =
                                Modifier.semantics {
                                    stateDescription = if (checked) "Expanded" else "Collapsed"
                                    contentDescription = "Toggle Button"
                                },
                        ) {
                            val rotation: Float by
                            animateFloatAsState(
                                targetValue = if (checked) 180f else 0f,
                                label = "Trailing Icon Rotation",
                            )
                            Icon(
                                Icons.Filled.KeyboardArrowDown,
                                modifier =
                                    Modifier
                                        .size(SplitButtonDefaults.TrailingIconSize)
                                        .graphicsLayer {
                                            this.rotationZ = rotation
                                        },
                                contentDescription = "Localized description",
                            )
                        }
                    },
                )

                DropdownMenu(expanded = checked, onDismissRequest = { checked = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { /* Handle edit! */ },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = { /* Handle settings! */ },
                        leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Send Feedback") },
                        onClick = { /* Handle send feedback! */ },
                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                        trailingIcon = { Text("F11", textAlign = TextAlign.Center) },
                    )
                }
            }
        }

        item {
            SectionTitle("Badges")
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BadgedBox(badge = { Badge { Text("3") } }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications"
                    )
                }

                BadgedBox(badge = { Badge { } }) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite"
                    )
                }
            }
        }

        item {
            SectionTitle("Loaders")
            Column(horizontalAlignment = Alignment.CenterHorizontally) { LoadingIndicator() }

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth()
                    .padding(vertical = 16.dp),
                thickness = 1.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LoadingIndicator(progress = { animatedProgressForLoadingAnimation1 })
                Spacer(Modifier.requiredHeight(30.dp))
                Text("Set loading progress:")
                Slider(
                    modifier = Modifier.width(300.dp),
                    value = progressForLoadingAnimation1,
                    valueRange = 0f..1f,
                    onValueChange = { progressForLoadingAnimation1 = it },
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth()
                    .padding(vertical = 16.dp),
                thickness = 1.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) { LinearWavyProgressIndicator() }

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth()
                    .padding(vertical = 16.dp),
                thickness = 1.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearWavyProgressIndicator(
                    progress = { animatedProgress },
                    // Thick height is slightly higher than the
                    // WavyProgressIndicatorDefaults.LinearContainerHeight default
                    modifier = Modifier.height(14.dp),
                    stroke = thickStroke,
                    trackStroke = thickStroke,
                )
                Spacer(Modifier.requiredHeight(30.dp))
                Text("Set progress:")
                Slider(
                    modifier = Modifier.width(300.dp),
                    value = progress,
                    valueRange = 0f..1f,
                    onValueChange = { progress = it },
                )
            }
        }

        item {
            SectionTitle("Dropdown menu")
            Box {
                IconButton(onClick = { isDropdownMenuExpanded = !isDropdownMenuExpanded }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(
                    expanded = isDropdownMenuExpanded,
                    onDismissRequest = { isDropdownMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Option 1") },
                        onClick = { /* Do something... */ }
                    )
                    DropdownMenuItem(
                        text = { Text("Option 2") },
                        onClick = { /* Do something... */ }
                    )
                }
            }
        }

        item {
            SectionTitle("Radio buttons")
            // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
            Column(Modifier.selectableGroup()) {
                radioOptions.forEach { text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .selectable(
                                selected = (text == selectedOption),
                                onClick = { onOptionSelected(text) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (text == selectedOption),
                            onClick = null // null recommended for accessibility with screen readers
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }

        item {
            SectionTitle("Slider")
            var sliderPositionSimple by rememberSaveable { mutableStateOf(0f) }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(text = "%.2f".format(sliderPositionSimple))
                Slider(value = sliderPositionSimple, onValueChange = { sliderPositionSimple = it })
            }
        }

        item {
            SectionTitle("Slider with steps")
            val sliderState =
                rememberSliderState(
                    // Only allow multiples of 10. Excluding the endpoints of `valueRange`,
                    // there are 9 steps (10, 20, ..., 90).
                    steps = 9,
                    valueRange = 0f..100f,
                    onValueChangeFinished = {
                        // launch some business logic update with the state you hold
                        // viewModel.updateSelectedSliderValue(sliderPosition)
                    },
                )
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(text = "%.2f".format(sliderState.value))
                Slider(state = sliderState)
            }
        }

        item {
            SectionTitle("Slider customised")
            var sliderPosition by rememberSaveable { mutableStateOf(0f) }
            val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    valueRange = 0f..100f,
                    interactionSource = interactionSource,
                    onValueChangeFinished = {
                        // launch some business logic update with the state you hold
                        // viewModel.updateSelectedSliderValue(sliderPosition)
                    },
                    thumb = {
                        Label(
                            label = {
                                PlainTooltip(
                                    modifier = Modifier
                                        .sizeIn(45.dp, 25.dp)
                                        .wrapContentWidth()
                                ) {
                                    Text("%.2f".format(sliderPosition))
                                }
                            },
                            interactionSource = interactionSource,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                tint = Color.Red,
                            )
                        }
                    },
                )
            }

            // and more examples at https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#Slider(androidx.compose.material3.SliderState,androidx.compose.ui.Modifier,kotlin.Boolean,androidx.compose.material3.SliderColors,androidx.compose.foundation.interaction.MutableInteractionSource,kotlin.Function1,kotlin.Function1)
        }

        item {
            SectionTitle("Switch")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                var checked1 by remember { mutableStateOf(true) }

                Switch(
                    checked = checked1,
                    onCheckedChange = {
                        checked1 = it
                    },
                    thumbContent = if (checked1) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else {
                        null
                    }
                )

                VerticalDivider(color = MaterialTheme.colorScheme.secondary)

                var checked2 by remember { mutableStateOf(true) }

                Switch(
                    checked = checked2,
                    onCheckedChange = {
                        checked2 = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                    )
                )
            }
        }

        item {
            SectionTitle("Checkboxes")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Minimal checkbox")

                Checkbox(
                    checked = isCheckboxChecked,
                    onCheckedChange = { isCheckboxChecked = it }
                )
            }

            Text(if (isCheckboxChecked) "Checkbox is checked" else "Checkbox is unchecked")
        }

        item {
            SectionTitle("Buttons")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}) { Text("Filled Button") }
                OutlinedButton(onClick = {}) { Text("Outlined Button") }
                FilledTonalButton(onClick = {}) { Text("Tonal Button") }
                TextButton(onClick = {}) { Text("Text Button") }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Icon Button"
                    )
                }
            }
        }

        item {
            SectionTitle("Chips")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(onClick = {}, label = { Text("Assist") })
                FilterChip(selected = true, onClick = {}, label = { Text("Filter") })
                SuggestionChip(onClick = {}, label = { Text("Suggestion") })
                InputChip(
                    selected = true,
                    onClick = {},
                    label = { Text("Input") },
                )

                InputChip(
                    onClick = {
                        //onDismiss()
                        isChipEnabled = !isChipEnabled
                    },
                    label = { Text("Some text") },
                    selected = isChipEnabled,
                    avatar = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Localized description",
                            Modifier.size(InputChipDefaults.AvatarSize)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Localized description",
                            Modifier.size(InputChipDefaults.AvatarSize)
                        )
                    },
                )

                FilterChip(
                    onClick = { isChipSelected = !isChipSelected },
                    label = {
                        Text("Filter chip")
                    },
                    selected = isChipSelected,
                    leadingIcon = if (isChipSelected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = "Done icon",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }

        item {
            SectionTitle("Cards")
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    modifier = Modifier
                        .size(width = 240.dp, height = 100.dp)
                ) {
                    CardContent("ElevatedCard")
                }

                OutlinedCard(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier
                        .size(width = 240.dp, height = 100.dp)
                ) {
                    CardContent("OutlinedCard")
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    modifier = Modifier
                        .size(width = 240.dp, height = 100.dp)
                ) {
                    CardContent("FilledCard")
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun CardContent(title: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text("This is a Material 3 $title", style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselExample_MultiBrowse() {
    data class CarouselItem(
        val id: Int,
        @DrawableRes val imageResId: Int,
        val contentDescription: String
    )

    val items = remember {
        listOf(
            CarouselItem(0, R.drawable.cupcake, "cupcake"),
            CarouselItem(1, R.drawable.donut, "donut"),
            CarouselItem(2, R.drawable.eclair, "eclair"),
            CarouselItem(3, R.drawable.froyo, "froyo"),
        )
    }

    HorizontalMultiBrowseCarousel(
        state = rememberCarouselState { items.count() },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 16.dp, bottom = 16.dp),
        preferredItemWidth = 186.dp,
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { i ->
        val item = items[i]
        Image(
            modifier = Modifier
                .height(205.dp)
                .maskClip(MaterialTheme.shapes.extraLarge),
            painter = painterResource(id = item.imageResId),
            contentDescription = item.contentDescription,
            contentScale = ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselExample() {
    data class CarouselItem(
        val id: Int,
        @DrawableRes val imageResId: Int,
        val contentDescription: String
    )

    val carouselItems = remember {
        listOf(
            CarouselItem(0, R.drawable.cupcake, "cupcake"),
            CarouselItem(1, R.drawable.donut, "donut"),
            CarouselItem(2, R.drawable.eclair, "eclair"),
            CarouselItem(3, R.drawable.froyo, "froyo"),
        )
    }

    HorizontalUncontainedCarousel(
        state = rememberCarouselState { carouselItems.count() },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 16.dp, bottom = 16.dp),
        itemWidth = 186.dp,
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { i ->
        val item = carouselItems[i]
        Image(
            modifier = Modifier
                .height(205.dp)
                .maskClip(MaterialTheme.shapes.extraLarge),
            painter = painterResource(id = item.imageResId),
            contentDescription = item.contentDescription,
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(showBackground = true, heightDp = 2000)
@Composable
fun PreviewM3ComponentsShowcaseScreen() {
    MaterialTheme {
        M3ComponentsShowcaseScreen()
    }
}