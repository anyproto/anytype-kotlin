package com.anytypeio.anytype.presentation.editor.editor.slash

import com.anytypeio.anytype.core_utils.diff.DefaultObjectDiffIdentifier
import com.anytypeio.anytype.presentation.relations.DocumentRelationView

sealed class SlashRelationView : DefaultObjectDiffIdentifier {
    data class Item(val view: DocumentRelationView) : SlashRelationView() {
        override val identifier: String get() = view.identifier
    }

    sealed class Section : SlashRelationView() {
        object Subheader : Section() {
            override val identifier: String get() = javaClass.name
        }

        object SubheaderWithBack : Section() {
            override val identifier: String get() = javaClass.name
        }
    }
    object RelationNew : SlashRelationView() {
        override val identifier: String get() = javaClass.name
    }
}