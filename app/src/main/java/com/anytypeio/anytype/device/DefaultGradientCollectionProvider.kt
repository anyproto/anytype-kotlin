package com.anytypeio.anytype.device

import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.cover.GradientCollectionProvider
import com.anytypeio.anytype.presentation.page.cover.CoverGradient

class DefaultGradientCollectionProvider : GradientCollectionProvider {
    override fun provide(): List<Id> = listOf(
        CoverGradient.YELLOW, CoverGradient.RED, CoverGradient.BLUE, CoverGradient.TEAL
    )
}