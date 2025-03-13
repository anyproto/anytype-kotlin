package com.anytypeio.anytype.feature_properties.edit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.widgets.dv.DragHandle
import com.anytypeio.anytype.feature_properties.edit.UiPropertyFormatsListState
import com.anytypeio.anytype.feature_properties.R
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesItem
import com.anytypeio.anytype.feature_properties.add.ui.PropertyTypeItem
import com.anytypeio.anytype.feature_properties.add.ui.commonItemModifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyFormatsListScreen(
    uiState: UiPropertyFormatsListState.Visible,
    onDismissRequest: () -> Unit,
    onFormatClick: (UiEditTypePropertiesItem.Format) -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val lazyListState = rememberLazyListState()

    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = onDismissRequest,
        dragHandle = { DragHandle() },
        scrimColor = colorResource(id = R.color.modal_screen_outside_background),
        containerColor = colorResource(id = R.color.background_primary),
        shape = RoundedCornerShape(16.dp),
        sheetState = bottomSheetState,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.property_select_format_title),
                style = Title1,
                color = colorResource(R.color.text_primary),
                textAlign = TextAlign.Center
            )
        }

        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

        LazyColumn(
            modifier = Modifier
                .background(color = colorResource(id = R.color.background_primary)),
            state = lazyListState
        ) {
            items(
                count = uiState.items.size,
                key = { index -> uiState.items[index].id },
                itemContent = { index ->
                    val item = uiState.items[index]
                    PropertyTypeItem(
                        modifier = commonItemModifier()
                            .clickable {
                                onFormatClick(item)
                            },
                        item = item
                    )
                }
            )
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}