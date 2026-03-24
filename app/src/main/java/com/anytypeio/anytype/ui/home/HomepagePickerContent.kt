package com.anytypeio.anytype.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.Title2
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
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorResource(id = R.color.background_primary),
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
            .padding(bottom = 16.dp)
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
                .padding(horizontal = 20.dp),
            text = stringResource(id = R.string.homepage_picker_title),
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            text = stringResource(id = R.string.homepage_picker_description),
            style = Caption1Regular,
            color = colorResource(id = R.color.text_secondary),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))

        val types = remember {
            listOf(
                HomepageType.CHAT,
                HomepageType.WIDGETS,
                HomepageType.PAGE,
                HomepageType.COLLECTION
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(types) { type ->
                ChannelTypeCard(
                    type = type,
                    isSelected = type == selectedType,
                    onClick = { selectedType = type }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ButtonOnboardingPrimaryLarge(
            onClick = { onHomepageSelected(selectedType) },
            text = stringResource(id = R.string.create),
            size = ButtonSize.Large,
            modifierBox = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            enabled = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .noRippleThrottledClickable { onLaterClicked() }
                .padding(vertical = 12.dp),
            text = stringResource(id = R.string.homepage_picker_later),
            style = BodyRegular,
            color = colorResource(id = R.color.text_secondary),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ChannelTypeCard(
    type: HomepageType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        colorResource(id = R.color.palette_system_blue)
    } else {
        Color.Transparent
    }

    val backgroundColor = if (isSelected) {
        colorResource(id = R.color.shape_transparent_secondary)
    } else {
        colorResource(id = R.color.shape_transparent_primary)
    }

    val title = when (type) {
        HomepageType.CHAT -> stringResource(id = R.string.homepage_picker_chat)
        HomepageType.WIDGETS -> stringResource(id = R.string.homepage_picker_widgets)
        HomepageType.PAGE -> stringResource(id = R.string.homepage_picker_page)
        HomepageType.COLLECTION -> stringResource(id = R.string.homepage_picker_collection)
    }

    Column(
        modifier = Modifier
            .width(140.dp)
            .noRippleThrottledClickable { onClick() }
            .border(
                border = BorderStroke(2.dp, borderColor),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Placeholder illustration area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .background(
                    color = colorResource(id = R.color.shape_transparent_secondary),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (type) {
                    HomepageType.CHAT -> "\uD83D\uDCAC"
                    HomepageType.WIDGETS -> "\uD83D\uDCCA"
                    HomepageType.PAGE -> "\uD83D\uDCC4"
                    HomepageType.COLLECTION -> "\uD83D\uDDC2"
                },
                style = HeadlineHeading
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = Title2,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )
    }
}
