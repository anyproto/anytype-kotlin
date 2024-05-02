package com.anytypeio.anytype.payments.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.payments.R
import com.anytypeio.anytype.payments.mapping.LocalizedPeriodString
import com.anytypeio.anytype.payments.models.TierConditionInfo
import com.anytypeio.anytype.payments.models.TierPeriod

@Composable
fun ConditionInfoView(
    state: TierConditionInfo
) {
    when (state) {
        is TierConditionInfo.Hidden -> {
            // Do nothing
        }

        is TierConditionInfo.Visible.LoadingBillingClient -> {
            ConditionInfoViewPriceAndText(
                period = stringResource(id = R.string.membership_price_pending),
                price = ""
            )
        }

        is TierConditionInfo.Visible.Valid -> {
            val validUntilDate = formatTimestamp(state.dateEnds)
            val result = when (state.period) {
                TierPeriod.Unknown -> stringResource(id = R.string.three_dots_text_placeholder)
                TierPeriod.Unlimited -> stringResource(id = R.string.payments_tier_details_free_forever)
                else -> validUntilDate
            }
            ConditionInfoViewValid(text = result)
        }

        is TierConditionInfo.Visible.Price -> {
            val periodString = stringResource(id = R.string.per_period, getDate(state.period))
            ConditionInfoViewPriceAndText(state.price, periodString)
        }

        is TierConditionInfo.Visible.PriceBilling -> {
            val periodString = stringResource(
                id = R.string.per_period,
                LocalizedPeriodString(desc = state.price.period)
            )
            ConditionInfoViewPriceAndText(
                price = state.price.formattedPrice,
                period = periodString
            )
        }

        is TierConditionInfo.Visible.Free -> {
            ConditionInfoViewFree(text = stringResource(id = R.string.three_dots_text_placeholder))
        }

        is TierConditionInfo.Visible.Error -> {
            ConditionInfoViewPriceAndText(price = "", period = state.message)
        }
    }
}

@Composable
private fun ConditionInfoViewPriceAndText(price: String, period: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
    ) {
        Text(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.CenterVertically),
            text = price,
            color = colorResource(id = R.color.text_primary),
            style = HeadlineTitle,
            textAlign = TextAlign.Start
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Bottom)
                .padding(start = 4.dp, bottom = 1.dp),
            text = period,
            color = colorResource(id = R.color.text_primary),
            style = Relations1,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun ConditionInfoViewValid(text: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = stringResource(id = R.string.payments_tier_current_title),
            color = colorResource(id = R.color.text_primary),
            style = BodyBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp)
                .background(
                    shape = RoundedCornerShape(12.dp),
                    color = colorResource(id = R.color.payments_tier_current_background)
                ),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 34.dp),
                text = stringResource(id = R.string.payments_tier_current_valid),
                color = colorResource(id = R.color.text_primary),
                style = Relations2,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                text = text,
                color = colorResource(id = R.color.text_primary),
                style = HeadlineTitle,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 23.dp),
                text = stringResource(id = R.string.payments_tier_current_paid_by),
                color = colorResource(id = R.color.text_secondary),
                style = Relations2,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ConditionInfoViewFree(text: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = stringResource(id = R.string.payments_tier_current_title),
            color = colorResource(id = R.color.text_primary),
            style = BodyBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp)
                .background(
                    shape = RoundedCornerShape(12.dp),
                    color = colorResource(id = R.color.payments_tier_current_background)
                ),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 34.dp),
                text = stringResource(id = R.string.payments_tier_current_valid),
                color = colorResource(id = R.color.text_primary),
                style = Relations2,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                text = text,
                color = colorResource(id = R.color.text_primary),
                style = HeadlineTitle,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
fun MyConditionInfoViewForeverFree() {
    ConditionInfoView(
        state = TierConditionInfo.Visible.Valid(
            period = TierPeriod.Unlimited,
            dateEnds = 0,
            payedBy = MembershipPaymentMethod.METHOD_CRYPTO
        )
    )
}

@Preview
@Composable
fun MyConditionInfoViewUntilDate() {
    ConditionInfoView(
        state = TierConditionInfo.Visible.Valid(
            period = TierPeriod.Year(1),
            dateEnds = 1714199910L,
            payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE
        )
    )
}

@Preview
@Composable
fun MyConditionInfoViewInknown() {
    ConditionInfoView(
        state = TierConditionInfo.Visible.Valid(
            period = TierPeriod.Unknown,
            dateEnds = 1714199910L,
            payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE
        )
    )
}

@Preview
@Composable
fun MyConditionInfoViewFree() {
    ConditionInfoView(
        state = TierConditionInfo.Visible.Free(
            period = TierPeriod.Unknown
        )
    )
}

@Preview
@Composable
fun MyConditionInfoViewPrice1() {
    ConditionInfoView(TierConditionInfo.Visible.Price("$99", TierPeriod.Year(1)))
}

@Preview
@Composable
fun MyConditionInfoViewPrice2() {
    ConditionInfoView(TierConditionInfo.Visible.Price("$99", TierPeriod.Year(2)))
}

@Preview
@Composable
fun MyConditionInfoViewLoading() {
    ConditionInfoView(TierConditionInfo.Visible.LoadingBillingClient)
}

