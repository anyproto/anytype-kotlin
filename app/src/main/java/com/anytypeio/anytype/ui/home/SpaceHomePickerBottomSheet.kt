package com.anytypeio.anytype.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_ui.widgets.SearchField
import com.anytypeio.anytype.presentation.home.SpaceHomePickerItem
import com.anytypeio.anytype.presentation.home.SpaceHomePickerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceHomePickerBottomSheet(
    state: SpaceHomePickerState.Visible,
    onNoHomeClicked: () -> Unit,
    onObjectClicked: (String) -> Unit,
    onQueryChanged: (String) -> Unit,
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
            onObjectClicked = onObjectClicked,
            onQueryChanged = onQueryChanged
        )
    }
}

@Composable
private fun SpaceHomePickerContent(
    state: SpaceHomePickerState.Visible,
    onNoHomeClicked: () -> Unit,
    onObjectClicked: (String) -> Unit,
    onQueryChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Dragger(modifier = Modifier.padding(vertical = 6.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            text = stringResource(id = R.string.space_home_row_title),
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )
        SearchField(
            query = state.query,
            onQueryChanged = onQueryChanged,
            onFocused = {}
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item(key = "no-home") {
                NoHomeRow(
                    onClick = onNoHomeClicked,
                    isSelected = state.currentHomepage == null
                )
                com.anytypeio.anytype.core_ui.foundation.Divider()
            }
            item(
                key = "section-object"
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 16.dp, top = 22.dp, bottom = 10.dp),
                    text = stringResource(id = R.string.objects),
                    style = Relations2,
                    color = colorResource(id = R.color.text_secondary)
                )
            }
            items(items = state.candidates, key = { it.objectId }) { item ->
                SpaceHomePickerRow(
                    item = item,
                    isSelected = state.currentHomepage == item.objectId,
                    onClick = { onObjectClicked(item.objectId) }
                )
                com.anytypeio.anytype.core_ui.foundation.Divider()
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
private fun NoHomeRow(
    onClick: () -> Unit,
    isSelected: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .noRippleThrottledClickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(48.dp),
            painter = painterResource(id = R.drawable.ic_remove_32),
            contentDescription = null,
            contentScale = ContentScale.Inside
        )
        Spacer(modifier = Modifier.padding(start = 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = R.string.space_home_no_home_title),
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(id = R.string.space_home_no_home_subtitle),
                style = Caption1Regular,
                color = colorResource(id = R.color.text_secondary)
            )
        }
        if (isSelected) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_checkbox_checked),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun SpaceHomePickerRow(
    item: SpaceHomePickerItem,
    isSelected: Boolean,
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = item.name,
                style = PreviewTitle2Medium,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = item.type,
                style = Relations2,
                color = colorResource(id = R.color.text_secondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isSelected) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_checkbox_checked),
                contentDescription = null
            )
        }
    }
}

@DefaultPreviews
@Composable
private fun SpaceHomePickerContentPreview() {
    SpaceHomePickerContent(
        state = SpaceHomePickerState.Visible(
            query = "",
            candidates = listOf(
                SpaceHomePickerItem(
                    objectId = "obj-1",
                    name = "Daily Journal",
                    icon = ObjectIcon.SimpleIcon("star", R.color.text_primary),
                    type = "Page"
                ),
                SpaceHomePickerItem(
                    objectId = "obj-2",
                    name = "Ideas",
                    icon = ObjectIcon.SimpleIcon("star", R.color.text_primary),
                    type = "Page"
                ),
                SpaceHomePickerItem(
                    objectId = "obj-3",
                    name = "Product Roadmap",
                    icon = ObjectIcon.SimpleIcon("star", R.color.text_primary),
                    type = "Page"
                )
            ),
            currentHomepage = "obj-1",
            isLoading = false
        ),
        onNoHomeClicked = {},
        onObjectClicked = {},
        onQueryChanged = {}
    )
}

@DefaultPreviews
@Composable
private fun SpaceHomePickerContentEmptyPreview() {
    SpaceHomePickerContent(
        state = SpaceHomePickerState.Visible(
            query = "",
            candidates = emptyList(),
            currentHomepage = null,
            isLoading = false
        ),
        onNoHomeClicked = {},
        onObjectClicked = {},
        onQueryChanged = {}
    )
}
