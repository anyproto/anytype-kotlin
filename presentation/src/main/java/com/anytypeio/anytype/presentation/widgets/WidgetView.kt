package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.editor.model.Indent

sealed class WidgetView {
    data class Tree(
        val id: Id,
        val obj: ObjectWrapper.Basic,
        val elements: List<Element>
    ) : WidgetView() {
        data class Element(
            // TODO val icon: WidgetView.Tree.Icon
            val indent: Indent,
            val obj: ObjectWrapper.Basic,
            val hasChildren: Boolean,
            val path: String
        )

        sealed class Icon {
            data class Branch(val isExpanded: Boolean) : Icon()
            object Leaf : Icon()
        }
    }
}