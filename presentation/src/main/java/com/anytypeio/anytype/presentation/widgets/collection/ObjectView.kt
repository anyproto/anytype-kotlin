package com.anytypeio.anytype.presentation.widgets.collection

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView

sealed interface CollectionObjectView {
    val obj: DefaultObjectView
    val isSelected: Boolean
}

sealed class CollectionView {

    data class ObjectView(
        override val obj: DefaultObjectView,
        override val isSelected: Boolean = false
    ) : CollectionView(), CollectionObjectView

    data class FavoritesView(
        override val obj: DefaultObjectView,
        val blockId: Id = "",
        override val isSelected: Boolean = false
    ) : CollectionView(), CollectionObjectView

    data class SectionView(
        val name: String
    ) : CollectionView()
}