package com.anytypeio.anytype.core_ui.foundation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium

@Composable
fun RowScope.DismissBackground(
    actionText: String,
    dismissState: SwipeToDismissBoxState
) {
    val color by animateColorAsState(
        when (dismissState.targetValue) {
            SwipeToDismissBoxValue.EndToStart -> colorResource(id = R.color.palette_system_red)
            else -> colorResource(id = R.color.glyph_active)
        }, label = ""
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            modifier = Modifier.padding(end = 16.dp),
            text = stringResource(R.string.move_to_bin),
            color = colorResource(id = R.color.text_white),
            style = BodyCalloutMedium
        )
    }
}
