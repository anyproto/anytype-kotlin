package com.anytypeio.anytype.presentation.sets.model

sealed class ViewerRelationListView {
    sealed class Section : ViewerRelationListView() {
        object Settings : Section()
        object Relations: Section()
    }
    sealed class Setting : ViewerRelationListView() {
        sealed class CardSize : Setting() {
            object Small : CardSize()
            object Large : CardSize()
        }
//        object Icon : Setting() {
//            object None : CardSize()
//            object Small : CardSize()
//            object Big : CardSize()
//        }
        sealed class Toggle : Setting() {
            abstract val toggled: Boolean
            data class FitImage(override val toggled: Boolean) : Toggle()
            data class HideIcon(override val toggled: Boolean) : Toggle()
        }
        sealed class ImagePreview : Setting() {
            object None : ImagePreview()
            object Cover : ImagePreview()
            data class Custom(val name: String) : ImagePreview()
        }
    }
    data class Relation(val view: SimpleRelationView) : ViewerRelationListView()
}
