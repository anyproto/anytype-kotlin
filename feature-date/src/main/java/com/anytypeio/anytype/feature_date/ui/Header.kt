package com.anytypeio.anytype.feature_date.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.common.ShimmerEffect
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.feature_date.R
import com.anytypeio.anytype.feature_date.models.DateObjectHeaderState

@Composable
fun DateObjectHeader(
    modifier: Modifier,
    state: DateObjectHeaderState,
    action: (DateObjectHeaderState.Action) -> Unit
) {
    Row(
        modifier = modifier
    ) {
        Image(
            modifier = Modifier
                .height(48.dp)
                .width(52.dp)
                .rotate(180f)
                .clickable {
                    action(DateObjectHeaderState.Action.Previous)
                },
            contentDescription = "Previous day",
            painter = painterResource(id = R.drawable.ic_arrow_disclosure_18),
            contentScale = ContentScale.None
        )
        when (state) {
            is DateObjectHeaderState.Content -> {
                Text(
                    textAlign = TextAlign.Center,
                    text = state.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    style = HeadlineTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colorResource(id = R.color.text_primary)
                )
            }

            DateObjectHeaderState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center
                ) {
                    ShimmerEffect(
                        modifier = Modifier
                            .width(200.dp)
                            .height(30.dp)
                    )
                }
            }

            DateObjectHeaderState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center
                ) {
                    Spacer(
                        modifier = Modifier
                            .width(200.dp)
                            .height(30.dp)
                    )
                }
            }
        }
        Image(
            modifier = Modifier
                .height(48.dp)
                .width(52.dp)
                .clickable {
                    action(DateObjectHeaderState.Action.Next)
                },
            contentDescription = "Next day",
            painter = painterResource(id = R.drawable.ic_arrow_disclosure_18),
            contentScale = ContentScale.None
        )
    }
}

@Composable
@DefaultPreviews
fun DateLayoutHeaderEmptyPreview() {
    val state = DateObjectHeaderState.Empty
    DateObjectHeader(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp), state = state
    ) {}
}

@Composable
@DefaultPreviews
fun DateLayoutHeaderLoadingPreview() {
    val state = DateObjectHeaderState.Loading
    DateObjectHeader(
        Modifier
            .fillMaxWidth()
            .height(48.dp), state
    ) {}
}

@Composable
@DefaultPreviews
fun DateLayoutHeaderPreview() {
    val state = DateObjectHeaderState.Content("Tue, 12 Oct")
    DateObjectHeader(
        Modifier
            .fillMaxWidth()
            .height(48.dp), state
    ) {}
}