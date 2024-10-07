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
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium

@Composable
fun DismissBackground(
    actionText: String,
    dismissState: SwipeToDismissBoxState,
    dismissBackgroundColor: Int = R.color.palette_system_red,
    mainBackgroundColor: Int = R.color.glyph_active,
    textColor: Int = R.color.text_white
) {
    val color by animateColorAsState(
        when (dismissState.targetValue) {
            SwipeToDismissBoxValue.EndToStart -> colorResource(id = dismissBackgroundColor)
            else -> colorResource(id = mainBackgroundColor)
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
            text = actionText,
            color = colorResource(id = textColor),
            style = BodyCalloutMedium
        )
    }
}
