package com.anytypeio.anytype.presentation.relations.model

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.objects.ObjectIcon

data class DefaultObjectTypeView(
    val id: Id,
    val title: String,
    val subtitle: String?,
    val icon: ObjectIcon
)

data class SelectLimitObjectTypeView(
    val item: DefaultObjectTypeView,
    val isSelected: Boolean
)

data class LimitObjectTypeValueView(val types: List<DefaultObjectTypeView>)