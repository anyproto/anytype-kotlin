package com.anytypeio.anytype.feature_date.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
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
import com.anytypeio.anytype.core_models.DayOfWeekCustom
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.common.ShimmerEffect
import com.anytypeio.anytype.core_ui.extensions.getLocalizedDayName
import com.anytypeio.anytype.core_ui.extensions.getPrettyName
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.feature_date.R
import com.anytypeio.anytype.feature_date.ui.models.DateEvent
import com.anytypeio.anytype.feature_date.viewmodel.UiHeaderState

@Composable
fun HeaderScreen(
    modifier: Modifier,
    uiState: UiHeaderState,
    onDateEvent: (DateEvent) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RelativeDateAndDayOfWeek(
            modifier = Modifier
                .wrapContentSize(),
            uiState = uiState
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .height(32.dp)
                    .width(52.dp)
                    .rotate(180f)
                    .noRippleThrottledClickable {
                        onDateEvent(DateEvent.Header.OnPreviousClick)
                    },
                contentDescription = "Previous day",
                painter = painterResource(id = R.drawable.ic_arrow_disclosure_18),
                contentScale = ContentScale.None
            )
            when (uiState) {
                is UiHeaderState.Content -> {
                    Text(
                        textAlign = TextAlign.Center,
                        text = uiState.title,
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .weight(1f),
                        style = HeadlineTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = colorResource(id = R.color.text_primary)
                    )
                }

                UiHeaderState.Loading -> {
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

                UiHeaderState.Empty -> {
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
                    .height(32.dp)
                    .width(52.dp)
                    .noRippleThrottledClickable {
                        onDateEvent(DateEvent.Header.OnNextClick)
                    },
                contentDescription = "Next day",
                painter = painterResource(id = R.drawable.ic_arrow_disclosure_18),
                contentScale = ContentScale.None
            )
        }
    }
}

@Composable
fun RelativeDateAndDayOfWeek(
    modifier: Modifier,
    uiState: UiHeaderState
) {
    Row(modifier = modifier) {
        if (uiState is UiHeaderState.Content) {
            when (val relativeDate = uiState.relativeDate) {
                is RelativeDate.Today -> {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = "${relativeDate.getPrettyName()} \u2022 ",
                        style = Relations2,
                        color = colorResource(id = R.color.text_secondary)
                    )
                }

                is RelativeDate.Tomorrow -> {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = "${relativeDate.getPrettyName()} \u2022 ",
                        style = Relations2,
                        color = colorResource(id = R.color.text_secondary)
                    )
                }

                is RelativeDate.Yesterday -> {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = "${relativeDate.getPrettyName()} \u2022 ",
                        style = Relations2,
                        color = colorResource(id = R.color.text_secondary)
                    )
                }

                else -> {}
            }
            val dayOfWeek = getLocalizedDayName(uiState.relativeDate.dayOfWeek)
            Text(
                modifier = Modifier.wrapContentSize(),
                text = dayOfWeek,
                style = Relations2,
                color = colorResource(id = R.color.text_secondary)
            )
        }
    }
}

@Composable
@DefaultPreviews
fun DateLayoutHeaderEmptyPreview() {
    val state = UiHeaderState.Empty
    HeaderScreen(
        modifier = Modifier
            .fillMaxWidth(), uiState = state
    ) {}
}

@Composable
@DefaultPreviews
fun DateLayoutHeaderLoadingPreview() {
    val state = UiHeaderState.Loading
    HeaderScreen(
        Modifier
            .fillMaxWidth(), state
    ) {}
}

@Composable
@DefaultPreviews
fun DateLayoutHeaderPreview() {
    val state = UiHeaderState.Content(
        title = "Tue, 12 Oct",
        relativeDate = RelativeDate.Other(
            initialTimeInMillis = 1634016000000,
            dayOfWeek = DayOfWeekCustom.MONDAY,
            formattedDate = "Tue, 12 Oct",
            formattedTime = "12:00 AM"
        )
    )
    HeaderScreen(
        Modifier
            .fillMaxWidth(), state
    ) {}
}