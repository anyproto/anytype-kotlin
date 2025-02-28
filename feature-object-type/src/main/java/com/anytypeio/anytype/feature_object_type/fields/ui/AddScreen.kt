package com.anytypeio.anytype.feature_object_type.fields.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.core_ui.foundation.DefaultSearchBar
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.widgets.dv.DragHandle
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent
import com.anytypeio.anytype.feature_object_type.fields.UiAddFieldItem
import com.anytypeio.anytype.feature_object_type.fields.UiAddFieldsScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFieldScreen(
    state: UiAddFieldsScreenState,
    fieldEvent: (FieldEvent) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var isSearchEmpty by remember { mutableStateOf(true) }

    val lazyListState = rememberLazyListState()

    if (state is UiAddFieldsScreenState.Visible) {
        ModalBottomSheet(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .nestedScroll(rememberNestedScrollInteropConnection()),
            dragHandle = { DragHandle() },
            scrimColor = colorResource(id = R.color.modal_screen_outside_background),
            containerColor = colorResource(id = R.color.background_primary),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            sheetState = bottomSheetState,
            onDismissRequest = {
                fieldEvent(FieldEvent.OnAddFieldScreenDismiss)
            },
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                state = lazyListState
            ) {
                item {
                    DefaultSearchBar(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        isSearchEmpty = it.isEmpty()
                        fieldEvent(FieldEvent.OnAddFieldSearchQueryChanged(it))
                    }
                }
                items(
                    count = state.items.size,
                    key = { index -> state.items[index].id },
                    itemContent = { index ->
                        val item = state.items[index]
                        FieldItem(
                            modifier = commonItemModifier()
                                .noRippleThrottledClickable {
                                    if (state.addToHeader) {
                                        fieldEvent(
                                            FieldEvent.OnAddToHeaderFieldClick(
                                                item = item
                                            )
                                        )
                                    } else {
                                        fieldEvent(
                                            FieldEvent.OnAddToSidebarFieldClick(
                                                item = item
                                            )
                                        )
                                    }
                                },
                            item = item
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FieldItem(
    modifier: Modifier,
    item: UiAddFieldItem
) {
    Row(
        modifier = modifier,
        verticalAlignment = CenterVertically
    ) {
        val formatIcon = item.format.simpleIcon()
        if (formatIcon != null) {
            Image(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(24.dp),
                painter = painterResource(id = formatIcon),
                contentDescription = "Relation format icon",
            )
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .padding(end = 16.dp),
            text = item.fieldTitle,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@DefaultPreviews
@Composable
fun PreviewAddFieldScreen() {
    AddFieldScreen(
        state = UiAddFieldsScreenState.Visible(
            items = listOf(
                UiAddFieldItem(
                    id = "1",
                    fieldKey = "key",
                    fieldTitle = "Title",
                    format = RelationFormat.LONG_TEXT
                )
            ),
            addToHeader = true
        ),
        fieldEvent = {}
    )
}