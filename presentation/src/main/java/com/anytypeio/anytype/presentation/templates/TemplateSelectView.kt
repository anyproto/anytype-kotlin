package com.anytypeio.anytype.presentation.templates

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType

sealed interface TemplateSelectView {
    data class Blank(
        val typeId: Id,
        val typeName: String = "",
        val layout: Int,
    ) : TemplateSelectView

    data class Template(
        val id: Id,
        val typeId: Id,
        val layout: ObjectType.Layout,
    ) : TemplateSelectView
}