package com.anytypeio.anytype.core_ui.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R

@Composable
fun Toolbar(title: String) {
    Box(
        Modifier.fillMaxWidth().height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.h3
        )
    }
}

@Composable
fun Dragger() {
    Box(
        modifier = Modifier.size(
            height = 4.dp,
            width = 48.dp
        ).background(
            color = colorResource(R.color.shape_primary),
            shape = RoundedCornerShape(6.dp)
        )
    )
}

@Composable
fun Divider(
    paddingStart: Dp = 20.dp,
    paddingEnd: Dp = 20.dp
) {
    Box(
        Modifier.padding(start = paddingStart, end = paddingEnd)
            .background(color = colorResource(R.color.shape_primary))
            .height(0.5.dp)
            .fillMaxWidth()
    )
}