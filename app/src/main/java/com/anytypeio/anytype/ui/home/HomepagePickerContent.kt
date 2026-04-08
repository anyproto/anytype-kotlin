package com.anytypeio.anytype.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingSecondaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.Title3
import com.anytypeio.anytype.presentation.spaces.HomepageType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomepagePickerBottomSheet(
    onHomepageSelected: (HomepageType) -> Unit,
    onLaterClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 8.dp),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        HomepagePickerContent(
            onHomepageSelected = onHomepageSelected,
            onLaterClicked = onLaterClicked
        )
    }
}

@Composable
fun HomepagePickerContent(
    onHomepageSelected: (HomepageType) -> Unit,
    onLaterClicked: () -> Unit
) {
    var selectedType by remember { mutableStateOf(HomepageType.CHAT) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.background_primary),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Dragger(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            text = stringResource(id = R.string.homepage_picker_title),
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            text = stringResource(id = R.string.homepage_picker_description),
            style = Title3,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))

        val types = remember {
            listOf(
                HomepageType.CHAT,
                HomepageType.PAGE,
                HomepageType.COLLECTION,
                HomepageType.EMPTY
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 24.dp,
                alignment = Alignment.CenterHorizontally
            ),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp)
        ) {
            items(types) { type ->
                HomepageOptionCard(
                    type = type,
                    selected = type == selectedType,
                    label = when (type) {
                        HomepageType.CHAT -> stringResource(id = R.string.homepage_picker_chat)
                        HomepageType.PAGE -> stringResource(id = R.string.homepage_picker_page)
                        HomepageType.COLLECTION -> stringResource(id = R.string.homepage_picker_collection)
                        HomepageType.EMPTY -> stringResource(id = R.string.homepage_picker_empty)
                    },
                    onClick = { selectedType = type }
                )
            }
        }

        Spacer(modifier = Modifier.height(31.dp))

        ButtonOnboardingPrimaryLarge(
            onClick = { onHomepageSelected(selectedType) },
            text = stringResource(id = R.string.create),
            size = ButtonSize.Large,
            modifierBox = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            enabled = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        ButtonOnboardingSecondaryLarge(
            onClick = onLaterClicked,
            text = stringResource(id = R.string.later),
            size = ButtonSize.Large,
            modifierBox = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * A selectable card displaying a channel type illustration with a label.
 * Used on the channel creation screen to let users pick a channel type.
 *
 * @param type the channel type determining which illustration to show
 * @param selected whether this card is currently selected (blue accent border/colors)
 * @param label the text label shown below the card
 * @param onClick called when the card is tapped
 */
@Composable
fun HomepageOptionCard(
    type: HomepageType,
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) {
        colorResource(R.color.control_accent_50)
    } else {
        colorResource(R.color.shape_secondary)
    }
    val borderWidth = if (selected) 1.5.dp else 1.dp
    val labelColor =
        if (selected) colorResource(R.color.control_accent_50) else colorResource(R.color.text_secondary)


    Column(
        modifier = modifier.noRippleThrottledClickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp, 176.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
        ) {
            when (type) {
                HomepageType.CHAT -> ChatIllustration(selected = selected)
                HomepageType.PAGE -> PageIllustration(selected = selected)
                HomepageType.COLLECTION -> CollectionIllustration(selected = selected)
                HomepageType.EMPTY -> WidgetsIllustration(selected = selected)
            }
        }
        Spacer(Modifier.height(7.dp))
        androidx.compose.material3.Text(
            text = label,
            style = Caption1Medium,
            color = labelColor,
            textAlign = TextAlign.Center
        )
    }
}

// --- Illustration colors ---

private data class IllustrationColors(
    val primary: Color,
    val secondary: Color,
    val widgetBg: Color
)

