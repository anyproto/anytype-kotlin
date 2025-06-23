package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.widgets.contentSizeForBackground

@Composable
fun DeletedIconView(
    modifier: Modifier = Modifier,
    backgroundSize: Dp,
    iconWithoutBackgroundMaxSize: Dp = 20.dp
) {
    val (containerModifier, iconModifier) = if (backgroundSize > iconWithoutBackgroundMaxSize) {
        modifier
            .size(backgroundSize) to Modifier.size(
            contentSizeForBackground(backgroundSize)
        )
    } else {
        modifier.size(backgroundSize) to Modifier
    }

    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.ic_relation_deleted),
            contentDescription = "Empty Object Icon",
            modifier = iconModifier
        )
    }
}

@DefaultPreviews
@Composable
fun Deleted16ObjectIconViewPreview() {
    DeletedIconView(backgroundSize = 16.dp)
}

@DefaultPreviews
@Composable
fun Deleted20ObjectIconViewPreview() {
    DeletedIconView(backgroundSize = 20.dp)
}

@DefaultPreviews
@Composable
fun Deleted32ObjectIconViewPreview() {
    DeletedIconView(backgroundSize = 32.dp)
}

@DefaultPreviews
@Composable
fun Deleted48ObjectIconViewPreview() {
    DeletedIconView(backgroundSize = 48.dp)
}

@DefaultPreviews
@Composable
fun Deleted64ObjectIconViewPreview() {
    DeletedIconView(backgroundSize = 64.dp)
}