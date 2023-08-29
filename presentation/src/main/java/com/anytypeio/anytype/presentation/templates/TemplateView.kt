package com.anytypeio.anytype.presentation.templates

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.presentation.editor.cover.CoverColor

sealed class TemplateView {

    abstract val isDefault: Boolean

    data class Blank(
        val typeId: Id,
        val typeName: String = "",
        val layout: Int,
        override val isDefault: Boolean = false
    ) : TemplateView()

    data class New(val targetObjectType: Id) : TemplateView() {
        override val isDefault: Boolean = false
    }

    data class Template(
        val id: Id,
        val name: String,
        val typeId: Id,
        val layout: ObjectType.Layout,
        val emoji: String?,
        val image: String?,
        val coverColor: CoverColor?,
        val coverImage: Url?,
        val coverGradient: String?,
        override val isDefault: Boolean = false
    ) : TemplateView() {

        fun isCoverPresent(): Boolean {
            return coverColor != null || coverImage != null || coverGradient != null
        }

        fun isImageOrEmojiPresent(): Boolean {
            return image != null || emoji != null
        }
    }
}

sealed class TemplateMenuClick {
    data class Default(val templateView: TemplateView.Template) : TemplateMenuClick()
    data class Edit(val templateView: TemplateView.Template) : TemplateMenuClick()
    data class Duplicate(val templateView: TemplateView.Template) : TemplateMenuClick()
    data class Delete(val templateView: TemplateView.Template) : TemplateMenuClick()
}