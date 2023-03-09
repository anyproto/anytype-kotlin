package com.anytypeio.anytype.ui_settings.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.ui_settings.R

@Composable
fun Section(modifier: Modifier = Modifier, title: String) {
    Text(
        modifier = modifier,
        text = title,
        color = colorResource(id = R.color.text_secondary),
        fontSize = 13.sp
    )
}

@Composable
fun NameBlock(modifier: Modifier = Modifier, name: String) {
    Column(modifier = modifier.padding(start = 20.dp)) {
        Text(
            text = "Name",
            color = colorResource(id = R.color.text_secondary),
            fontSize = 13.sp
        )
        Text(
            text = name,
            style = MaterialTheme.typography.h2,
        )
    }
}

@Composable
fun SpaceNameBlock(modifier: Modifier = Modifier) {
    Text(
        text = "Space",
        style = MaterialTheme.typography.h3,
    )
}

@Composable
fun SpaceImageBlock(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier
            .height(96.dp)
            .width(96.dp)
            .padding(bottom = 21.dp),
        painter = painterResource(id = R.drawable.ic_home_widget_space),
        contentDescription = "space_image"
    )
}