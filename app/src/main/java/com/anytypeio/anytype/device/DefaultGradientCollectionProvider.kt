package com.anytypeio.anytype.device

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.cover.GradientCollectionProvider
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient

class DefaultGradientCollectionProvider : GradientCollectionProvider {
    override fun provide(): List<Id> = listOf(
        CoverGradient.YELLOW, CoverGradient.RED, CoverGradient.BLUE, CoverGradient.TEAL
    )
}