@Composable
private fun illustrationColors(selected: Boolean): IllustrationColors {
    return if (selected) {
        IllustrationColors(
            primary = colorResource(R.color.control_accent_50),
            secondary = colorResource(R.color.control_accent_25),
            widgetBg = colorResource(R.color.control_accent_25).copy(alpha = 0.5f)
        )
    } else {
        IllustrationColors(
            primary = colorResource(R.color.control_tertiary),
            secondary = colorResource(R.color.shape_secondary),
            widgetBg = colorResource(R.color.shape_transparent_tertiary)
        )
    }
}

// --- Shape helper ---

@Composable
private fun Pill(
    x: Dp, y: Dp, w: Dp, h: Dp,
    color: Color,
    cornerRadius: Dp = 2.dp
) {
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(w, h)
            .clip(RoundedCornerShape(cornerRadius))
            .background(color)
    )
}

private val illustrationTitleStyle = TextStyle(
    fontSize = 15.sp,
    fontWeight = FontWeight.W500,
    lineHeight = 20.sp
)

// --- Chat Illustration ---

@Composable
private fun ChatIllustration(selected: Boolean) {
    val c = illustrationColors(selected)
    Box(Modifier.fillMaxSize()) {
        // Top-right bubble
        Pill(40.dp, 20.dp, 40.dp, 12.dp, c.primary, 8.dp)
        // Left text line
        Pill(24.dp, 36.dp, 48.dp, 12.dp, c.secondary, 6.dp)
        // Left large block
        Pill(24.dp, 52.dp, 40.dp, 44.dp, c.secondary, 6.dp)
        // Avatar 1
        Box(Modifier
            .offset(8.dp, 84.dp)
            .size(12.dp)
            .clip(CircleShape)
            .background(c.primary))
        // Mid bar
        Pill(32.dp, 100.dp, 48.dp, 12.dp, c.primary, 6.dp)
        // Bottom-right block
        Pill(40.dp, 116.dp, 40.dp, 32.dp, c.primary, 6.dp)
        // Avatar 2
        Box(Modifier
            .offset(8.dp, 152.dp)
            .size(12.dp)
            .clip(CircleShape)
            .background(c.primary))
        // Bottom-left text
        Pill(24.dp, 152.dp, 40.dp, 12.dp, c.secondary, 6.dp)
    }
}

// --- Widgets Illustration ---

