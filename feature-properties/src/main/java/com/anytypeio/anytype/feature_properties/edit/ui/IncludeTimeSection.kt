package com.anytypeio.anytype.feature_properties.edit.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.BodyRegular

@Composable
fun PropertyIncludeTimeSection(
    modifier: Modifier,
    isIncluded: Boolean,
    isEditable: Boolean,
    onChangeIncludeTimeClick: () -> Unit
) {
    Box(modifier = modifier) {
        Text(
            modifier = Modifier.align(Alignment.CenterStart),
            text = stringResource(id = R.string.property_include_time_section),
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary)
        )

        var checked = remember { mutableStateOf(isIncluded) }

        Switch(
            modifier = Modifier.align(Alignment.CenterEnd),
            checked = checked.value,
            onCheckedChange = {
                checked.value = it
            },
            enabled = isEditable,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorResource(id = R.color.white),
                disabledCheckedThumbColor = colorResource(id = R.color.white),
                uncheckedThumbColor = colorResource(id = R.color.white),
                disabledUncheckedThumbColor = colorResource(id = R.color.white),

                checkedTrackColor = colorResource(id = R.color.palette_system_amber_50),
                disabledCheckedTrackColor = colorResource(id = R.color.palette_system_amber_50_opacity_12),
                uncheckedTrackColor = colorResource(id = R.color.shape_secondary),
                disabledUncheckedTrackColor = colorResource(id = R.color.shape_tertiary),

                disabledUncheckedBorderColor = Color.Transparent,
            )
        )
    }
}