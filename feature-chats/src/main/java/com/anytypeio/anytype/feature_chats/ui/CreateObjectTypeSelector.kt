package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.DefaultSearchBar
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.menu.ObjectTypeMenuItem
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor

/**
 * Full-screen type selector for creating new objects.
 *
 * This component shows:
 * - A list of available object types (Page, Task, Collection, Set, Note, Project, Goal, Folder)
 * - A search bar at the bottom for filtering types
 * - An empty state when no results match the search query
 *
 * Used when user taps "See all" from the attachment menu popup.
 */
@Composable
fun CreateObjectTypeSelector(
    objectTypes: List<ObjectTypeMenuItem>,
    onTypeSelected: (Key, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredTypes = remember(objectTypes, searchQuery) {
        if (searchQuery.isBlank()) {
            objectTypes
        } else {
            objectTypes.filter { type ->
                type.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = colorResource(id = R.color.background_primary),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Dragger handle
            Dragger(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Type list
            if (filteredTypes.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(
                        items = filteredTypes,
                        key = { it.typeKey }
                    ) { typeItem ->
                        ObjectTypeRow(
                            typeItem = typeItem,
                            onClick = {
                                onTypeSelected(typeItem.typeKey, typeItem.name)
                            }
                        )
                    }
                }
            } else {
                // Empty state
                EmptySearchState(
                    query = searchQuery,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }

            // Search bar at the bottom
            SearchBarSection(
                query = searchQuery,
                onQueryChanged = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun ObjectTypeRow(
    typeItem: ObjectTypeMenuItem,
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
            icon = typeItem.icon,
            modifier = Modifier,
            iconSize = 24.dp,
            iconWithoutBackgroundMaxSize = 24.dp,
            backgroundColor = R.color.transparent_black
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = typeItem.name,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Composable
private fun EmptySearchState(
    query: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.attachment_menu_no_results, query),
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.attachment_menu_no_results_hint),
                style = Relations2,
                color = colorResource(id = R.color.text_secondary),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SearchBarSection(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = colorResource(id = R.color.shape_transparent_secondary),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        DefaultSearchBar(
            value = query,
            onQueryChanged = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            hint = R.string.attachment_menu_search_placeholder
        )
    }
}

@DefaultPreviews
@Composable
private fun CreateObjectTypeSelectorPreview() {
    val sampleTypes = listOf(
        ObjectTypeMenuItem(
            typeKey = "ot-page",
            name = "Page",
            icon = ObjectIcon.TypeIcon.Default("document", CustomIconColor.DEFAULT)
        ),
        ObjectTypeMenuItem(
            typeKey = "ot-task",
            name = "Task",
            icon = ObjectIcon.TypeIcon.Default("checkbox", CustomIconColor.DEFAULT)
        ),
        ObjectTypeMenuItem(
            typeKey = "ot-collection",
            name = "Collection",
            icon = ObjectIcon.TypeIcon.Default("layers", CustomIconColor.DEFAULT)
        ),
        ObjectTypeMenuItem(
            typeKey = "ot-set",
            name = "Set",
            icon = ObjectIcon.TypeIcon.Default("list", CustomIconColor.DEFAULT)
        ),
        ObjectTypeMenuItem(
            typeKey = "ot-note",
            name = "Note",
            icon = ObjectIcon.TypeIcon.Default("document-text", CustomIconColor.DEFAULT)
        ),
        ObjectTypeMenuItem(
            typeKey = "ot-project",
            name = "Project",
            icon = ObjectIcon.TypeIcon.Default("briefcase", CustomIconColor.DEFAULT)
        )
    )

    CreateObjectTypeSelector(
        objectTypes = sampleTypes,
        onTypeSelected = { _, _ -> },
        onDismiss = {}
    )
}

@DefaultPreviews
@Composable
private fun EmptySearchStatePreview() {
    EmptySearchState(
        query = "AAA",
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(colorResource(id = R.color.background_primary))
    )
}
