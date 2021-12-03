package com.anytypeio.anytype.presentation.sets.viewer

import com.anytypeio.anytype.core_models.Id

sealed class ViewerImagePreviewSelectView {
    sealed class Item : ViewerImagePreviewSelectView() {
        abstract val isSelected: Boolean
        data class None(override val isSelected: Boolean) : Item()
        data class Cover(override val isSelected: Boolean) : Item()
        data class Relation(val id: Id, val name: String, override val isSelected: Boolean) : Item()
    }
    sealed class Section: ViewerImagePreviewSelectView() {
        object Relations: Section()
    }
}
