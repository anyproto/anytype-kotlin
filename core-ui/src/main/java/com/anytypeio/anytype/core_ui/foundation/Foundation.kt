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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.membership.Membership.Status
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarningLoading
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_models.membership.MembershipStatus

@Composable
fun Toolbar(
    title: String,
    color: Color = colorResource(R.color.text_primary)
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = Title1,
            color = color
        )
    }
}

@Composable
fun Dragger(
    modifier: Modifier = Modifier,
    color: Color = colorResource(R.color.shape_primary)
) {
    Box(
        modifier = modifier
            .size(
                height = 4.dp,
                width = 48.dp
            )
            .background(
                color = color,
                shape = RoundedCornerShape(6.dp)
            )
    )
}

@Composable
fun Divider(
    modifier: Modifier = Modifier,
    paddingStart: Dp = 20.dp,
    paddingEnd: Dp = 20.dp,
    visible: Boolean = true
) {
    Box(
        modifier = modifier
            .alpha(if (visible) 1f else 0f)
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
fun OptionMembership(
    @DrawableRes image: Int,
    text: String,
    onClick: () -> Unit = {},
    membershipStatus: MembershipStatus?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleThrottledClickable { onClick() }

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
        when (membershipStatus?.status) {
            Status.STATUS_ACTIVE -> {
                Box(
                    modifier = Modifier.weight(1.0f, true),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 38.dp),
                        text = membershipStatus.tiers.firstOrNull { it.id == membershipStatus.activeTier?.value }?.name.orEmpty(),
                        color = colorResource(R.color.text_secondary),
                        style = BodyRegular
                    )
                    Arrow()
                }
            }
            else -> {
                Box(
                    modifier = Modifier.weight(1.0f, true),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 38.dp),
                        text = stringResource(id = R.string.membership_price_pending),
                        color = colorResource(R.color.text_secondary),
                        style = BodyRegular
                    )
                }
            }
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
    isInProgress: Boolean = false,
    footerHeight: Dp = 10.dp
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
        if (subtitle.isNotEmpty()) {
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
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }
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
            ButtonWarningLoading(
                onClick = onPositiveClick,
                size = ButtonSize.Large,
                text = actionButtonText,
                modifierBox = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                modifierButton = Modifier.fillMaxWidth(),
                loading = isInProgress
            )
        }
        Spacer(modifier = Modifier.height(footerHeight))
    }
}

@Composable
fun Announcement(
    title: String,
    subtitle: String,
    actionButtonText: String,
    cancelButtonText: String,
    onRightClicked: () -> Unit,
    onLeftClicked: () -> Unit,
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
        if (subtitle.isNotEmpty()) {
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
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }
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
                onClick = onLeftClicked,
                size = ButtonSize.LargeSecondary,
                text = cancelButtonText,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            ButtonPrimary(
                onClick = onRightClicked,
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

@Preview
@Composable
fun WarningPreview() {
    Warning(
        title = "Leave space",
        subtitle = "Investors space will be removed from your devices  and you will no longer have access to it",
        actionButtonText = "Leave space",
        cancelButtonText = "Cancel",
        onNegativeClick = {},
        onPositiveClick = {}
    )
}

@Preview
@Composable
fun AnnouncementPreview() {
    Announcement(
        title = "Leave space",
        subtitle = "Investors space will be removed from your devices  and you will no longer have access to it",
        actionButtonText = "Leave space",
        cancelButtonText = "Cancel",
        onLeftClicked = {},
        onRightClicked = {}
    )
}