package com.anytypeio.anytype.feature_object_type.ui.header

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.UiIconState
import com.anytypeio.anytype.feature_object_type.ui.UiTitleState
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import timber.log.Timber


@Composable
fun IconAndTitleWidget(
    modifier: Modifier,
    uiIconState: UiIconState,
    uiTitleState: UiTitleState,
    onTypeEvent: (TypeEvent) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.noRippleThrottledClickable {
                if (uiIconState.isEditable) {
                    onTypeEvent.invoke(TypeEvent.OnObjectTypeIconClick)
                } else {
                    Timber.d("IconAndTitleWidget: Icon is not editable")
                }
            }
        ) {
            ListWidgetObjectIcon(
                modifier = Modifier.size(32.dp),
                icon = uiIconState.icon,
                backgroundColor = R.color.amp_transparent
            )
        }
        NameField(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            initialName = uiTitleState.title,
            onTypeEvent = onTypeEvent,
        )
    }
}

@Composable
fun NameField(
    modifier: Modifier,
    initialName: String,
    onTypeEvent: (TypeEvent) -> Unit
) {
    val (text, color) = if (initialName.isEmpty()) {
        stringResource(R.string.untitled) to colorResource(id = R.color.text_tertiary)
    } else {
        initialName to colorResource(id = R.color.text_primary)
    }
    Text(
        text = text,
        style = HeadlineTitle.copy(color = color),
        modifier = modifier
            .padding(start = 12.dp, end = 20.dp)
            .noRippleThrottledClickable {
                onTypeEvent(TypeEvent.OnObjectTypeTitleClick)
            }
    )
}

@DefaultPreviews
@Composable
fun IconAndTitleWidgetPreview() {
    IconAndTitleWidget(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        onTypeEvent = {},
        uiIconState = UiIconState(icon = ObjectIcon.TypeIcon.Default.DEFAULT, isEditable = true),
        uiTitleState = UiTitleState(
            title = "I understand that contributing to this repository will require me to agree with the",
            isEditable = true,
            originalName = "Page"
        )
    )
}

@DefaultPreviews
@Composable
fun IconAndTitleEmptyWidgetPreview() {
    IconAndTitleWidget(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        onTypeEvent = {},
        uiIconState = UiIconState(icon = ObjectIcon.TypeIcon.Default.DEFAULT, isEditable = true),
        uiTitleState = UiTitleState(
            title = "",
            isEditable = true,
            originalName = "Page"
        )
    )
}