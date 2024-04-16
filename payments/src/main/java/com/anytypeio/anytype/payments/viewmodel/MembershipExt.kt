package com.anytypeio.anytype.payments.viewmodel

import com.anytypeio.anytype.payments.constants.TiersConstants.BUILDER_ID
import com.anytypeio.anytype.payments.constants.TiersConstants.CO_CREATOR_ID
import com.anytypeio.anytype.payments.constants.TiersConstants.EXPLORER_ID
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.Tier
import com.anytypeio.anytype.presentation.membership.models.TierId

fun MembershipStatus.toTiersView(): List<Tier> {
    return this.tiers.map { tier ->
        when (tier.id) {
            EXPLORER_ID -> Tier.Explorer(
                id = TierId(tier.id),
                isCurrent = isCurrentTier(EXPLORER_ID),
                prettyName = tier.name,
                color = tier.colorStr,
                features = tier.features
            )
            BUILDER_ID -> Tier.Builder(
                id = TierId(tier.id),
                isCurrent = isCurrentTier(BUILDER_ID),
                prettyName = tier.name,
                color = tier.colorStr,
                features = tier.features
            )
            CO_CREATOR_ID -> Tier.CoCreator(
                id = TierId(tier.id),
                isCurrent = isCurrentTier(CO_CREATOR_ID),
                prettyName = tier.name,
                color = tier.colorStr,
                features = tier.features

            )
            else -> Tier.Custom(
                id = TierId(tier.id),
                isCurrent = isCurrentTier(tier.id),
                prettyName = tier.name,
                color = tier.colorStr,
                features = tier.features
            )
        }
    }
}

private fun MembershipStatus.isCurrentTier(tierId: Int): Boolean {
    return when (this) {
        is MembershipStatus.Active -> activeTier.id == tierId
        else -> false
    }
}