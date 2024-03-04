package com.anytypeio.anytype.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
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

@Composable
fun Tier(
    title: String,
    subTitle: String,
    price: String,
    colorGradientStart: Int = R.color.payments_main_color_start,
    colorGradientEnd: Int = R.color.payments_main_color_end,
    icon: Int,
    buttonText: String,
    onClick: () -> Unit
) {
    val brush = Brush.verticalGradient(
        listOf(
            colorResource(id = colorGradientEnd), colorResource(id = colorGradientStart)
        )
    )

    Column(
        modifier = Modifier
            .width(192.dp)
            .wrapContentHeight()
            .background(brush)
            .noRippleThrottledClickable { onClick() }
    ) {
        Icon(
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp),
            painter = painterResource(id = icon),
            contentDescription = "logo"
        )
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

@Preview
@Composable
fun TierPreview() {
    Tier(
        title = "Explorer",
        subTitle = "Dive into the network and enjoy the thrill of one-on-one collaboration",
        price = "9.99",
        buttonText = "Subscribe",
        onClick = {},
        icon = R.drawable.logo_co_creator
    )
}

val titleTextStyle = TextStyle(
    fontFamily = fontInterSemibold,
    fontWeight = FontWeight.W600,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).em
)