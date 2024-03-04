package com.anytypeio.anytype.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
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
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.views.fontRiccioneRegular
import com.anytypeio.anytype.viewmodel.PaymentsState

@Composable
fun MainPaymentsScreen(state: PaymentsState) {
    Box(
        modifier = Modifier
            .nestedScroll(rememberNestedScrollInteropConnection())
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp)
        ) {
            Header(state = state)
        }
    }
}

@Composable
private fun Header(state: PaymentsState) {

    // Dragger at the top, centered
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Dragger(modifier = Modifier.align(Alignment.Center))
    }

    // Main content box
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 37.dp),
            text = stringResource(id = R.string.payments_header),
            color = colorResource(id = R.color.text_primary),
            style = headerTextStyle,
            textAlign = TextAlign.Center
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 60.dp, end = 60.dp, top = 7.dp),
            text = stringResource(id = R.string.payments_subheader),
            color = colorResource(id = R.color.text_primary),
            style = Relations2,
            textAlign = TextAlign.Center
        )
    }

    val brush = Brush.verticalGradient(
        listOf(
            colorResource(id = R.color.payments_main_color_end),
            colorResource(id = R.color.payments_main_color_start)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 32.dp)
            .background(brush),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 39.dp, end = 39.dp, top = 32.dp)
                .verticalScroll(rememberScrollState()),
            painter = painterResource(id = R.drawable.iamge_payments_main),
            contentDescription = "Main payments image"
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 39.dp, end = 39.dp, top = 7.dp),
            text = stringResource(id = R.string.payments_text),
            color = colorResource(id = R.color.text_primary),
            style = Relations2,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Preview
@Composable
fun MainPaymentsScreenPreview() {
    MainPaymentsScreen(PaymentsState.Success)
}

val headerTextStyle = TextStyle(
    fontFamily = fontRiccioneRegular,
    fontWeight = FontWeight.W400,
    fontSize = 48.sp,
    lineHeight = 48.sp,
    letterSpacing = (-0.010833).em
)
