package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.ObjectTypeIds.NOTE
import com.anytypeio.anytype.core_models.ObjectTypeIds.PAGE
import com.anytypeio.anytype.core_models.ObjectTypeIds.SET
import com.anytypeio.anytype.core_models.ObjectTypeIds.TASK

sealed class ObjectTypeItemView {
    data class Type(val view: ObjectTypeView) : ObjectTypeItemView()
    sealed class Section : ObjectTypeItemView() {
        object Library : Section()
        object Marketplace : Section()
    }
}

data class ObjectTypeView(
    val id: String,
    val name: String,
    val description: String?,
    val emoji: String?,
    val isSelected: Boolean = false
)

class ObjectTypeViewComparator : Comparator<ObjectTypeView> {

    /**
     * Sort object types by PAGE, NOTE, SET, TASK ids and others
     */
    override fun compare(o1: ObjectTypeView, o2: ObjectTypeView): Int {
        val o1Url = o1.id
        val o2Url = o2.id
        if (o1Url == o2Url) return 0

        if (o1Url == PAGE && o2Url != PAGE) return -1
        if (o1Url != PAGE && o2Url == PAGE) return 1

        if (o1Url == NOTE && o2Url != NOTE) return -1
        if (o1Url != NOTE && o2Url == NOTE) return 1

        if (o1Url == SET && o2Url != SET) return -1
        if (o1Url != SET && o2Url == SET) return 1

        if (o1Url == TASK && o2Url != TASK) return -1
        if (o1Url != TASK && o2Url == TASK) return 1

        val o1Name = o1.name
        val o2Name = o2.name

        return o1Name.compareTo(o2Name)
    }
}
