package com.anytypeio.anytype.presentation.sets.model

sealed class ViewerRelationListView {
    data class Relation(val view: SimpleRelationView) : ViewerRelationListView()
}
