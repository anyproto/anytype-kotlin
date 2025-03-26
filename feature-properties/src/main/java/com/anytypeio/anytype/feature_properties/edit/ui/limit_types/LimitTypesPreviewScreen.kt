package com.anytypeio.anytype.feature_properties.edit.ui.limit_types

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.feature_properties.R
import com.anytypeio.anytype.feature_properties.add.ui.commonItemModifier
import com.anytypeio.anytype.feature_properties.edit.UiPropertyLimitTypeItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyLimitTypesPreviewScreen(
    items: List<UiPropertyLimitTypeItem>,
    onDismissRequest: () -> Unit,
) {
    val currentItems = remember {
        mutableStateListOf<UiPropertyLimitTypeItem>().apply { addAll(items) }
    }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val lazyListState = rememberLazyListState()

    ModalBottomSheet(
        modifier = Modifier
            .padding(top = 60.dp)
            .fillMaxWidth(),
        onDismissRequest = onDismissRequest,
        dragHandle = null,
        scrimColor = colorResource(id = R.color.modal_screen_outside_background),
        containerColor = colorResource(id = R.color.background_primary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetState = bottomSheetState,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Dragger(
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .align(Alignment.TopCenter)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.property_limit_objects_title),
                        style = Title1,
                        color = colorResource(R.color.text_primary),
                        textAlign = TextAlign.Center
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .padding(top = 64.dp)
                        .fillMaxWidth()
                        .nestedScroll(rememberNestedScrollInteropConnection()),
                    state = lazyListState
                ) {
                    items(
                        count = currentItems.size,
                        key = { index -> currentItems[index].id },
                        itemContent = { index ->
                            val item = currentItems[index]
                            TypeItem(
                                modifier = commonItemModifier(),
                                item = item
                            )
                        }
                    )
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        },
    )
}

@Composable
private fun TypeItem(
    modifier: Modifier,
    item: UiPropertyLimitTypeItem
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 20.dp)
            .fillMaxHeight(),
        verticalAlignment = CenterVertically,
    ) {
        ListWidgetObjectIcon(
            modifier = Modifier,
            icon = item.icon!!,
            iconSize = 24.dp
        )

        Spacer(modifier = Modifier.size(10.dp))

        Text(
            modifier = Modifier,
            text = item.name,
            style = PreviewTitle1Regular,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}