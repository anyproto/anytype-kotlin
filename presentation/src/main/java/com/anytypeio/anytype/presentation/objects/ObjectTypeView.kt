package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectType.Companion.NOTE_URL
import com.anytypeio.anytype.core_models.ObjectType.Companion.PAGE_URL

sealed class ObjectTypeView {

    abstract val id: String

    data class Item(
        override val id: String,
        val name: String,
        val description: String?,
        val emoji: String?,
        val layout: ObjectType.Layout
    ) : ObjectTypeView()

    data class Search(
        override val id: String = ""
    ) : ObjectTypeView()
}

fun MutableList<ObjectTypeView>.sortByType(
    defaultType: String?
): List<ObjectTypeView> {
    if (defaultType == NOTE_URL) {
        this.removeAll { it.id == defaultType }
        val index = this.indexOfFirst { it.id == PAGE_URL }
        if (index != -1) {
            val item = this[index]
            this.removeAt(index)
            this.add(0, item)
        }
    } else {
        this.removeAll { it.id == defaultType }
        val index = this.indexOfFirst { it.id == NOTE_URL }
        if (index != -1) {
            val item = this[index]
            this.removeAt(index)
            this.add(0, item)
        }
    }
    return this
}
