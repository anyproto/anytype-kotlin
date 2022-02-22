package com.anytypeio.anytype.core_ui.foundation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
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
            style = MaterialTheme.typography.h3,
            color = colorResource(R.color.text_primary)
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

@Composable
fun Option(
    @DrawableRes image: Int,
    text: String,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(52.dp).clickable(onClick = onClick)

    ) {
        Image(
            painterResource(image),
            contentDescription = "Option icon",
            modifier = Modifier.padding(
                start = 20.dp
            )
        )
        Text(
            text = text,
            color = colorResource(R.color.text_primary),
            modifier = Modifier.padding(
                start = 12.dp
            )
        )
        Box(
            modifier = Modifier.weight(1.0f, true),
            contentAlignment = Alignment.CenterEnd
        ) {
            Arrow()
        }
    }
}

@Composable
fun Arrow() {
    Image(
        painterResource(R.drawable.ic_arrow_forward),
        contentDescription = "Arrow forward",
        modifier = Modifier.padding(
            end = 20.dp
        )
    )
}