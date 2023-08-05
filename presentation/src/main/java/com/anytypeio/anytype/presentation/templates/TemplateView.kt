package com.anytypeio.anytype.presentation.templates

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType

sealed class TemplateView {

    data class Blank(
        val typeId: Id, val typeName: String, val layout: Int
    ) : TemplateView()

    data class Template(
        val id: Id,
        val name: String,
        val typeId: Id,
        val layout: ObjectType.Layout,
        val emoji: String?,
        val image: String?
    ) : TemplateView()
}