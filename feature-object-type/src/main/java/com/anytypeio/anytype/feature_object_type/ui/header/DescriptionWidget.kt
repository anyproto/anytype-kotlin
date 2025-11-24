package com.anytypeio.anytype.feature_object_type.ui.header

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.feature_object_type.ui.UiDescriptionState

/**
 * Description widget for ObjectType screen.
 * Displays an editable description field styled as Relations1 (15sp, Inter Regular).
 * Shows placeholder text when empty.
 */
@Composable
fun DescriptionWidget(
    modifier: Modifier = Modifier,
    uiDescriptionState: UiDescriptionState,
    onDescriptionChanged: (String) -> Unit
) {
    if (!uiDescriptionState.isVisible) return

    var text by remember(uiDescriptionState.description) {
        mutableStateOf(uiDescriptionState.description)
    }

    BasicTextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            onDescriptionChanged(newText)
        },
        modifier = modifier,
        enabled = uiDescriptionState.isEditable,
        textStyle = Relations1.copy(
            color = colorResource(id = R.color.text_primary)
        ),
        cursorBrush = SolidColor(colorResource(id = R.color.text_primary)),
        decorationBox = { innerTextField ->
            Box {
                if (text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.description),
                        style = Relations1,
                        color = colorResource(id = R.color.text_tertiary)
                    )
                }
                innerTextField()
            }
        }
    )
}

@DefaultPreviews
@Composable
private fun DescriptionWidgetPreview() {
    DescriptionWidget(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        uiDescriptionState = UiDescriptionState(
            description = "",
            isEditable = true,
            isVisible = true
        ),
        onDescriptionChanged = {}
    )
}

@DefaultPreviews
@Composable
private fun DescriptionWidgetWithTextPreview() {
    DescriptionWidget(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        uiDescriptionState = UiDescriptionState(
            description = "This is a description of the object type.",
            isEditable = true,
            isVisible = true
        ),
        onDescriptionChanged = {}
    )
}
