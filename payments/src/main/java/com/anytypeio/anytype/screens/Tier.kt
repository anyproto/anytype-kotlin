package com.anytypeio.anytype.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import com.anytypeio.anytype.core_ui.views.fontInterSemibold
import com.anytypeio.anytype.viewmodel.TierState

@Composable
private fun Tier(
    title: String,
    subTitle: String,
    price: String,
    colorGradient: Color,
    radialGradient: Color,
    icon: Int,
    buttonText: String,
    onClick: () -> Unit
) {
    val brush = Brush.verticalGradient(
        listOf(
            colorGradient,
            Color.Transparent
        )
    )

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
            contentAlignment = androidx.compose.ui.Alignment.BottomStart
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
        PriceOrOption()
        ButtonPrimary(
            text = buttonText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClick = { /*TODO*/ },
            size = ButtonSize.Small
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun PriceOrOption() {
    Text(
        modifier = Modifier.padding(start = 16.dp),
        text = "9.99",
        style = titleTextStyle,
        color = colorResource(id = R.color.text_primary)
    )
}

@Composable
fun TierByType(tier: TierState) {
    when (tier) {
        is TierState.Builder -> {
            Tier(
                title = stringResource(id = R.string.payments_tier_builder),
                subTitle = stringResource(id = R.string.payments_tier_builder_description),
                price = tier.price,
                colorGradient = Color(0xFFE4E7FF),
                radialGradient = Color(0xFFA5AEFF),
                icon = R.drawable.logo_builder,
                buttonText = stringResource(id = R.string.payments_button_learn),
                onClick = { /*TODO*/ }
            )
        }

        is TierState.CoCreator -> {
            Tier(
                title = stringResource(id = R.string.payments_tier_cocreator),
                subTitle = stringResource(id = R.string.payments_tier_cocreator_description),
                price = tier.price,
                colorGradient = Color(0xFFFBEAEA),
                radialGradient = Color(0xFFF05F5F),
                icon = R.drawable.logo_co_creator,
                buttonText = stringResource(id = R.string.payments_button_learn),
                onClick = { /*TODO*/ }
            )
        }

        is TierState.Custom -> {
            Tier(
                title = stringResource(id = R.string.payments_tier_custom),
                subTitle = stringResource(id = R.string.payments_tier_custom_description),
                price = tier.price,
                colorGradient = Color(0xFFFBEAFF),
                radialGradient = Color(0xFFFE86DE3),
                icon = R.drawable.logo_custom,
                buttonText = stringResource(id = R.string.payments_button_learn),
                onClick = { /*TODO*/ }
            )
        }

        is TierState.Explorer -> {
            Tier(
                title = stringResource(id = R.string.payments_tier_explorer),
                subTitle = stringResource(id = R.string.payments_tier_explorer_description),
                price = tier.price,
                colorGradient = Color(0xFFCFFAFF),
                radialGradient = Color(0xFF24BFD4),
                icon = R.drawable.logo_explorer,
                buttonText = stringResource(id = R.string.payments_button_learn),
                onClick = { /*TODO*/ }
            )
        }
    }
}

@Preview
@Composable
fun TierPreview() {
    Tier(
        title = "Explorer",
        subTitle = "Dive into the network and enjoy the thrill of one-on-one collaboration",
        price = "9.99",
        buttonText = "Subscribe",
        onClick = {},
        icon = R.drawable.logo_co_creator,
        colorGradient = Color(0xFFCFF6CF),
        radialGradient = Color(0xFF24BFD4)
    )
}

val titleTextStyle = TextStyle(
    fontFamily = fontInterSemibold,
    fontWeight = FontWeight.W600,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).em
)