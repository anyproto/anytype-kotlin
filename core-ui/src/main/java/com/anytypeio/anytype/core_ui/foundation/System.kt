package com.anytypeio.anytype.core_ui.foundation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title1

@Composable
fun Section(
    title: String,
    color: Color = colorResource(id = R.color.text_secondary),
    textPaddingStart: Dp = 20.dp
) {
    Box(modifier = Modifier
        .height(52.dp)
        .fillMaxWidth()) {
        Text(
            modifier = Modifier
                .padding(
                    start = textPaddingStart,
                    bottom = 8.dp
                )
                .align(Alignment.BottomStart),
            text = title,
            color = color,
            style = Caption1Regular
        )
    }
}

@Composable
fun Header(
    text: String
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .align(Alignment.Center)
            ,
            text = text,
            color = colorResource(id = R.color.text_primary),
            style = Title1,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun HeaderPreview() {
    Header(text = "Header")
}
