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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi.Action.Dismiss
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi.State.ImagePreview

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ViewerLayoutCoverWidget(
    uiState: ViewerLayoutWidgetUi,
    action: (ViewerLayoutWidgetUi.Action) -> Unit,
) {

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (uiState.showCoverMenu) {
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
                Content(
                    uiState = uiState,
                    action = action
                )
            }
        )
    }
}

@Composable
private fun ColumnScope.Content(
    uiState: ViewerLayoutWidgetUi,
    action: (ViewerLayoutWidgetUi.Action) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        text = stringResource(R.string.view_layout_cover_widget_title),
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
            count = uiState.imagePreviewItems.size,
            key = { index -> uiState.imagePreviewItems[index].relationKey.key }
        ) { idx ->
            val item = uiState.imagePreviewItems[idx]
            val title = item.getTitle()
            val iconDrawableRes = when (item) {
                is ImagePreview.None -> null
                is ImagePreview.PageCover -> null
                is ImagePreview.Custom -> R.drawable.ic_relation_attachment_24
            }
            CoverItem(
                text = title,
                checked = item.isChecked,
                iconDrawableRes = iconDrawableRes
            ) {
                action(ViewerLayoutWidgetUi.Action.ImagePreviewUpdate(item))
            }
        }
    }
}

@Composable
private fun CoverItem(
    text: String,
    checked: Boolean,
    @DrawableRes iconDrawableRes: Int? = null,
    action: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp)
            .height(58.dp)
            .noRippleThrottledClickable { action() }
    ) {
        if (iconDrawableRes != null) {
            Image(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterStart),
                painter = painterResource(id = iconDrawableRes),
                contentDescription = "File relation icon"
            )
            Text(
                modifier = Modifier
                    .padding(start = 34.dp)
                    .align(Alignment.CenterStart),
                text = text,
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
        } else {
            Text(
                modifier = Modifier.align(Alignment.CenterStart),
                text = text,
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
        }
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

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun PreviewLayoutCoverWidget() {
    ViewerLayoutWidget(
        uiState = ViewerLayoutWidgetUi(
            showWidget = true,
            layoutType = DVViewerType.GRID,
            withIcon = ViewerLayoutWidgetUi.State.Toggle.WithIcon(
                toggled = false
            ),
            fitImage = ViewerLayoutWidgetUi.State.Toggle.FitImage(
                toggled = false
            ),
            cardSize = ViewerLayoutWidgetUi.State.CardSize.Small,
            showCardSize = true,
            viewer = "",
            showCoverMenu = true,
            imagePreviewItems = listOf(
                ImagePreview.None(isChecked = false),
                ImagePreview.PageCover(isChecked = true),
                ImagePreview.Custom(
                    relationKey = RelationKey(Relations.IDENTITY),
                    isChecked = false,
                    name = "Some File Relation"
                )
            )
        ),
        action = {}
    )
}