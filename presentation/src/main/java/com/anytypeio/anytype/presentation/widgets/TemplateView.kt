package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.templates.TemplateObjectTypeView
import com.anytypeio.anytype.presentation.templates.TemplateView

sealed class TypeTemplatesWidgetUI {
    object Init : TypeTemplatesWidgetUI()

    sealed class Data : TypeTemplatesWidgetUI() {
        abstract val showWidget: Boolean
        abstract val isEditing: Boolean
        abstract val isMoreMenuVisible: Boolean

        data class DefaultObject(
            override val showWidget: Boolean,
            override val isEditing: Boolean,
            override val isMoreMenuVisible: Boolean
        ) : Data()

        data class DefaultTemplate(
            override val showWidget: Boolean,
            override val isEditing: Boolean,
            override val isMoreMenuVisible: Boolean
        ) : Data()

        data class CreateObject(
            override val showWidget: Boolean,
            override val isEditing: Boolean,
            override val isMoreMenuVisible: Boolean
        ) : Data()
    }
}

//data class TypeTemplatesWidgetUI(
//    val items: List<TemplateView>,
//    val showWidget: Boolean,
//    val isEditing: Boolean,
//    val isMoreMenuVisible: Boolean,
//    val moreMenuTemplate: TemplateView.Template?,
//    val defaultObjectType: ObjectWrapper.Type? = null,
//    val objectTypes: List<TemplateObjectTypeView> = listOf(TemplateObjectTypeView.Search)
//) {
//    fun dismiss() = copy(
//        showWidget = false,
//        isEditing = false,
//        isMoreMenuVisible = false,
//        moreMenuTemplate = null
//    )
//
//    companion object {
//        fun init() = TypeTemplatesWidgetUI(
//            items = emptyList(),
//            showWidget = false,
//            isEditing = false,
//            isMoreMenuVisible = false,
//            moreMenuTemplate = null,
//            objectTypes = emptyList(),
//            defaultObjectType = null
//        )
//    }
//}
