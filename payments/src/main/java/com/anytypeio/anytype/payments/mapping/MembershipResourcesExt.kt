package com.anytypeio.anytype.payments.mapping

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.payments.constants.TiersConstants
import com.anytypeio.anytype.payments.models.PeriodDescription
import com.anytypeio.anytype.payments.models.PeriodUnit
import com.anytypeio.anytype.payments.models.TierPeriod
import com.anytypeio.anytype.presentation.membership.models.TierId
import java.time.Period

fun TierId.getTierTitle(): Int {
    return when (this.value) {
        TiersConstants.EXPLORER_ID -> R.string.payments_tier_explorer
        TiersConstants.BUILDER_ID -> R.string.payments_tier_builder
        TiersConstants.CO_CREATOR_ID -> R.string.payments_tier_cocreator
        else -> R.string.payments_tier_custom
    }
}

fun TierId.getTierSubtitle(): Int {
    return when (value) {
        TiersConstants.EXPLORER_ID -> R.string.payments_tier_explorer_description
        TiersConstants.BUILDER_ID -> R.string.payments_tier_builder_description
        TiersConstants.CO_CREATOR_ID -> R.string.payments_tier_cocreator_description
        else -> R.string.payments_tier_custom_description
    }
}

fun String.parsePeriod(): PeriodDescription? {
    return try {
        val period = Period.parse(this)
        when {
            period.years > 0 -> PeriodDescription(period.years, PeriodUnit.YEARS)
            period.months > 0 -> PeriodDescription(period.months, PeriodUnit.MONTHS)
            period.days > 0 -> PeriodDescription(period.days, PeriodUnit.DAYS)
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun LocalizedPeriodString(desc: PeriodDescription?): String {
    desc ?: return ""
    val quantityStringId = when (desc.unit) {
        PeriodUnit.YEARS -> R.plurals.period_years
        PeriodUnit.MONTHS -> R.plurals.period_months
        PeriodUnit.DAYS -> R.plurals.period_days
        PeriodUnit.WEEKS -> R.plurals.period_weeks
    }
    return pluralStringResource(
        id = quantityStringId,
        count = desc.amount,
        formatArgs = arrayOf(desc.amount)
    )
}

fun TierPeriod.toPeriodDescription(): PeriodDescription {
    return when (this) {
        is TierPeriod.Unknown -> PeriodDescription(0, PeriodUnit.DAYS)
        is TierPeriod.Unlimited -> PeriodDescription(Int.MAX_VALUE, PeriodUnit.DAYS)
        is TierPeriod.Year -> PeriodDescription(count, PeriodUnit.YEARS)
        is TierPeriod.Month -> PeriodDescription(count, PeriodUnit.MONTHS)
        is TierPeriod.Week -> PeriodDescription(count, PeriodUnit.WEEKS)
        is TierPeriod.Day -> PeriodDescription(count, PeriodUnit.DAYS)
    }
}