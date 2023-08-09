package com.anytypeio.anytype.presentation.templates

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.presentation.editor.cover.CoverColor

sealed class TemplateView {

    data class Blank(
        val typeId: Id, val typeName: String = "", val layout: Int
    ) : TemplateView()

    data class Template(
        val id: Id,
        val name: String,
        val typeId: Id,
        val layout: ObjectType.Layout,
        val emoji: String?,
        val image: String?,
        val coverColor: CoverColor?,
        val coverImage: Url?,
        val coverGradient: String?
    ) : TemplateView() {

        fun isCoverPresent(): Boolean {
            return coverColor != null || coverImage != null || coverGradient != null
        }
    }
}