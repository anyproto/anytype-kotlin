package com.anytypeio.anytype.feature_object_type.ui.header

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.common.OldDevicesPreview
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.UiFieldsButtonState
import com.anytypeio.anytype.feature_object_type.ui.UiLayoutButtonState
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesButtonState

@Composable
fun HorizontalButtons(
    modifier: Modifier,
    uiLayoutButtonState: UiLayoutButtonState,
    uiFieldsButtonState: UiFieldsButtonState,
    uiTemplatesButtonState: UiTemplatesButtonState = UiTemplatesButtonState.Hidden,
    onTypeEvent: (TypeEvent) -> Unit
) {
    val horizontalScrollState = rememberScrollState()

    Row(
        modifier = modifier.horizontalScroll(state = horizontalScrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val modifierButton = Modifier
            .height(40.dp)
            .wrapContentWidth()
            .border(
                width = 1.dp,
                color = colorResource(R.color.shape_primary),
                shape = RoundedCornerShape(size = 8.dp)
            )

        if (uiFieldsButtonState is UiFieldsButtonState.Visible) {
            Row(
                modifier = modifierButton.noRippleThrottledClickable {
                    onTypeEvent(TypeEvent.OnFieldsButtonClick)
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(start = 12.dp),
                    text = stringResource(R.string.button_fields),
                    style = PreviewTitle2Medium,
                    color = colorResource(R.color.text_primary)
                )
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(start = 6.dp, end = 12.dp),
                    text = uiFieldsButtonState.count.toString(),
                    style = PreviewTitle2Medium,
                    color = colorResource(R.color.glyph_active)
                )
            }
        }

        if (uiLayoutButtonState is UiLayoutButtonState.Visible) {
            Row(
                modifier = modifierButton.noRippleThrottledClickable {
                    onTypeEvent(TypeEvent.OnLayoutButtonClick)
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(start = 12.dp),
                    text = stringResource(R.string.button_layout),
                    style = PreviewTitle2Medium,
                    color = colorResource(R.color.text_primary)
                )
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(start = 6.dp, end = 12.dp),
                    text = uiLayoutButtonState.layout.name.substring(0, 1).uppercase()
                            + uiLayoutButtonState.layout.name.substring(1)
                        .toLowerCase(Locale.current),
                    style = PreviewTitle2Medium,
                    color = colorResource(R.color.glyph_active)
                )
            }
        }

        if (uiTemplatesButtonState is UiTemplatesButtonState.Visible) {
            Row(
                modifier = modifierButton.noRippleThrottledClickable {
                    onTypeEvent(TypeEvent.OnTemplatesButtonClick)
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(start = 12.dp),
                    text = stringResource(R.string.button_templates),
                    style = PreviewTitle2Medium,
                    color = colorResource(R.color.text_primary)
                )
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(start = 6.dp, end = 12.dp),
                    text = uiTemplatesButtonState.count.toString(),
                    style = PreviewTitle2Medium,
                    color = colorResource(R.color.glyph_active)
                )
            }
        }
    }
}

@DefaultPreviews
@Composable
fun HorizontalButtonsPreview() {
    HorizontalButtons(
        modifier = Modifier
            .height(32.dp)
            .fillMaxWidth()
            .padding(start = 20.dp),
        uiLayoutButtonState = UiLayoutButtonState.Visible(ObjectType.Layout.BASIC),
        uiFieldsButtonState = UiFieldsButtonState.Visible(3),
        uiTemplatesButtonState = UiTemplatesButtonState.Visible(2),
        onTypeEvent = {}
    )
}
