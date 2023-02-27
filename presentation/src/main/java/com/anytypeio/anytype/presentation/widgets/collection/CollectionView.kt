package com.anytypeio.anytype.presentation.widgets.collection

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView

data class CollectionView(
    val obj: DefaultObjectView,
    val blockId: Id = "",
    val isSelected: Boolean = false
)