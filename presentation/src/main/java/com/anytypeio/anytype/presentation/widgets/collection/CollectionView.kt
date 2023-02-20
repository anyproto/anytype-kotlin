package com.anytypeio.anytype.presentation.widgets.collection

import com.anytypeio.anytype.presentation.navigation.DefaultObjectView

data class CollectionView(
    val obj: DefaultObjectView,
    val isSelected: Boolean = false
)