package com.anytypeio.anytype.core_ui.widgets

import androidx.compose.runtime.Composable
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.templates.TemplateObjectTypeView
import com.anytypeio.anytype.presentation.templates.TemplateView
import com.anytypeio.anytype.presentation.templates.TemplateView.Companion.DEFAULT_TEMPLATE_ID_BLANK
import com.anytypeio.anytype.presentation.widgets.TypeTemplatesWidgetUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@DefaultPreviews
@Composable
fun TypeTemplatesWidgetPreview() {
    val items = listOf(
        TemplateView.Blank(
            id = DEFAULT_TEMPLATE_ID_BLANK,
            targetTypeId = TypeId("page"),
            targetTypeKey = TypeKey("ot-page"),
            typeName = "Page",
            layout = ObjectType.Layout.BASIC.code
        ),
        TemplateView.Template(
            id = "1",
            name = "Template Title",
            targetTypeId = TypeId("page"),
            targetTypeKey = TypeKey("ot-page"),
            layout = ObjectType.Layout.PROFILE,
            image = null,
            emoji = "📄",
            coverColor = CoverColor.RED,
            coverGradient = null,
            coverImage = null,
            isDefault = true
        ),
    )
    val state = TypeTemplatesWidgetUI.Data(
        templates = items,
        showWidget = true,
        isEditing = true,
        moreMenuItem = null,
        objectTypes = listOf(
            TemplateObjectTypeView.Search,
            TemplateObjectTypeView.Item(
                type = ObjectWrapper.Type(
                    map = mapOf(Relations.ID to "123", Relations.NAME to "Page"),
                ),
                icon = ObjectIcon.TypeIcon.Default.DEFAULT
            )
        ),
        viewerId = "",
        isPossibleToChangeType = true,
        isPossibleToChangeTemplate = false
    )
    TypeTemplatesWidget(
        state = state,
        onDismiss = {},
        editClick = {},
        doneClick = {},
        moreClick = {},
        scope = CoroutineScope(
            Dispatchers.Main
        ),
        menuClick = {},
        action = {}
    )
}