package com.anytypeio.anytype.payments.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.views.fontRiccioneRegular
import com.anytypeio.anytype.presentation.membership.models.Tier
import com.anytypeio.anytype.payments.viewmodel.PaymentsMainState
import com.anytypeio.anytype.presentation.membership.models.TierId

@Composable
fun MainPaymentsScreen(state: PaymentsMainState, tierClicked: (TierId) -> Unit) {
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
                .verticalScroll(rememberScrollState())
        ) {
            if (state is PaymentsMainState.Default) {
                Title()
                Spacer(modifier = Modifier.height(7.dp))
                Subtitle()
                Spacer(modifier = Modifier.height(32.dp))
                InfoCards()
                Spacer(modifier = Modifier.height(32.dp))
                TiersList(tiers = state.tiers, tierClicked = tierClicked)
                Spacer(modifier = Modifier.height(32.dp))
                LinkButton(text = stringResource(id = R.string.payments_member_link), action = {})
                Divider()
                LinkButton(text = stringResource(id = R.string.payments_privacy_link), action = {})
                Divider()
                LinkButton(text = stringResource(id = R.string.payments_terms_link), action = {})
                Spacer(modifier = Modifier.height(32.dp))
                BottomText()
            }
            if (state is PaymentsMainState.PaymentSuccess) {
                Title()
                Spacer(modifier = Modifier.height(39.dp))
                TiersList(tiers = state.tiers, tierClicked = tierClicked)
                Spacer(modifier = Modifier.height(32.dp))
                LinkButton(text = stringResource(id = R.string.payments_member_link), action = {})
                Divider()
                LinkButton(text = stringResource(id = R.string.payments_privacy_link), action = {})
                Divider()
                LinkButton(text = stringResource(id = R.string.payments_terms_link), action = {})
                Spacer(modifier = Modifier.height(32.dp))
                BottomText()
            }
        }
    }
}

@Composable
private fun Title() {

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
}

@Composable
private fun Subtitle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 60.dp, end = 60.dp),
            text = stringResource(id = R.string.payments_subheader),
            color = colorResource(id = R.color.text_primary),
            style = Relations2,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TiersList(tiers: List<Tier>, tierClicked: (TierId) -> Unit) {
    val itemsScroll = rememberLazyListState(initialFirstVisibleItemIndex = 1)
    LazyRow(
        state = itemsScroll,
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
        flingBehavior = rememberSnapFlingBehavior(lazyListState = itemsScroll)
    ) {
        itemsIndexed(tiers) { _, tier ->
            val resources = mapTierToResources(tier)
            if (resources != null) {
                TierView(
                    title = resources.title,
                    subTitle = resources.subtitle,
                    colorGradient = resources.colorGradient,
                    radialGradient = resources.radialGradient,
                    icon = resources.smallIcon,
                    buttonText = stringResource(id = R.string.payments_button_learn),
                    onClick = { tierClicked.invoke(tier.id) },
                    isCurrent = tier.isCurrent
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfoCards() {
    val cards = infoCardsState()
    val pagerState = rememberPagerState {
        cards.size
    }
    val dotCurrentColor = colorResource(id = R.color.glyph_button)
    val dotColor = colorResource(id = R.color.glyph_inactive)
    Box(modifier = Modifier) {
        HorizontalPager(state = pagerState) { index ->
            val card = cards[index]
            InfoCard(
                gradient = card.gradient,
                title = card.title,
                subtitle = card.subtitle,
                image = card.image
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(cards.size) { iteration ->
                val color =
                    if (pagerState.currentPage == iteration) dotCurrentColor else dotColor
                Box(
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .background(color, CircleShape)
                        .size(6.dp)
                )
            }
        }
    }
}

@Composable
fun LinkButton(text: String, action: () -> Unit) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .noRippleThrottledClickable { action.invoke() }
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart),
            text = text,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary)
        )
        Image(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterEnd),
            painter = painterResource(id = R.drawable.ic_web_link),
            contentDescription = "web link icon"
        )
    }
}

@Composable
fun BottomText() {
    val start = stringResource(id = R.string.payments_let_us_link_start)
    val end = stringResource(id = R.string.payments_let_us_link_end)
    val buildString = buildAnnotatedString {
        append(start)
        append(" ")
        append(end)
        pushStringAnnotation(
            tag = "link", annotation = "www.anytype.io"
        )
        addStyle(
            style = SpanStyle(textDecoration = TextDecoration.Underline),
            start = start.length + 1,
            end = start.length + 1 + end.length
        )
        pop()
    }
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .wrapContentHeight(),
        text = buildString,
        style = Caption1Regular,
        color = colorResource(id = R.color.text_primary)
    )
}

@Preview
@Composable
fun MainPaymentsScreenPreview() {
    val tiers = listOf(
        Tier.Explorer(TierId("999"), isCurrent = true, validUntil = "2022-12-31"),
        Tier.Builder(TierId("999"), isCurrent = true, validUntil = "2022-12-31"),
        Tier.CoCreator(TierId("999"), isCurrent = false, validUntil = "2022-12-31"),
        Tier.Custom(TierId("999"), isCurrent = false, validUntil = "2022-12-31")
    )
    MainPaymentsScreen(PaymentsMainState.PaymentSuccess(tiers), {})
}

val headerTextStyle = TextStyle(
    fontFamily = fontRiccioneRegular,
    fontWeight = FontWeight.W400,
    fontSize = 48.sp,
    lineHeight = 48.sp,
    letterSpacing = (-0.010833).em
)