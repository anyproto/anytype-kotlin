package com.anytypeio.anytype.payments.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
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
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.payments.R
import com.anytypeio.anytype.payments.mapping.LocalizedPeriodString
import com.anytypeio.anytype.payments.mapping.toPeriodDescription
import com.anytypeio.anytype.payments.models.BillingPriceInfo
import com.anytypeio.anytype.payments.models.PeriodDescription
import com.anytypeio.anytype.payments.models.PeriodUnit
import com.anytypeio.anytype.payments.models.TierConditionInfo
import com.anytypeio.anytype.payments.models.TierPeriod
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ConditionInfoPreview(
    state: TierConditionInfo
) {
    when (state) {
        is TierConditionInfo.Hidden -> {
            // Do nothing
        }

        is TierConditionInfo.Visible.LoadingBillingClient -> {
            ConditionInfoPreviewText(text = stringResource(id = R.string.membership_price_pending))
        }

        is TierConditionInfo.Visible.Valid -> {
            val validUntilDate = formatTimestamp(state.dateEnds)
            val result = when (state.period)  {
                TierPeriod.Unknown -> stringResource(id = R.string.membership_valid_for_unknown)
                TierPeriod.Unlimited -> stringResource(id = R.string.payments_tier_details_free_forever)
                else -> stringResource(id = R.string.payments_tier_details_valid_until, validUntilDate)
            }
            ConditionInfoPreviewText(text = result)
        }

        is TierConditionInfo.Visible.Price -> {
            val periodString = stringResource(id = R.string.per_period, getDate(state.period))
            ConditionInfoPreviewPriceAndText(state.price, periodString)
        }

        is TierConditionInfo.Visible.PriceBilling -> {
            val periodString = stringResource(id = R.string.per_period, LocalizedPeriodString(desc = state.price.period))
            ConditionInfoPreviewPriceAndText(
                price = state.price.formattedPrice,
                period = periodString
            )
        }

        is TierConditionInfo.Visible.Free -> {
            //todo: add some refactoring here
            val text = when (state.period) {
                is TierPeriod.Day -> stringResource(id = R.string.free_for, getDate(state.period))
                is TierPeriod.Month -> stringResource(id = R.string.free_for, getDate(state.period))
                TierPeriod.Unknown -> stringResource(id = R.string.free_for_unknown)
                TierPeriod.Unlimited -> stringResource(id = R.string.payments_tier_details_free_forever)
                is TierPeriod.Week -> stringResource(id = R.string.free_for, getDate(state.period))
                is TierPeriod.Year -> stringResource(id = R.string.free_for, getDate(state.period))
            }
            ConditionInfoPreviewText(text = text)
        }

        is TierConditionInfo.Visible.Error -> {
            ConditionInfoPreviewText(text = state.message, textColor = R.color.palette_dark_red)
        }

        TierConditionInfo.Visible.Pending -> ConditionInfoPreviewText(text = stringResource(id = R.string.membership_price_pending))
    }
}

@Composable
fun getDate(tierPeriod: TierPeriod): String {
    tierPeriod.toPeriodDescription().let { desc ->
        return LocalizedPeriodString(desc)
    }
}

@Composable
private fun ConditionInfoPreviewText(text: String, textColor: Int = R.color.text_primary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = text,
            color = colorResource(id = textColor),
            style = Caption1Regular,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ConditionInfoPreviewPriceAndText(price: String, period: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.CenterVertically),
            text = price,
            color = colorResource(id = R.color.text_primary),
            style = BodyBold,
            textAlign = TextAlign.Start
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Bottom)
                .padding(start = 4.dp, bottom = 2.dp),
            text = period,
            color = colorResource(id = R.color.text_primary),
            style = Caption1Regular,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

fun formatTimestamp(timestamp: Long, locale: java.util.Locale = java.util.Locale.getDefault()): String {
    val dateTime = Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    val formatter = DateTimeFormatter.ofPattern("d MMM uuuu", locale)
    return dateTime.format(formatter)
}

@Preview
@Composable
fun MyConditionInfoPreview1() {
    ConditionInfoPreview(TierConditionInfo.Visible.Price("$99", TierPeriod.Year(1)))
}

@Preview
@Composable
fun MyConditionInfoPreview2() {
    ConditionInfoPreview(TierConditionInfo.Visible.Price("$99", TierPeriod.Year(2)))
}

@Preview
@Composable
fun MyConditionInfoPreview3() {
    ConditionInfoPreview(TierConditionInfo.Visible.LoadingBillingClient)
}

@Preview
@Composable
fun MyConditionInfoPreview4() {
    ConditionInfoPreview(TierConditionInfo.Visible.Error("Error message"))
}

@Preview
@Composable
fun MyConditionInfoPreview5() {
    ConditionInfoPreview(TierConditionInfo.Visible.Free(TierPeriod.Unlimited))
}

@Preview
@Composable
fun MyConditionInfoPreview6() {
    ConditionInfoPreview(TierConditionInfo.Visible.Free(TierPeriod.Year(1)))
}

@Preview
@Composable
fun MyConditionInfoPreview7() {
    ConditionInfoPreview(TierConditionInfo.Visible.Free(TierPeriod.Year(2)))
}

@Preview
@Composable
fun MyConditionInfoPreview8() {
    ConditionInfoPreview(TierConditionInfo.Visible.Free(TierPeriod.Unknown))
}

@Preview
@Composable
fun MyConditionInfoValidValidUntilDate() {
    ConditionInfoPreview(
        TierConditionInfo.Visible.Valid(
            dateEnds = 1714199910L,
            payedBy = MembershipPaymentMethod.METHOD_CRYPTO,
            period = TierPeriod.Year(1)
        )
    )
}

@Preview
@Composable
fun MyConditionInfoValidForever() {
    ConditionInfoPreview(
        TierConditionInfo.Visible.Valid(
            dateEnds = 1714199910L,
            payedBy = MembershipPaymentMethod.METHOD_CRYPTO,
            period = TierPeriod.Unlimited
        )
    )
}

@Preview
@Composable
fun MyConditionInfoValidUnknown() {
    ConditionInfoPreview(
        TierConditionInfo.Visible.Valid(
            dateEnds = 1714199910L,
            payedBy = MembershipPaymentMethod.METHOD_CRYPTO,
            period = TierPeriod.Unknown
        )
    )
}

@Preview
@Composable
fun MyConditionInfoValidFree() {
    ConditionInfoPreview(
        TierConditionInfo.Visible.Valid(
            dateEnds = 0,
            payedBy = MembershipPaymentMethod.METHOD_CRYPTO,
            period = TierPeriod.Unlimited
        )
    )
}

@Preview
@Composable
fun MyConditionInfoPriceBilling1() {
    ConditionInfoPreview(
        TierConditionInfo.Visible.PriceBilling(
            price = BillingPriceInfo(
                formattedPrice = "$99",
                period = PeriodDescription(1, PeriodUnit.YEARS)
            ),
        )
    )
}

@Preview
@Composable
fun MyConditionInfoPriceBilling2() {
    ConditionInfoPreview(
        TierConditionInfo.Visible.PriceBilling(
            price = BillingPriceInfo(
                formattedPrice = "$300",
                period = PeriodDescription(3, PeriodUnit.YEARS)
            ),
        )
    )
}