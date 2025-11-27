package com.anytypeio.anytype.payments.screens

import android.content.res.Configuration
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.fontInterSemibold
import com.anytypeio.anytype.core_models.membership.MembershipConstants.BUILDER_ID
import com.anytypeio.anytype.core_models.membership.MembershipConstants.CO_CREATOR_ID
import com.anytypeio.anytype.core_models.membership.MembershipConstants.FREE_ID
import com.anytypeio.anytype.core_models.membership.MembershipConstants.NEW_EXPLORER_ID
import com.anytypeio.anytype.core_models.membership.MembershipConstants.OLD_EXPLORER_ID
import com.anytypeio.anytype.core_models.membership.MembershipConstants.STARTER_ID
import com.anytypeio.anytype.core_models.membership.MembershipConstants.PIONEER_ID
import com.anytypeio.anytype.core_models.membership.MembershipConstants.PRO_MONTHLY_ID
import com.anytypeio.anytype.payments.models.TierPreview
import com.anytypeio.anytype.payments.models.Tier
import com.anytypeio.anytype.payments.models.TierConditionInfo
import com.anytypeio.anytype.payments.models.TierPeriod
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.core_models.membership.TierId

@Composable
fun TierPreviewView(
    tier: TierPreview,
    onClick: (TierId) -> Unit
) {
    val resources = mapTierPreviewToResources(tier)

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
                    color = colorResource(id = R.color.shape_secondary),
                    shape = RoundedCornerShape(16.dp)
                )
                .noRippleThrottledClickable { onClick(tier.id) }
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
                    .padding(start = 17.dp, top = 11.dp),
                text = tier.title,
                color = colorResource(id = R.color.text_primary),
                style = titleTextStyle,
                textAlign = TextAlign.Start
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(99.dp)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                text = tier.subtitle,
                color = colorResource(id = R.color.text_primary),
                style = Caption1Regular,
                textAlign = TextAlign.Start
            )
            ConditionInfoPreview(state = tier.conditionInfo)
            Spacer(modifier = Modifier.height(2.dp))
            ButtonPrimary(
                text = stringResource(id = R.string.payments_button_learn),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = { onClick(tier.id) },
                size = ButtonSize.XSmall
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (tier.isActive) {
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
fun mapTierToResources(tier: Tier): TierResources {
    return when (tier.id.value) {
        BUILDER_ID -> TierResources(
            mediumIcon = R.drawable.logo_builder_96,
            smallIcon = R.drawable.logo_builder_64,
            colors = toValue(tier.color),
            features = tier.features,
        )

        CO_CREATOR_ID -> TierResources(
            mediumIcon = R.drawable.logo_co_creator_96,
            smallIcon = R.drawable.logo_co_creator_64,
            colors = toValue(tier.color),
            features = tier.features,
        )
        OLD_EXPLORER_ID -> TierResources(
            mediumIcon = R.drawable.logo_explorer_96,
            smallIcon = R.drawable.logo_explorer_64,
            colors = toValue(tier.color),
            features = tier.features,
        )
        STARTER_ID -> TierResources(
            mediumIcon = R.drawable.logo_explorer_96,
            smallIcon = R.drawable.logo_explorer_64,
            colors = toValue(tier.color),
            features = tier.features,
        )
        PIONEER_ID -> TierResources(
            mediumIcon = R.drawable.logo_custom_64,
            smallIcon = R.drawable.logo_custom_64,
            colors = toValue(tier.color),
            features = tier.features,
        )
        NEW_EXPLORER_ID -> TierResources(
            mediumIcon = R.drawable.logo_new_explorer_96,
            smallIcon = R.drawable.logo_new_explorer_64,
            colors = toValue(tier.color),
            features = tier.features,
        )
        FREE_ID -> TierResources(
            mediumIcon = R.drawable.logo_explorer_96,
            smallIcon = R.drawable.logo_explorer_64,
            colors = toValue(tier.color),
            features = tier.features,
        )
        PRO_MONTHLY_ID -> TierResources(
            mediumIcon = R.drawable.logo_co_creator_96,
            smallIcon = R.drawable.logo_co_creator_64,
            colors = toValue(tier.color),
            features = tier.features,
        )
        else -> TierResources(
            smallIcon = R.drawable.logo_custom_64,
            mediumIcon = R.drawable.logo_custom_64,
            colors = toValue(tier.color),
            features = tier.features,
        )
    }
}

@Composable
fun mapTierPreviewToResources(tier: TierPreview): TierResources {
    return when (tier.id.value) {
        BUILDER_ID -> TierResources(
            mediumIcon = R.drawable.logo_builder_96,
            smallIcon = R.drawable.logo_builder_64,
            colors = toValue(tier.color)
        )

        CO_CREATOR_ID -> TierResources(
            mediumIcon = R.drawable.logo_co_creator_96,
            smallIcon = R.drawable.logo_co_creator_64,
            colors = toValue(tier.color)
        )
        OLD_EXPLORER_ID -> TierResources(
            mediumIcon = R.drawable.logo_explorer_96,
            smallIcon = R.drawable.logo_explorer_64,
            colors = toValue(tier.color)
        )
        STARTER_ID -> TierResources(
            mediumIcon = R.drawable.logo_explorer_96,
            smallIcon = R.drawable.logo_explorer_64,
            colors = toValue(tier.color)
        )
        PIONEER_ID -> TierResources(
            mediumIcon = R.drawable.logo_custom_64,
            smallIcon = R.drawable.logo_custom_64,
            colors = toValue(tier.color)
        )
        NEW_EXPLORER_ID -> TierResources(
            mediumIcon = R.drawable.logo_new_explorer_96,
            smallIcon = R.drawable.logo_new_explorer_64,
            colors = toValue(tier.color)
        )
        FREE_ID -> TierResources(
            mediumIcon = R.drawable.logo_explorer_96,
            smallIcon = R.drawable.logo_explorer_64,
            colors = toValue(tier.color),
        )
        PRO_MONTHLY_ID -> TierResources(
            mediumIcon = R.drawable.logo_co_creator_96,
            smallIcon = R.drawable.logo_co_creator_64,
            colors = toValue(tier.color),
        )
        else -> TierResources(
            smallIcon = R.drawable.logo_custom_64,
            mediumIcon = R.drawable.logo_custom_64,
            colors = toValue(tier.color)
        )
    }
}

@Composable
fun toValue(colorCode: String): TierColors {
    return TierColors(
        gradientStart = colorCode.gradientStart(),
        gradientEnd = colorCode.gradientEnd()
    )
}

@Composable
fun String.gradientStart(): Color = when (this) {
    CoverColor.RED.code -> colorResource(id = R.color.tier_gradient_red_start)
    CoverColor.BLUE.code -> colorResource(id = R.color.tier_gradient_blue_start)
    CoverColor.GREEN.code -> colorResource(id = R.color.tier_gradient_teal_start)
    CoverColor.PURPLE.code -> colorResource(id = R.color.tier_gradient_purple_start)
    CoverColor.ICE.code -> colorResource(id = R.color.tier_gradient_ice_start)
    else -> colorResource(id = R.color.tier_gradient_blue_start)
}

@Composable
private fun String.gradientEnd(): Color = when (this) {
    CoverColor.RED.code -> colorResource(id = R.color.tier_gradient_red_end)
    CoverColor.BLUE.code -> colorResource(id = R.color.tier_gradient_blue_end)
    CoverColor.GREEN.code -> colorResource(id = R.color.tier_gradient_teal_end)
    CoverColor.PURPLE.code -> colorResource(id = R.color.tier_gradient_purple_end)
    CoverColor.ICE.code -> colorResource(id = R.color.tier_gradient_ice_end)
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
    val mediumIcon: Int,
    val smallIcon: Int,
    val colors: TierColors,
    val features: List<String> = emptyList()
)

data class TierColors(
    val gradientStart: Color,
    val gradientEnd: Color
)

@ExperimentalMaterial3Api
@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
fun TierPreviewViewPreview() {
    TierPreviewView(
        tier = TierPreview(
            id = TierId(PRO_MONTHLY_ID),
            title = "Pro (monthly)",
            subtitle = "For those who want to build and create",
            conditionInfo = TierConditionInfo.Visible.Price("99.99", TierPeriod.Year(1)),
            isActive = true,
            color = "red"
        )
    ) {}
}