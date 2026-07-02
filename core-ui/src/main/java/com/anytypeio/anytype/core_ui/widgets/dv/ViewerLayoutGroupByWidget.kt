package com.anytypeio.anytype.core_ui.widgets.dv

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi.Action.Dismiss

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ViewerLayoutGroupByWidget(
    uiState: ViewerLayoutWidgetUi,
    action: (ViewerLayoutWidgetUi.Action) -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (uiState.showGroupByMenu) {
        ModalBottomSheet(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.ime)
                .fillMaxWidth()
                .wrapContentHeight(),
            scrimColor = colorResource(id = R.color.modal_screen_outside_background),
            containerColor = colorResource(id = R.color.background_secondary),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            onDismissRequest = { action(Dismiss) },
            sheetState = bottomSheetState,
            dragHandle = { DragHandle() },
            content = {
                GroupByContent(uiState = uiState, action = action)
            }
        )
    }
}

@Composable
private fun ColumnScope.GroupByContent(
    uiState: ViewerLayoutWidgetUi,
    action: (ViewerLayoutWidgetUi.Action) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        text = stringResource(R.string.view_layout_group_by_widget_title),
        style = Title1,
        color = colorResource(R.color.text_primary)
    )
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 250.dp)
    ) {
        items(
            count = uiState.groupByItems.size,
            key = { index -> uiState.groupByItems[index].relationKey.key }
        ) { idx ->
            val item = uiState.groupByItems[idx]
            GroupByItem(
                text = item.name,
                iconDrawableRes = item.format.simpleIcon(),
                checked = item.isChecked
            ) {
                action(ViewerLayoutWidgetUi.Action.GroupByUpdate(item))
            }
        }
    }
}

@Composable
private fun GroupByItem(
    text: String,
    @DrawableRes iconDrawableRes: Int,
    checked: Boolean,
    action: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp)
            .height(58.dp)
            .noRippleThrottledClickable { action() }
    ) {
        Image(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterStart),
            painter = painterResource(id = iconDrawableRes),
            contentDescription = "Relation format icon"
        )
        Text(
            modifier = Modifier
                .padding(start = 34.dp)
                .align(Alignment.CenterStart),
            text = text,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary)
        )
        if (checked) {
            Image(
                modifier = Modifier.align(Alignment.CenterEnd),
                painter = painterResource(id = R.drawable.ic_option_checked_black),
                contentDescription = "Checked"
            )
        }
    }
    Divider()
}
