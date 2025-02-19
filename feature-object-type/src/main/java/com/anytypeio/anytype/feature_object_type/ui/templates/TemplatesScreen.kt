package com.anytypeio.anytype.feature_object_type.ui.templates

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent.OnTemplatesAddIconClick
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesAddIconState
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesHeaderState
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesListState


@Composable
fun LazyItemScope.TemplatesScreen(
    uiTemplatesHeaderState: UiTemplatesHeaderState.Visible,
    uiTemplatesAddIconState: UiTemplatesAddIconState,
    uiTemplatesListState: UiTemplatesListState,
    onTypeEvent: (TypeEvent) -> Unit
) {
    Spacer(
        modifier = Modifier.height(44.dp)
    )
    TemplatesHeader(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        uiTemplatesHeaderState = uiTemplatesHeaderState,
        uiTemplatesAddIconState = uiTemplatesAddIconState,
        onTypeEvent = onTypeEvent
    )
    Spacer(
        modifier = Modifier.height(12.dp)
    )
    TemplatesList(
        uiTemplatesListState = uiTemplatesListState,
        onTypeEvent = onTypeEvent
    )
    Spacer(
        modifier = Modifier.height(32.dp)
    )
}

@Composable
fun TemplatesHeader(
    modifier: Modifier,
    uiTemplatesHeaderState: UiTemplatesHeaderState.Visible,
    uiTemplatesAddIconState: UiTemplatesAddIconState,
    onTypeEvent: (TypeEvent) -> Unit
) {
    Box(
        modifier = modifier.padding(horizontal = 20.dp),
    ) {
        Row(modifier = Modifier.wrapContentWidth().align(Alignment.CenterStart)) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically),
                text = stringResource(R.string.templates),
                style = BodyBold,
                color = colorResource(R.color.text_primary)
            )
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(start = 8.dp),
                text = uiTemplatesHeaderState.count,
                style = PreviewTitle1Regular,
                color = colorResource(R.color.text_secondary)
            )
        }
        if (uiTemplatesAddIconState is UiTemplatesAddIconState.Visible) {
            Image(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterEnd)
                    .noRippleThrottledClickable {
                        onTypeEvent(OnTemplatesAddIconClick)
                    },
                painter = painterResource(R.drawable.ic_default_plus),
                contentDescription = "Add",
                contentScale = ContentScale.Inside
            )
        }
    }
}

@DefaultPreviews
@Composable
fun TemplatesHeaderPreview() {
    TemplatesHeader(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        uiTemplatesHeaderState = UiTemplatesHeaderState.Visible(
            count = "2"
        ),
        uiTemplatesAddIconState = UiTemplatesAddIconState.Visible,
    ) { }
}