@Composable
private fun WidgetsIllustration(selected: Boolean) {
    val c = illustrationColors(selected)
    Box(Modifier.fillMaxSize()) {
        // Top widget block background
        Pill(6.dp, 20.dp, 76.dp, 52.dp, c.widgetBg, 8.dp)
        // Top block title
        Pill(12.dp, 25.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(22.dp, 26.dp, 22.dp, 4.dp, c.primary, 3.dp)
        // Row 1
        Pill(12.dp, 37.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(22.dp, 38.dp, 54.dp, 4.dp, c.primary, 2.dp)
        // Row 2
        Pill(12.dp, 49.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(22.dp, 50.dp, 36.dp, 4.dp, c.primary, 2.dp)
        // Row 3
        Pill(12.dp, 61.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(22.dp, 62.dp, 54.dp, 4.dp, c.primary, 2.dp)

        // Section header
        Pill(6.dp, 78.dp, 76.dp, 16.dp, c.widgetBg, 8.dp)
        Pill(12.dp, 83.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(22.dp, 84.dp, 36.dp, 4.dp, c.primary, 3.dp)

        // Bottom widget block background
        Pill(6.dp, 100.dp, 76.dp, 64.dp, c.widgetBg, 8.dp)
        // Bottom block title
        Pill(12.dp, 105.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(22.dp, 106.dp, 22.dp, 4.dp, c.primary, 3.dp)
        // Row 1
        Pill(12.dp, 117.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(22.dp, 118.dp, 54.dp, 4.dp, c.primary, 2.dp)
        // Row 2
        Pill(12.dp, 129.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(22.dp, 130.dp, 36.dp, 4.dp, c.primary, 2.dp)
        // Row 3
        Pill(12.dp, 141.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(22.dp, 142.dp, 54.dp, 4.dp, c.primary, 2.dp)
        // Row 4
        Pill(12.dp, 153.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(22.dp, 154.dp, 36.dp, 4.dp, c.primary, 2.dp)
    }
}

// --- Page Illustration ---

@Composable
private fun PageIllustration(selected: Boolean) {
    val c = illustrationColors(selected)
    Box(Modifier.fillMaxSize()) {
        // Header area (active only)
        if (selected) {
            Pill(2.dp, 2.dp, 84.dp, 38.dp, c.secondary)
        }
        // Document icon placeholder
        Pill(10.dp, 24.dp, 18.dp, 22.dp, c.primary.copy(alpha = 0.7f), 3.dp)
        Pill(22.dp, 24.dp, 6.dp, 6.dp, c.secondary, 1.dp)
        // Title
        androidx.compose.material3.Text(
            text = "Idea",
            modifier = Modifier.offset(11.dp, 53.dp),
            style = illustrationTitleStyle,
            color = c.primary
        )
        // Paragraph 1
        Pill(10.dp, 80.dp, 68.dp, 4.dp, c.primary, 2.dp)
        Pill(10.dp, 90.dp, 68.dp, 4.dp, c.primary, 2.dp)
        Pill(10.dp, 100.dp, 68.dp, 4.dp, c.primary, 2.dp)
        Pill(10.dp, 110.dp, 51.dp, 4.dp, c.primary, 2.dp)
        // Paragraph 2
        Pill(10.dp, 126.dp, 68.dp, 4.dp, c.primary, 2.dp)
        Pill(10.dp, 136.dp, 68.dp, 4.dp, c.primary, 2.dp)
        Pill(10.dp, 146.dp, 68.dp, 4.dp, c.primary, 2.dp)
        Pill(10.dp, 156.dp, 34.dp, 4.dp, c.primary, 2.dp)
    }
}

// --- Collection Illustration ---

@Composable
private fun CollectionIllustration(selected: Boolean) {
    val c = illustrationColors(selected)
    val badgeColor = if (selected) colorResource(R.color.control_accent) else c.primary

    Box(Modifier.fillMaxSize()) {
        // Title
        androidx.compose.material3.Text(
            text = "Tasks",
            modifier = Modifier.offset(11.dp, 27.dp),
            style = illustrationTitleStyle,
            color = c.primary
        )
        // Title underline
        Pill(10.dp, 54.dp, 32.dp, 4.dp, c.primary, 2.dp)
        // Badge
        Pill(58.dp, 51.dp, 20.dp, 10.dp, badgeColor, 5.dp)
        // Row 1
        Pill(10.dp, 71.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(20.dp, 72.dp, 58.dp, 4.dp, c.primary, 2.dp)
        Pill(10.dp, 83.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(20.dp, 84.dp, 43.dp, 4.dp, c.primary, 2.dp)
        // Row 2
        Pill(10.dp, 95.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(20.dp, 96.dp, 58.dp, 4.dp, c.primary, 2.dp)
        Pill(10.dp, 107.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(20.dp, 108.dp, 28.dp, 4.dp, c.primary, 2.dp)
        // Row 3
        Pill(10.dp, 119.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(20.dp, 120.dp, 58.dp, 4.dp, c.primary, 2.dp)
        Pill(10.dp, 131.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(20.dp, 132.dp, 43.dp, 4.dp, c.primary, 2.dp)
        // Row 4
        Pill(10.dp, 143.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(20.dp, 144.dp, 58.dp, 4.dp, c.primary, 2.dp)
        Pill(10.dp, 155.dp, 6.dp, 6.dp, c.primary, 3.dp)
        Pill(20.dp, 156.dp, 28.dp, 4.dp, c.primary, 2.dp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@DefaultPreviews
@Composable
fun HomepagePickerBottomSheetPreview() {
    HomepagePickerBottomSheet(
        onHomepageSelected = {},
        onLaterClicked = {},
        onDismiss = {}
    )
}
