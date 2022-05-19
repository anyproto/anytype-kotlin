package com.anytypeio.anytype.device

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.cover.GradientCollectionProvider
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient

object DefaultGradientCollectionProvider : GradientCollectionProvider {
    override fun provide(): List<Id> = CoverGradient.default
}