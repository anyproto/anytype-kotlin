package com.anytypeio.anytype.presentation.templates

import com.anytypeio.anytype.core_models.Id

sealed class TemplateView {

    data class Blank(
        val typeId: Id, val typeName: String, val layout: Int
    ) : TemplateView()

    data class Template(val id: Id, val name: String) : TemplateView()
}