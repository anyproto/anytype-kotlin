package com.anytypeio.anytype.core_ui.widgets.dv
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.UXBody


@Composable
fun ObjectSetTitle(isVisible: Boolean, doneAction: () -> Unit) {
    val currentState by rememberUpdatedState(isVisible)
    if (currentState) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(end = 16.dp)
                .background(color = colorResource(id = R.color.background_primary))
                .noRippleThrottledClickable { doneAction() },
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = stringResource(id = R.string.done),
                style = UXBody,
                color = colorResource(id = R.color.glyph_accent)
            )
        }
    }
}