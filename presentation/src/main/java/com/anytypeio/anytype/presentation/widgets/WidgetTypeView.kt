package com.anytypeio.anytype.presentation.widgets

sealed class WidgetTypeView {
    abstract val isSelected: Boolean
    data class List(override val isSelected: Boolean) : WidgetTypeView()
    data class Tree(override val isSelected: Boolean): WidgetTypeView()
    data class Link(override val isSelected: Boolean): WidgetTypeView()
}