package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.templates.TemplateObjectTypeView
import com.anytypeio.anytype.presentation.templates.TemplateView

sealed class TypeTemplatesWidgetUI {
    abstract val showWidget: Boolean
    abstract val isEditing: Boolean

    data class Init(
        override val showWidget: Boolean = false,
        override val isEditing: Boolean = false
    ) : TypeTemplatesWidgetUI()

    sealed class Data : TypeTemplatesWidgetUI() {

        abstract val moreMenuItemId: TemplateView?
        abstract val templates: List<TemplateView>
        abstract val defaultObjectType: ObjectWrapper.Type?
        abstract val defaultTemplateId: Id?
        abstract val viewerId: Id

        data class DefaultTemplate(
            override val showWidget: Boolean,
            override val isEditing: Boolean,
            override val moreMenuItemId: TemplateView?,
            override val templates: List<TemplateView>,
            override val defaultObjectType: ObjectWrapper.Type?,
            override val defaultTemplateId: Id?,
            override val viewerId: Id
        ) : Data()

        sealed class Types : Data() {

            abstract val objectTypes: List<TemplateObjectTypeView>

            data class DefaultObject(
                override val showWidget: Boolean,
                override val isEditing: Boolean,
                override val moreMenuItemId: TemplateView?,
                override val templates: List<TemplateView>,
                override val objectTypes: List<TemplateObjectTypeView>,
                override val defaultObjectType: ObjectWrapper.Type?,
                override val defaultTemplateId: Id?,
                override val viewerId: Id,
            ) : Types()

            data class CreateObject(
                override val showWidget: Boolean,
                override val isEditing: Boolean,
                override val moreMenuItemId: TemplateView?,
                override val templates: List<TemplateView>,
                override val objectTypes: List<TemplateObjectTypeView>,
                override val defaultObjectType: ObjectWrapper.Type?,
                override val defaultTemplateId: Id?,
                override val viewerId: Id,
            ) : Types()
        }
    }

    fun getWidgetViewerId(): Id? = if (this is Data) viewerId else null
}

sealed class TypeTemplatesWidgetUIAction {

    sealed class TypeClick : TypeTemplatesWidgetUIAction() {
        data class Item(val type: ObjectWrapper.Type) : TypeClick()
        object Search : TypeClick()
    }

    data class TemplateClick(val template: TemplateView): TypeTemplatesWidgetUIAction()
}

fun TypeTemplatesWidgetUI.Data.enterEditing(): TypeTemplatesWidgetUI.Data {
    return when (this) {
        is TypeTemplatesWidgetUI.Data.DefaultTemplate -> copy(isEditing = true)
        is TypeTemplatesWidgetUI.Data.Types.DefaultObject -> copy(isEditing = true)
        is TypeTemplatesWidgetUI.Data.Types.CreateObject -> copy(isEditing = true)
    }
}

fun TypeTemplatesWidgetUI.Data.exitEditing(): TypeTemplatesWidgetUI.Data {
    return when (this) {
        is TypeTemplatesWidgetUI.Data.DefaultTemplate -> copy(
            isEditing = false,
            moreMenuItemId = null
        )

        is TypeTemplatesWidgetUI.Data.Types.DefaultObject -> copy(
            isEditing = false,
            moreMenuItemId = null
        )

        is TypeTemplatesWidgetUI.Data.Types.CreateObject -> copy(
            isEditing = false,
            moreMenuItemId = null
        )
    }
}

fun TypeTemplatesWidgetUI.Data.showMoreMenu(item: TemplateView): TypeTemplatesWidgetUI.Data {
    return when (this) {
        is TypeTemplatesWidgetUI.Data.DefaultTemplate -> copy(moreMenuItemId = item)
        is TypeTemplatesWidgetUI.Data.Types.DefaultObject -> copy(moreMenuItemId = item)
        is TypeTemplatesWidgetUI.Data.Types.CreateObject -> copy(moreMenuItemId = item)
    }
}

fun TypeTemplatesWidgetUI.Data.hideMoreMenu(): TypeTemplatesWidgetUI.Data {
    return when (this) {
        is TypeTemplatesWidgetUI.Data.DefaultTemplate -> copy(moreMenuItemId = null)
        is TypeTemplatesWidgetUI.Data.Types.DefaultObject -> copy(moreMenuItemId = null)
        is TypeTemplatesWidgetUI.Data.Types.CreateObject -> copy(moreMenuItemId = null)
    }
}
