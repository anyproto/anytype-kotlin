package com.anytypeio.anytype.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.Relations2

@Composable
fun InfoCard(
    image: Int,
    gradient: Brush,
    title: String,
    subtitle: String,
) {
    val configuration = LocalConfiguration.current

    Column(
        modifier = Modifier
            .width(configuration.screenWidthDp.dp)
            .height(284.dp)
            .background(color = colorResource(id = R.color.shape_tertiary)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(136.dp)
                .background(gradient)
                .verticalScroll(rememberScrollState())
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                painter = painterResource(id = image),
                contentDescription = "Main payments image"
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            text = title,
            color = colorResource(id = R.color.text_primary),
            style = BodyBold,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .padding(start = 32.dp, end = 32.dp, top = 6.dp)
                .verticalScroll(rememberScrollState()),
            text = subtitle,
            color = colorResource(id = R.color.text_primary),
            style = Relations2,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun infoCardsState() = listOf(
    InfoCardState(
        image = R.drawable.payments_card_0,
        title = stringResource(id = R.string.payments_card_text_1),
        subtitle = stringResource(id = R.string.payments_card_description_1),
        gradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFCFF6CF),
                Color.Transparent
            )
        )
    ),
    InfoCardState(
        image = R.drawable.payments_card_1,
        title = stringResource(id = R.string.payments_card_text_2),
        subtitle = stringResource(id = R.string.payments_card_description_2),
        gradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFEF2C6),
                Color.Transparent
            )
        )
    ),
    InfoCardState(
        image = R.drawable.payments_card_2,
        title = stringResource(id = R.string.payments_card_text_3),
        subtitle = stringResource(id = R.string.payments_card_description_3),
        gradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFEBEB),
                Color.Transparent
            )
        )
    ),
    InfoCardState(
        image = R.drawable.payments_card_3,
        title = stringResource(id = R.string.payments_card_text_4),
        subtitle = stringResource(id = R.string.payments_card_description_4),
        gradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFEBEDFE),
                Color.Transparent
            )
        )
    )
)

data class InfoCardState(
    val image: Int,
    val title: String,
    val subtitle: String,
    val gradient: Brush
)