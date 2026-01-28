package com.anytypeio.anytype.feature_create_object.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectAction
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectState
import com.anytypeio.anytype.feature_create_object.presentation.ObjectTypeItem

/**
 * The main content composable for the create object screen.
 * This component is shared between popup and bottom sheet variants.
 *
 * Layout structure:
 * - Media section (Photos, Camera, Files) - if showMediaSection
 * - Attach existing object button - if showAttachExisting
 * - Scrollable list of object types (with search filtering)
 * - Search bar at the bottom
 *
 * @param state The current UI state containing object types and search query
 * @param onAction Callback for handling user actions
 * @param modifier Optional modifier for the root container
 */
@Composable
fun CreateObjectContent(
    state: CreateObjectState,
    onAction: (CreateObjectAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Media section
        if (state.showMediaSection) {
            MediaSection(onAction = onAction)
        }

        // Attach existing object
        if (state.showAttachExisting) {
            AttachExistingObjectButton(
                onClick = { onAction(CreateObjectAction.AttachExistingObject) }
            )
        }

        // Object types list with search
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                state.isLoading -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(24.dp)
                                .size(24.dp),
                            color = colorResource(id = R.color.text_secondary)
                        )
                    }
                }

                state.filteredObjectTypes.isEmpty() -> {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.searchQuery.isBlank()) {
                                "No object types available"
                            } else {
                                "No types found"
                            },
                            style = BodyRegular,
                            color = colorResource(id = R.color.text_secondary)
                        )
                    }
                }

                else -> {
                    // Object types list
                    ObjectTypesList(
                        types = state.filteredObjectTypes,
                        onTypeClick = { type ->
                            onAction(
                                CreateObjectAction.CreateObjectOfType(
                                    typeKey = type.typeKey,
                                    typeName = type.name
                                )
                            )
                        }
                    )
                }
            }
        }

        // Search bar at bottom
        SearchBar(
            query = state.searchQuery,
            onQueryChange = { query ->
                onAction(CreateObjectAction.UpdateSearch(query))
            }
        )
    }
}

/**
 * Media section with Photos, Camera, and Files options.
 */
@Composable
private fun MediaSection(
    onAction: (CreateObjectAction) -> Unit
) {
    Column {
        MenuItem(
            icon = R.drawable.ic_attachment_menu_photos,
            text = stringResource(R.string.attachment_menu_photos),
            onClick = { onAction(CreateObjectAction.SelectPhotos) }
        )

        MenuItem(
            icon = R.drawable.ic_attachment_menu_camera,
            text = stringResource(R.string.attachment_menu_camera),
            onClick = { onAction(CreateObjectAction.TakePhoto) }
        )

        MenuItem(
            icon = R.drawable.ic_attachment_menu_files,
            text = stringResource(R.string.attachment_menu_files),
            onClick = { onAction(CreateObjectAction.SelectFiles) }
        )
    }
}

/**
 * Button for attaching an existing object.
 */
@Composable
private fun AttachExistingObjectButton(
    onClick: () -> Unit
) {
    MenuItem(
        icon = R.drawable.ic_attachment_menu_link,
        text = stringResource(R.string.attachment_menu_attach_object),
        onClick = onClick
    )
}

/**
 * Scrollable list of object types.
 */
@Composable
private fun ObjectTypesList(
    types: List<ObjectTypeItem>,
    onTypeClick: (ObjectTypeItem) -> Unit
) {
    LazyColumn {
        items(
            items = types,
            key = { it.typeKey }
        ) { type ->
            ObjectTypeMenuItem(
                type = type,
                onClick = { onTypeClick(type) }
            )
        }
    }
}

/**
 * Search bar for filtering object types.
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(48.dp)
            .background(
                color = colorResource(id = R.color.shape_transparent_tertiary),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.shape_primary),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 16.dp)
    ) {
        // Search icon
        Image(
            painter = painterResource(id = R.drawable.ic_search_18),
            contentDescription = stringResource(R.string.search),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Search input
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            textStyle = BodyRegular.copy(
                color = colorResource(id = R.color.text_primary)
            ),
            cursorBrush = SolidColor(colorResource(id = R.color.control_accent)),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_secondary)
                    )
                }
                innerTextField()
            }
        )
    }
}

/**
 * Single menu item with a drawable icon (for media actions).
 */
@Composable
private fun MenuItem(
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
 * Single menu item for an object type (with ObjectIcon).
 */
@Composable
private fun ObjectTypeMenuItem(
    type: ObjectTypeItem,
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
            icon = type.icon,
            modifier = Modifier,
            iconSize = 24.dp,
            iconWithoutBackgroundMaxSize = 24.dp,
            backgroundColor = R.color.transparent_black
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = type.name,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary)
        )
    }
}
