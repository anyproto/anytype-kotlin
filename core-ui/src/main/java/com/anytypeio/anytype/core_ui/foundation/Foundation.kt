package com.anytypeio.anytype.core_ui.foundation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun Dragger(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(
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
            style = MaterialTheme.typography.h2,
            text = title,
            modifier = Modifier.padding(
                top = 24.dp,
                start = 20.dp,
                end = 20.dp
            ),
            color = colorResource(R.color.text_primary)
        )
        Text(
            text = subtitle,
            modifier = Modifier.padding(
                top = 12.dp,
                start = 20.dp,
                end = 20.dp
            ),
            color = colorResource(R.color.text_primary)
        )
        Row(
            modifier = Modifier.height(68.dp).padding(
                top = 8.dp,
                start = 20.dp,
                end = 20.dp
            ).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.height(48.dp).border(
                    width = 1.dp,
                    color = colorResource(R.color.shape_primary),
                    shape = RoundedCornerShape(10.dp)
                ).weight(1.0f, true).clickable(onClick = onNegativeClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cancelButtonText,
                    color = colorResource(R.color.text_primary),
                    fontSize = 17.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier.height(48.dp).background(
                    color = colorResource(R.color.palette_system_red),
                    shape = RoundedCornerShape(10.dp)
                ).weight(1.0f, true).clickable(onClick = onPositiveClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionButtonText,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (isInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp).size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
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
                    style = MaterialTheme.typography.h2,
                    text = title,
                    modifier = Modifier.padding(
                        top = 24.dp,
                        start = 20.dp,
                        end = 20.dp
                    ),
                    color = colorResource(R.color.text_primary)
                )
                Text(
                    text = subtitle,
                    modifier = Modifier.padding(
                        top = 12.dp,
                        start = 20.dp,
                        end = 20.dp
                    ),
                    color = colorResource(R.color.text_primary)
                )
                Row(
                    modifier = Modifier.height(68.dp).padding(
                        top = 8.dp,
                        start = 20.dp,
                        end = 20.dp
                    ).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier.height(48.dp).weight(1.0f, true),
                        onClick = onBackClicked,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = colorResource(R.color.shape_primary)
                        ),
                        elevation = 0.dp,
                        backgroundColor = Color.Transparent
                    ) {
                        Box(Modifier.wrapContentSize(Alignment.Center)) {
                            Text(
                                text = stringResource(R.string.back),
                                color = colorResource(R.color.text_primary),
                                fontSize = 17.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Card(
                        modifier = Modifier.height(48.dp).weight(1.0f, true),
                        onClick = onNextClicked,
                        backgroundColor = colorResource(R.color.glyph_accent),
                        shape = RoundedCornerShape(10.dp),
                        elevation = 0.dp
                    ) {
                        Box(Modifier.wrapContentSize(Alignment.Center)) {
                            Text(
                                text = stringResource(R.string.next),
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}