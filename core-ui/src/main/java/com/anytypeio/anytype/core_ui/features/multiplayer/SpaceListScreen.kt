package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Header
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.Title2

@Composable
fun SpaceListScreen() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        )
        Header(text = "Spaces")
        SpaceListCardItem()
        SpaceListCardItem()
        SpaceListCardItem()
    }
}

@Composable
fun SpaceListCardItem() {
    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (icon, title, subtitle, divider, dots, circle, network, status) = createRefs()

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Red)
                .constrainAs(icon) {
                    top.linkTo(parent.top, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                }
        )

        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color.Blue)
                .constrainAs(dots) {
                    top.linkTo(parent.top, margin = 24.dp)
                    end.linkTo(parent.end, margin = 12.dp)
                }
        )

        Text(
            text = LoremIpsum(30).values.toString(),
            style = Title2,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top, margin = 21.dp)
                start.linkTo(icon.end, margin = 12.dp)
                end.linkTo(dots.start, margin = 12.dp)
                width = Dimension.fillToConstraints
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = LoremIpsum(30).values.toString(),
            style = Relations2,
            color = colorResource(id = R.color.text_secondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.constrainAs(subtitle) {
                top.linkTo(title.bottom)
                start.linkTo(icon.end, margin = 12.dp)
                end.linkTo(dots.start, margin = 12.dp)
                width = Dimension.fillToConstraints
            }
        )

        Divider(
            modifier = Modifier.constrainAs(divider) {
                top.linkTo(icon.bottom, margin = 16.dp)
                start.linkTo(parent.start, margin = 16.dp)
                end.linkTo(parent.end, margin = 12.dp)
            }
        )

        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color.Green, CircleShape)
                .constrainAs(circle) {
                    start.linkTo(parent.start, margin = 16.dp)
                    top.linkTo(network.top)
                    bottom.linkTo(network.bottom)
                }
        )

        Text(
            text = stringResource(id = R.string.network),
            color = colorResource(id = R.color.text_secondary),
            style = Relations3,
            modifier = Modifier.constrainAs(network) {
                top.linkTo(divider.bottom, margin = 12.dp)
                start.linkTo(circle.end, margin = 6.dp)
            }
        )

        Text(
            text = "Active",
            style = Relations3,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.constrainAs(status) {
                top.linkTo(divider.bottom, margin = 12.dp)
                start.linkTo(network.end, margin = 4.dp)
            }
        )
    }
}

@Preview
@Composable
private fun SpaceListScreenPreview() {
    SpaceListScreen()
}

@Preview
@Composable
private fun SpaceCardItemPreview() {
    SpaceListCardItem()
}



