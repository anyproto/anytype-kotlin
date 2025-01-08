package com.anytypeio.anytype.feature_object_type.ui.objects

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.anytypeio.anytype.feature_object_type.viewmodel.UiObjectsAddIconState
import com.anytypeio.anytype.feature_object_type.viewmodel.UiObjectsHeaderState
import com.anytypeio.anytype.feature_object_type.viewmodel.UiObjectsSettingsIconState

@Composable
fun ObjectsHeader(
    modifier: Modifier,
    uiObjectsHeaderState: UiObjectsHeaderState,
    uiObjectsAddIconState: UiObjectsAddIconState,
    uiObjectsSettingsIconState: UiObjectsSettingsIconState,
    onTypeEvent: (TypeEvent) -> Unit
) {
    Box(
        modifier = modifier.padding(start = 20.dp),
    ) {
        Row(
            modifier = Modifier.matchParentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically),
                text = stringResource(R.string.objects),
                style = BodyBold,
                color = colorResource(R.color.text_primary)
            )
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(start = 8.dp),
                text = uiObjectsHeaderState.count,
                style = PreviewTitle1Regular,
                color = colorResource(R.color.text_secondary)
            )
        }
        Row(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            if (uiObjectsSettingsIconState is UiObjectsSettingsIconState.Visible) {
                Image(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .size(24.dp)
                        .noRippleThrottledClickable {
                            onTypeEvent(OnTemplatesAddIconClick)
                        },
                    painter = painterResource(R.drawable.ic_space_list_dots),
                    contentDescription = "Settings"
                )
            }
            if (uiObjectsAddIconState is UiObjectsAddIconState.Visible) {
                Image(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 20.dp)
                        .size(24.dp)
                        .noRippleThrottledClickable {
                            onTypeEvent(OnTemplatesAddIconClick)
                        },
                    painter = painterResource(R.drawable.ic_default_plus),
                    contentDescription = "Add"
                )
            }
        }
    }
}

@DefaultPreviews
@Composable
fun ObjectsHeaderPreview() {
    ObjectsHeader(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        uiObjectsHeaderState = UiObjectsHeaderState("3"),
        uiObjectsAddIconState = UiObjectsAddIconState.Visible,
        uiObjectsSettingsIconState = UiObjectsSettingsIconState.Visible,
        onTypeEvent = {}
    )
}