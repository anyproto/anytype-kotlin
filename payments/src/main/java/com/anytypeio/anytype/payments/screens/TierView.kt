package com.anytypeio.anytype.payments.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.fontInterSemibold
import com.anytypeio.anytype.core_utils.ext.formatToDateString
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.Tier

@Composable
fun TierView(
    membershipStatus: MembershipStatus,
    tier: Tier,
    onClick: (Tier) -> Unit
) {
    val resources = mapTierToResources(tier) ?: return

    val brush = Brush.verticalGradient(
        listOf(
            resources.colors.gradientStart,
            Color.Transparent
        )
    )

    Box(modifier = Modifier.wrapContentSize()) {
        Column(
            modifier = Modifier
                .width(192.dp)
                .wrapContentHeight()
                .background(
                    color = colorResource(id = R.color.shape_tertiary),
                    shape = RoundedCornerShape(16.dp)
                )
                .noRippleThrottledClickable { onClick(tier) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(brush = brush, shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.BottomStart
            ) {
                Icon(
                    modifier = Modifier
                        .padding(start = 16.dp),
                    painter = painterResource(id = resources.smallIcon),
                    contentDescription = "logo",
                    tint = resources.colors.gradientEnd
                )
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 17.dp, top = 10.dp),
                text = resources.title,
                color = colorResource(id = R.color.text_primary),
                style = titleTextStyle,
                textAlign = TextAlign.Start
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .padding(start = 16.dp, end = 16.dp, top = 5.dp),
                text = resources.subtitle,
                color = colorResource(id = R.color.text_primary),
                style = Caption1Regular,
                textAlign = TextAlign.Start
            )
            if (tier.isCurrent) {
                ValidUntilText(
                    expirationText(
                        formattedDateEnds = membershipStatus.formattedDateEnds,
                        tier = tier
                    )
                )
            } else {
                //PriceText(price = tier.price, interval = tier.interval)
            }
//            Price(tier = tier)
//            TierPreviewButton(tier = tier) {
//                onClick()
//            }
            ButtonPrimary(
                text = resources.btnText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = { onClick(tier) },
                size = ButtonSize.Small
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (tier.isCurrent) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(end = 15.5.dp, top = 18.dp)
                    .border(
                        shape = RoundedCornerShape(11.dp),
                        color = colorResource(id = R.color.text_primary),
                        width = 1.dp
                    )
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, bottom = 3.dp, start = 8.dp, end = 8.dp),
                text = stringResource(id = R.string.payments_current_label),
                color = colorResource(id = R.color.text_primary),
                style = Relations3,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PriceText(price: String, interval: String) {
    Row() {
        Text(
            modifier = Modifier
                .wrapContentWidth()
                .padding(start = 20.dp),
            text = price,
            color = colorResource(id = R.color.text_primary),
            style = HeadlineTitle,
            textAlign = TextAlign.Start
        )
        Text(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.Bottom)
                .padding(bottom = 4.dp, start = 6.dp),
            text = interval,
            color = colorResource(id = R.color.text_primary),
            style = Relations1,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun ValidUntilText(text: String) {
    Text(
        modifier = Modifier.padding(start = 16.dp),
        text = text,
        style = Caption1Regular,
        color = colorResource(id = R.color.text_primary)
    )
}

@Composable
fun mapTierToResources(tier: Tier?): TierResources? {
    return when (tier) {
        is Tier.Builder -> TierResources(
            title = stringResource(id = R.string.payments_tier_builder),
            subtitle = stringResource(id = R.string.payments_tier_builder_description),
            mediumIcon = R.drawable.logo_builder_96,
            smallIcon = R.drawable.logo_builder_64,
            colors = toValue(tier.color),
            features = tier.features,
            btnText = getButtonText(tier)
        )

        is Tier.CoCreator -> TierResources(
            title = stringResource(id = R.string.payments_tier_cocreator),
            subtitle = stringResource(id = R.string.payments_tier_cocreator_description),
            mediumIcon = R.drawable.logo_co_creator_96,
            smallIcon = R.drawable.logo_co_creator_64,
            colors = toValue(tier.color),
            features = tier.features,
            btnText = getButtonText(tier)
        )

        is Tier.Custom -> TierResources(
            title = stringResource(id = R.string.payments_tier_custom),
            subtitle = stringResource(id = R.string.payments_tier_custom_description),
            smallIcon = R.drawable.logo_custom_64,
            mediumIcon = R.drawable.logo_custom_64,
            colors = toValue(tier.color),
            features = tier.features,
            btnText = getButtonText(tier)
        )

        is Tier.Explorer -> TierResources(
            title = stringResource(id = R.string.payments_tier_explorer),
            subtitle = stringResource(id = R.string.payments_tier_explorer_description),
            mediumIcon = R.drawable.logo_explorer_96,
            smallIcon = R.drawable.logo_explorer_64,
            colors = toValue(tier.color),
            features = tier.features,
            btnText = getButtonText(tier)
        )
        else -> null
    }
}

@Composable
private fun getButtonText(tier: Tier): String {
    return if (tier.isCurrent) {
        stringResource(id = R.string.payments_button_manage)
    } else {
        stringResource(id = R.string.payments_button_learn)
    }
}

@Composable
private fun expirationText(formattedDateEnds: String, tier: Tier): String {
    return when (tier) {
        is Tier.Explorer -> stringResource(id = R.string.payments_tier_details_free_forever)
        is Tier.Builder -> stringResource(id = R.string.payments_tier_details_valid_until, formattedDateEnds)
        is Tier.CoCreator -> stringResource(id = R.string.payments_tier_details_valid_until, formattedDateEnds)
        is Tier.Custom -> stringResource(id = R.string.payments_tier_details_valid_until, formattedDateEnds)
    }
}

@Composable
private fun toValue(colorCode: String): TierColors {
    return TierColors(
        gradientStart = colorCode.gradientStart(),
        gradientEnd = colorCode.gradientEnd()
    )
}

@Composable
private fun String.gradientStart(): Color = when (this) {
    CoverColor.RED.code -> colorResource(id = R.color.tier_gradient_red_start)
    CoverColor.BLUE.code -> colorResource(id = R.color.tier_gradient_blue_start)
    CoverColor.GREEN.code -> colorResource(id = R.color.tier_gradient_teal_start)
    CoverColor.PURPLE.code -> colorResource(id = R.color.tier_gradient_purple_start)
    else -> colorResource(id = R.color.tier_gradient_blue_start)
}

@Composable
private fun String.gradientEnd(): Color = when (this) {
    CoverColor.RED.code -> colorResource(id = R.color.tier_gradient_red_end)
    CoverColor.BLUE.code -> colorResource(id = R.color.tier_gradient_blue_end)
    CoverColor.GREEN.code -> colorResource(id = R.color.tier_gradient_teal_end)
    CoverColor.PURPLE.code -> colorResource(id = R.color.tier_gradient_purple_end)
    else -> colorResource(id = R.color.tier_gradient_blue_end)
}

val titleTextStyle = TextStyle(
    fontFamily = fontInterSemibold,
    fontWeight = FontWeight.W600,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).em
)

data class TierResources(
    val title: String,
    val subtitle: String,
    val mediumIcon: Int,
    val smallIcon: Int,
    val colors: TierColors,
    val features: List<String>,
    val btnText: String
)

data class TierColors(
    val gradientStart: Color,
    val gradientEnd: Color
)