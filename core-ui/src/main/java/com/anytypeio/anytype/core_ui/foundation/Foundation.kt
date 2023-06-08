package com.anytypeio.anytype.core_ui.foundation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarning
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.Title1

@Composable
fun Toolbar(title: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = Title1,
            color = colorResource(R.color.text_primary)
        )
    }
}

@Composable
fun Dragger(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(
                height = 4.dp,
                width = 48.dp
            )
            .background(
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
        Modifier
            .padding(start = paddingStart, end = paddingEnd)
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
        modifier = Modifier
            .height(52.dp)
            .clickable(onClick = onClick)

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
            ),
            style = BodyRegular
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

@Composable
fun Warning(
    title: String,
    subtitle: String,
    actionButtonText: String,
    cancelButtonText: String,
    onNegativeClick: () -> Unit,
    onPositiveClick: () -> Unit,
    isInProgress: Boolean = false
) {
    Column {
        Text(
            text = title,
            modifier = Modifier.padding(
                top = 24.dp,
                start = 20.dp,
                end = 20.dp
            ),
            style = HeadlineHeading,
            color = colorResource(R.color.text_primary)
        )
        Text(
            text = subtitle,
            modifier = Modifier.padding(
                top = 12.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 10.dp
            ),
            style = BodyCalloutRegular,
            color = colorResource(R.color.text_primary)
        )
        Row(
            modifier = Modifier
                .padding(
                    top = 10.dp,
                    start = 20.dp,
                    end = 20.dp
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ButtonSecondary(
                onClick = onNegativeClick,
                size = ButtonSize.LargeSecondary,
                text = cancelButtonText,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            ButtonWarning(
                onClick = onPositiveClick,
                size = ButtonSize.Large,
                text = actionButtonText,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@ExperimentalMaterialApi
@Composable
fun Announcement(
    title: String,
    subtitle: String,
    onBackClicked: () -> Unit,
    onNextClicked: () -> Unit
) {
    Box(contentAlignment = Alignment.BottomCenter) {
        Card(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                bottom = 16.dp
            ),
            shape = RoundedCornerShape(12.dp),
            backgroundColor = colorResource(R.color.background_secondary)
        ) {
            Column {
                Text(
                    text = title,
                    modifier = Modifier.padding(
                        top = 24.dp,
                        start = 20.dp,
                        end = 20.dp
                    ),
                    style = HeadlineHeading,
                    color = colorResource(R.color.text_primary)
                )
                Text(
                    text = subtitle,
                    modifier = Modifier.padding(
                        top = 12.dp,
                        start = 20.dp,
                        end = 20.dp
                    ),
                    style = BodyCalloutRegular,
                    color = colorResource(R.color.text_primary)
                )
                Row(
                    modifier = Modifier
                        .height(68.dp)
                        .padding(
                            top = 8.dp,
                            start = 20.dp,
                            end = 20.dp
                        )
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ButtonSecondary(
                        text = stringResource(R.string.back),
                        onClick = onBackClicked,
                        size = ButtonSize.LargeSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.0f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    ButtonPrimary(
                        text = stringResource(R.string.next),
                        onClick = onNextClicked,
                        size = ButtonSize.Large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.0f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}