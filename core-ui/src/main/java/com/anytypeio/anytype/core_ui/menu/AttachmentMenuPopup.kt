package com.anytypeio.anytype.core_ui.menu

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ui.CustomIconColor
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon

/**
 * Sealed class representing menu items in the attachment popup.
 */
sealed class AttachmentMenuAction {
    data object Photos : AttachmentMenuAction()
    data object Camera : AttachmentMenuAction()
    data object Files : AttachmentMenuAction()
    data object AttachObject : AttachmentMenuAction()
    data class CreateObjectOfType(val typeKey: Key, val typeName: String) : AttachmentMenuAction()
    data object SeeAll : AttachmentMenuAction()
}

/**
 * Data class representing an object type option in the menu.
 */
data class ObjectTypeMenuItem(
    val typeKey: Key,
    val name: String,
    val icon: ObjectIcon
)

/**
 * Attachment menu popup component that displays options for:
 * - Media attachment (Photos, Camera, Files)
 * - Attach existing object
 * - Quick-create for common object types
 * - "See all" to expand full type list
 *
 * This component follows the new design from Figma (DROID-4201) which replaces
 * the old dropdown menu with a more comprehensive floating popup.
 */
@Composable
fun AttachmentMenuPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onAction: (AttachmentMenuAction) -> Unit,
    quickCreateTypes: List<ObjectTypeMenuItem> = emptyList(),
    modifier: Modifier = Modifier,
    offset: DpOffset = StyledDropdownMenuDefaults.DefaultOffset
) {
    StyledDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset
    ) {
        AttachmentMenuContent(
            onAction = { action ->
                onDismissRequest()
                onAction(action)
            },
            quickCreateTypes = quickCreateTypes
        )
    }
}

/**
 * Content of the attachment menu, can be used standalone or within a popup.
 */
@Composable
fun AttachmentMenuContent(
    onAction: (AttachmentMenuAction) -> Unit,
    quickCreateTypes: List<ObjectTypeMenuItem> = emptyList()
) {
    Column {
        // Media section
        AttachmentMenuItem(
            icon = R.drawable.ic_attachment_menu_photos,
            text = stringResource(R.string.attachment_menu_photos),
            onClick = { onAction(AttachmentMenuAction.Photos) }
        )

        AttachmentMenuItem(
            icon = R.drawable.ic_attachment_menu_camera,
            text = stringResource(R.string.attachment_menu_camera),
            onClick = { onAction(AttachmentMenuAction.Camera) }
        )

        AttachmentMenuItem(
            icon = R.drawable.ic_attachment_menu_files,
            text = stringResource(R.string.attachment_menu_files),
            onClick = { onAction(AttachmentMenuAction.Files) }
        )

        // Attach existing object
        AttachmentMenuItem(
            icon = R.drawable.ic_attachment_menu_link,
            text = stringResource(R.string.attachment_menu_attach_object),
            onClick = { onAction(AttachmentMenuAction.AttachObject) }
        )

        // Quick create types
        quickCreateTypes.forEach { typeItem ->
            AttachmentMenuItemWithObjectIcon(
                icon = typeItem.icon,
                text = typeItem.name,
                onClick = {
                    onAction(
                        AttachmentMenuAction.CreateObjectOfType(
                            typeKey = typeItem.typeKey,
                            typeName = typeItem.name
                        )
                    )
                }
            )
        }

        // See all option (only show if there are quick create types)
        if (quickCreateTypes.isNotEmpty()) {
            AttachmentMenuItem(
                icon = R.drawable.ic_attachment_menu_see_all,
                text = stringResource(R.string.attachment_menu_see_all),
                onClick = { onAction(AttachmentMenuAction.SeeAll) }
            )
        }
    }
}

/**
 * Single menu item with a drawable icon.
 */
@Composable
private fun AttachmentMenuItem(
    @DrawableRes icon: Int,
    text: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp)
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = text,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = text,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

/**
 * Single menu item with an ObjectIcon (for object type items).
 */
@Composable
private fun AttachmentMenuItemWithObjectIcon(
    icon: ObjectIcon,
    text: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp)
    ) {
        ListWidgetObjectIcon(
            icon = icon,
            modifier = Modifier,
            iconSize = 24.dp,
            iconWithoutBackgroundMaxSize = 24.dp,
            backgroundColor = R.color.transparent_black
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = text,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@DefaultPreviews
@Composable
private fun AttachmentMenuContentPreview() {
    Box(
        modifier = Modifier
            .background(colorResource(id = R.color.background_secondary))
            .padding(8.dp)
    ) {
        AttachmentMenuContent(
            onAction = {},
            quickCreateTypes = listOf(
                ObjectTypeMenuItem(
                    typeKey = "ot-page",
                    name = "Page",
                    icon = ObjectIcon.TypeIcon.Default(
                        rawValue = "document",
                        color = CustomIconColor.DEFAULT
                    )
                ),
                ObjectTypeMenuItem(
                    typeKey = "ot-task",
                    name = "Task",
                    icon = ObjectIcon.TypeIcon.Default(
                        rawValue = "checkbox",
                        color = CustomIconColor.DEFAULT
                    )
                ),
                ObjectTypeMenuItem(
                    typeKey = "ot-collection",
                    name = "Collection",
                    icon = ObjectIcon.TypeIcon.Default(
                        rawValue = "layers",
                        color = CustomIconColor.DEFAULT
                    )
                )
            )
        )
    }
}
