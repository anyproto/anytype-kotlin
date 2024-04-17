package com.anytypeio.anytype.payments.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.Membership.Status.*
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.fontInterSemibold
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.membership.models.Tier
import com.anytypeio.anytype.presentation.membership.models.TierId

@Composable
fun TierView(
    tier: Tier,
    title: String,
    subTitle: String,
    colorGradient: Color,
    radialGradient: Color,
    icon: Int,
    buttonText: String,
    onClick: () -> Unit,
    isCurrent: Boolean
) {
    val brush = Brush.verticalGradient(
        listOf(
            colorGradient,
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
                .noRippleThrottledClickable { onClick() }
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
                    painter = painterResource(id = icon),
                    contentDescription = "logo",
                    tint = radialGradient
                )
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 17.dp, top = 10.dp),
                text = title,
                color = colorResource(id = R.color.text_primary),
                style = titleTextStyle,
                textAlign = TextAlign.Start
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .padding(start = 16.dp, end = 16.dp, top = 5.dp),
                text = subTitle,
                color = colorResource(id = R.color.text_primary),
                style = Caption1Regular,
                textAlign = TextAlign.Start
            )
            PriceOrOption(tier = tier)
            TierPreviewButton(tier = tier) {
                onClick()
            }
            ButtonPrimary(
                text = buttonText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = { onClick() },
                size = ButtonSize.Small
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (isCurrent) {
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
fun PriceOrOption(tier: Tier) {
    if (tier.isCurrent) {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = "9.99",
            style = titleTextStyle,
            color = colorResource(id = R.color.text_primary)
        )
    } else {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = "9.99",
            style = titleTextStyle,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Composable
fun TierPreviewButton(tier: Tier, onClick: () -> Unit) {
    if (tier.status != STATUS_PENDING) {

    }
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
            features = tier.features
        )

        is Tier.CoCreator -> TierResources(
            title = stringResource(id = R.string.payments_tier_cocreator),
            subtitle = stringResource(id = R.string.payments_tier_cocreator_description),
            mediumIcon = R.drawable.logo_co_creator_96,
            smallIcon = R.drawable.logo_co_creator_64,
            colors = toValue(tier.color),
            features = tier.features
        )

        is Tier.Custom -> TierResources(
            title = stringResource(id = R.string.payments_tier_custom),
            subtitle = stringResource(id = R.string.payments_tier_custom_description),
            smallIcon = R.drawable.logo_custom_64,
            mediumIcon = R.drawable.logo_custom_64,
            colors = toValue(tier.color),
            features = tier.features
        )

        is Tier.Explorer -> TierResources(
            title = stringResource(id = R.string.payments_tier_explorer),
            subtitle = stringResource(id = R.string.payments_tier_explorer_description),
            mediumIcon = R.drawable.logo_explorer_96,
            smallIcon = R.drawable.logo_explorer_64,
            colors = toValue(tier.color),
            features = tier.features
        )
        else -> null
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

@Preview
@Composable
fun TierPreview() {
    TierView(
        title = "Explorer",
        subTitle = "Dive into the network and enjoy the thrill of one-on-one collaboration",
        buttonText = "Subscribe",
        onClick = {},
        icon = R.drawable.logo_co_creator_64,
        colorGradient = Color(0xFFCFF6CF),
        radialGradient = Color(0xFF24BFD4),
        isCurrent = true,
        tier = Tier.Explorer(
            id = TierId(1),
            isCurrent = true,
            color = CoverColor.BLUE.code,
            features = listOf("Feature 1", "Feature 2", "Feature 3"),
            status = Membership.Status.STATUS_ACTIVE
        )
    )
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
    val features: List<String>
)

data class TierColors(
    val gradientStart: Color,
    val gradientEnd: Color
)