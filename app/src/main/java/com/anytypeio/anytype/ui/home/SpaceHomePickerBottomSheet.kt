package com.anytypeio.anytype.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.home.SpaceHomePickerItem
import com.anytypeio.anytype.presentation.home.SpaceHomePickerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceHomePickerBottomSheet(
    state: SpaceHomePickerState.Visible,
    onNoHomeClicked: () -> Unit,
    onObjectClicked: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        SpaceHomePickerContent(
            state = state,
            onNoHomeClicked = onNoHomeClicked,
            onObjectClicked = onObjectClicked
        )
    }
}

@Composable
private fun SpaceHomePickerContent(
    state: SpaceHomePickerState.Visible,
    onNoHomeClicked: () -> Unit,
    onObjectClicked: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
    ) {
        Dragger(modifier = Modifier.padding(vertical = 6.dp))
        androidx.compose.material3.Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            text = stringResource(id = R.string.space_home_row_title),
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item(key = "no-home") {
                NoHomeRow(onClick = onNoHomeClicked)
                Divider(color = colorResource(id = R.color.shape_primary))
            }
            items(items = state.candidates, key = { it.objectId }) { item ->
                SpaceHomePickerRow(
                    item = item,
                    onClick = { onObjectClicked(item.objectId) }
                )
            }
            if (state.isLoading && state.candidates.isEmpty()) {
                item(key = "loading") {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun NoHomeRow(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleThrottledClickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        androidx.compose.material3.Text(
            text = stringResource(id = R.string.space_home_no_home_title),
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary)
        )
        Spacer(modifier = Modifier.height(2.dp))
        androidx.compose.material3.Text(
            text = stringResource(id = R.string.space_home_no_home_subtitle),
            style = Caption1Regular,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}

@Composable
private fun SpaceHomePickerRow(
    item: SpaceHomePickerItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .noRippleThrottledClickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ListWidgetObjectIcon(
            modifier = Modifier,
            iconSize = 48.dp,
            icon = item.icon
        )
        Spacer(modifier = Modifier.padding(start = 12.dp))
        Column {
            androidx.compose.material3.Text(
                modifier = Modifier.fillMaxWidth(),
                text = item.name,
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
             Spacer(modifier = Modifier.height(2.dp))
            androidx.compose.material3.Text(
                modifier = Modifier.fillMaxWidth(),
                text = item.name,
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

