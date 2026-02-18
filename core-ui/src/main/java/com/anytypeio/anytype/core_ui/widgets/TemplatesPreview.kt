package com.anytypeio.anytype.core_ui.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

// region TypeTemplatesWidget Previews

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
        isEditing = false,
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
        isPossibleToChangeTemplate = true
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

@DefaultPreviews
@Composable
fun TypeTemplatesWidgetViewModePreview() {
    val items = listOf(
        TemplateView.Blank(
            id = DEFAULT_TEMPLATE_ID_BLANK,
            targetTypeId = TypeId("page"),
            targetTypeKey = TypeKey("ot-page"),
            typeName = "Page",
            layout = ObjectType.Layout.BASIC.code,
            isDefault = true
        ),
        TemplateView.Template(
            id = "1",
            name = "Meeting Notes",
            targetTypeId = TypeId("page"),
            targetTypeKey = TypeKey("ot-page"),
            layout = ObjectType.Layout.BASIC,
            emoji = "📝"
        ),
        TemplateView.Template(
            id = "2",
            name = "Project Plan",
            targetTypeId = TypeId("page"),
            targetTypeKey = TypeKey("ot-page"),
            layout = ObjectType.Layout.BASIC,
            emoji = "📋",
            coverColor = CoverColor.BLUE
        ),
        TemplateView.New(
            targetTypeId = TypeId("page"),
            targetTypeKey = TypeKey("ot-page")
        )
    )
    val state = TypeTemplatesWidgetUI.Data(
        templates = items,
        showWidget = true,
        isEditing = false,
        moreMenuItem = null,
        objectTypes = emptyList(),
        viewerId = "",
        isPossibleToChangeType = false,
        isPossibleToChangeTemplate = true
    )
    TypeTemplatesWidget(
        state = state,
        onDismiss = {},
        editClick = {},
        doneClick = {},
        moreClick = {},
        scope = CoroutineScope(Dispatchers.Main),
        menuClick = {},
        action = {}
    )
}

@DefaultPreviews
@Composable
fun TypeTemplatesWidgetWithMultipleTypesPreview() {
    val items = listOf(
        TemplateView.Blank(
            id = DEFAULT_TEMPLATE_ID_BLANK,
            targetTypeId = TypeId("task"),
            targetTypeKey = TypeKey("ot-task"),
            typeName = "Task",
            layout = ObjectType.Layout.TODO.code
        ),
        TemplateView.Template(
            id = "1",
            name = "Daily Task",
            targetTypeId = TypeId("task"),
            targetTypeKey = TypeKey("ot-task"),
            layout = ObjectType.Layout.TODO,
            isDefault = true
        )
    )
    val objectTypes = listOf(
        TemplateObjectTypeView.Item(
            type = ObjectWrapper.Type(
                map = mapOf(Relations.ID to "page-id", Relations.NAME to "Page")
            ),
            icon = ObjectIcon.TypeIcon.Default.DEFAULT,
            isSelected = false
        ),
        TemplateObjectTypeView.Item(
            type = ObjectWrapper.Type(
                map = mapOf(Relations.ID to "task-id", Relations.NAME to "Task")
            ),
            icon = ObjectIcon.TypeIcon.Default.DEFAULT,
            isSelected = true
        ),
        TemplateObjectTypeView.Item(
            type = ObjectWrapper.Type(
                map = mapOf(Relations.ID to "note-id", Relations.NAME to "Note")
            ),
            icon = ObjectIcon.TypeIcon.Default.DEFAULT,
            isSelected = false
        )
    )
    val state = TypeTemplatesWidgetUI.Data(
        templates = items,
        showWidget = true,
        isEditing = false,
        moreMenuItem = null,
        objectTypes = objectTypes,
        viewerId = "",
        isPossibleToChangeType = true,
        isPossibleToChangeTemplate = true
    )
    TypeTemplatesWidget(
        state = state,
        onDismiss = {},
        editClick = {},
        doneClick = {},
        moreClick = {},
        scope = CoroutineScope(Dispatchers.Main),
        menuClick = {},
        action = {}
    )
}

// endregion

// region TemplateItemContent Previews

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TemplateItemBlankPreview() {
    Box(
        modifier = Modifier
            .height(224.dp)
            .width(120.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        TemplateItemContent(
            item = TemplateView.Blank(
                id = DEFAULT_TEMPLATE_ID_BLANK,
                targetTypeId = TypeId("page"),
                targetTypeKey = TypeKey("ot-page"),
                typeName = "Page",
                layout = ObjectType.Layout.BASIC.code
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TemplateItemBasicPreview() {
    Box(
        modifier = Modifier
            .height(224.dp)
            .width(120.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        TemplateItemContent(
            item = TemplateView.Template(
                id = "1",
                name = "Meeting Notes",
                targetTypeId = TypeId("page"),
                targetTypeKey = TypeKey("ot-page"),
                layout = ObjectType.Layout.BASIC,
                emoji = "📝"
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TemplateItemWithCoverPreview() {
    Box(
        modifier = Modifier
            .height(224.dp)
            .width(120.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        TemplateItemContent(
            item = TemplateView.Template(
                id = "1",
                name = "Project Plan",
                targetTypeId = TypeId("page"),
                targetTypeKey = TypeKey("ot-page"),
                layout = ObjectType.Layout.BASIC,
                emoji = "📋",
                coverColor = CoverColor.TEAL
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TemplateItemProfilePreview() {
    Box(
        modifier = Modifier
            .height(224.dp)
            .width(120.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        TemplateItemContent(
            item = TemplateView.Template(
                id = "1",
                name = "Contact",
                targetTypeId = TypeId("profile"),
                targetTypeKey = TypeKey("ot-profile"),
                layout = ObjectType.Layout.PROFILE
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TemplateItemTodoPreview() {
    Box(
        modifier = Modifier
            .height(224.dp)
            .width(120.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        TemplateItemContent(
            item = TemplateView.Template(
                id = "1",
                name = "Daily Task",
                targetTypeId = TypeId("task"),
                targetTypeKey = TypeKey("ot-task"),
                layout = ObjectType.Layout.TODO
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TemplateItemNewPreview() {
    Box(
        modifier = Modifier
            .height(224.dp)
            .width(120.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        TemplateItemContent(
            item = TemplateView.New(
                targetTypeId = TypeId("page"),
                targetTypeKey = TypeKey("ot-page")
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TemplateItemDefaultPreview() {
    Box(
        modifier = Modifier
            .height(224.dp)
            .width(120.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        TemplateItemContent(
            item = TemplateView.Blank(
                id = DEFAULT_TEMPLATE_ID_BLANK,
                targetTypeId = TypeId("page"),
                targetTypeKey = TypeKey("ot-page"),
                typeName = "Page",
                layout = ObjectType.Layout.BASIC.code,
                isDefault = true
            ),
            showDefaultIcon = true
        )
    }
}

// endregion