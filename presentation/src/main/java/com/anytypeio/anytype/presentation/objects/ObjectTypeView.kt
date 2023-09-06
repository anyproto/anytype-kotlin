package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectTypeIds.COLLECTION
import com.anytypeio.anytype.core_models.ObjectTypeIds.NOTE
import com.anytypeio.anytype.core_models.ObjectTypeIds.PAGE
import com.anytypeio.anytype.core_models.ObjectTypeIds.TASK

sealed class ObjectTypeItemView {
    data class Type(val view: ObjectTypeView) : ObjectTypeItemView()
    sealed class Section : ObjectTypeItemView() {
        object Library : Section()
        object Marketplace : Section()
    }
    data class EmptyState(val query: String) : ObjectTypeItemView()
}

data class ObjectTypeView(
    val id: Id,
    val key: Key,
    val name: String,
    val description: String?,
    val emoji: String?,
    val isSelected: Boolean = false
)

class ObjectTypeViewComparator : Comparator<ObjectTypeView> {

    /**
     * Sort object types by PAGE, NOTE, COLLECTION, TASK ids and others
     */
    override fun compare(o1: ObjectTypeView, o2: ObjectTypeView): Int {
        val o1Key = o1.key
        val o2Key = o2.key

        if (o1Key == o2Key) return 0

        if (o1Key == PAGE && o2Key != PAGE) return -1
        if (o1Key != PAGE && o2Key == PAGE) return 1

        if (o1Key == NOTE && o2Key != NOTE) return -1
        if (o1Key != NOTE && o2Key == NOTE) return 1

        if (o1Key == COLLECTION && o2Key != COLLECTION) return -1
        if (o1Key != COLLECTION && o2Key == COLLECTION) return 1

        if (o1Key == TASK && o2Key != TASK) return -1
        if (o1Key != TASK && o2Key == TASK) return 1

        val o1Name = o1.name
        val o2Name = o2.name

        return o1Name.compareTo(o2Name)
    }
}
