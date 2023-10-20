package com.anytypeio.anytype.presentation.templates

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.presentation.editor.cover.CoverColor

sealed class TemplateView {

    abstract val isDefault: Boolean
    abstract val targetTypeId: TypeId
    abstract val targetTypeKey: TypeKey

    data class Blank(
        val id: Id,
        override val targetTypeId: TypeId,
        override val targetTypeKey: TypeKey,
        val typeName: String = "",
        val layout: Int,
        override val isDefault: Boolean = false
    ) : TemplateView()

    data class New(
        override val targetTypeId: TypeId,
        override val targetTypeKey: TypeKey,
    ) : TemplateView() {
        override val isDefault: Boolean = false
    }

    data class Template(
        val id: Id,
        val name: String,
        override val targetTypeId: TypeId,
        override val targetTypeKey: TypeKey,
        val layout: ObjectType.Layout = ObjectType.Layout.BASIC,
        val emoji: String? = null,
        val image: String? = null,
        val coverColor: CoverColor? = null,
        val coverImage: Url? = null,
        val coverGradient: String? = null,
        override val isDefault: Boolean = false
    ) : TemplateView() {

        fun isCoverPresent(): Boolean {
            return coverColor != null || coverImage != null || coverGradient != null
        }

        fun isImageOrEmojiPresent(): Boolean {
            return image != null || emoji != null
        }
    }

    companion object {
        const val DEFAULT_TEMPLATE_ID_BLANK = "blank"
    }
}

sealed class TemplateMenuClick {
    data class Default(val template: Id) : TemplateMenuClick()
    data class Edit(val template: Id) : TemplateMenuClick()
    data class Duplicate(val template: Id) : TemplateMenuClick()
    data class Delete(val template: Id) : TemplateMenuClick()
}

sealed class TemplateObjectTypeView {

    data class Item(
        val type: ObjectWrapper.Type,
        val isDefault: Boolean = false
    ) : TemplateObjectTypeView()

    object Search : TemplateObjectTypeView()
}