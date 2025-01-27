package com.anytypeio.anytype.feature_object_type.ui.templates

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.widgets.TemplateItemContent
import com.anytypeio.anytype.feature_object_type.viewmodel.UiTemplatesListState
import com.anytypeio.anytype.presentation.templates.TemplateView
import timber.log.Timber

@Composable
fun TemplatesList(uiTemplatesListState: UiTemplatesListState) {

    Timber.d("TemplatesList :$uiTemplatesListState")

    val scrollState = rememberLazyListState()

    LazyRow(
        state = scrollState,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(
            count = uiTemplatesListState.items.size,
            key = { index ->
                val item = uiTemplatesListState.items[index]
                when (item) {
                    is TemplateView.Blank -> item.id
                    is TemplateView.New -> "new"
                    is TemplateView.Template -> item.id
                }
            },
            itemContent = {
                val item = uiTemplatesListState.items[it]
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = colorResource(id = R.color.shape_secondary),
                            shape = RoundedCornerShape(size = 16.dp)
                        )
                        .height(224.dp)
                        .width(120.dp)
                        .clickable {
                            //action(TemplateClick(item))
                        }
                ) {
                    TemplateItemContent(item)
                }
                when (item) {
                    is TemplateView.Blank -> item.id
                    is TemplateView.New -> "new"
                    is TemplateView.Template -> item.id
                }
            }
        )
    }
}