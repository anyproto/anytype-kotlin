package com.anytypeio.anytype.ui_settings.space

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.presentation.settings.SpacesStorageViewModel
import com.anytypeio.anytype.presentation.settings.SpacesStorageViewModel.SpacesStorageScreenState
import com.anytypeio.anytype.presentation.settings.SpacesStorageViewModel.SegmentLegendItem.Active
import com.anytypeio.anytype.presentation.settings.SpacesStorageViewModel.SegmentLegendItem.Free
import com.anytypeio.anytype.presentation.settings.SpacesStorageViewModel.SegmentLegendItem.Other
import com.anytypeio.anytype.ui_settings.R

@Composable
fun SpaceStorageScreen(
    data: SpacesStorageScreenState?,
    onManageFilesClicked: () -> Unit,
    onGetMoreSpaceClicked: () -> Unit
) {
    data?.let { currentData ->
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = colorResource(id = R.color.background_secondary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp),
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Dragger()
                }
                Header(text = stringResource(id = R.string.remote_storage))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.you_can_store, data.spaceLimit),
                    modifier = Modifier.fillMaxWidth(),
                    color = colorResource(R.color.text_primary),
                    style = BodyCalloutRegular
                )
                if (data.isShowGetMoreSpace) {
                    Text(
                        text = stringResource(id = R.string.get_more_space),
                        color = colorResource(R.color.palette_system_red),
                        style = BodyCalloutMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .clickable { onGetMoreSpaceClicked() }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(
                        id = R.string.space_usage,
                        data.spaceUsage,
                        data.spaceLimit
                    ),
                    style = Relations3,
                    color = if (data.isShowSpaceUsedWarning) {
                        colorResource(id = R.color.palette_system_red)
                    } else {
                        colorResource(id = R.color.text_secondary)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                SegmentLine(items = currentData.segmentLineItems)
                Spacer(modifier = Modifier.height(16.dp))
                SegmentLegend(items = currentData.segmentLegendItems)
                ButtonSecondary(
                    text = stringResource(id = R.string.manage_files),
                    onClick = onManageFilesClicked,
                    size = ButtonSize.SmallSecondary.apply {
                        contentPadding = PaddingValues(12.dp, 7.dp, 12.dp, 7.dp)
                    }
                )
                Spacer(modifier = Modifier.height(44.dp))
            }
        }
    }
}

@Composable
private fun Header(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(top = 12.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = Title1,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Composable
private fun SegmentLegend(
    items: List<SpacesStorageViewModel.SegmentLegendItem>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (color, text) = when (item) {
                    is Active -> {
                        colorResource(id = R.color.palette_system_amber_125) to "${item.name} | ${item.usage}"
                    }

                    is Free -> {
                        colorResource(id = R.color.shape_tertiary) to "Free | ${item.legend}"
                    }

                    is Other -> {
                        colorResource(id = R.color.palette_system_amber_50) to "Other spaces | ${item.legend}"
                    }
                }
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    modifier = Modifier
                        .padding(start = 10.dp),
                    text = text,
                    style = Caption1Medium,
                    color = colorResource(id = R.color.text_primary)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview
@Composable
fun PreviewSpaceStorageScreen() {
    SpaceStorageScreen(data = SpacesStorageScreenState(
        spaceLimit = "sociosqu",
        spaceUsage = "error",
        isShowGetMoreSpace = false,
        isShowSpaceUsedWarning = false,
        segmentLegendItems = listOf(),
        segmentLineItems = listOf()
    ), onManageFilesClicked = { /*TODO*/ }) {
    }
}