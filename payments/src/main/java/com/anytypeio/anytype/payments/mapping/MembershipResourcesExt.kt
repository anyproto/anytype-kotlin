package com.anytypeio.anytype.payments.mapping

import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.payments.constants.TiersConstants
import com.anytypeio.anytype.presentation.membership.models.TierId

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