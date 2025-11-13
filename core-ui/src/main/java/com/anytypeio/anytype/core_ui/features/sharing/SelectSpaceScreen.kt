package com.anytypeio.anytype.core_ui.features.sharing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.DefaultSearchBar
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.SpaceIconView as SpaceIcon

/**
 * Data model for selectable space items in the grid
 */
data class SelectableSpaceItem(
    val id: String,
    val icon: SpaceIcon,
    val name: String,
    val isSelected: Boolean = false
)

/**
 * Full screen for space selection with search and grid layout
 *
 * @param spaces List of selectable space items
 * @param searchQuery Current search query text
 * @param onSearchQueryChanged Callback when search query changes
 * @param onSpaceSelected Callback when a space is selected
 * @param modifier Modifier for the screen
 */
@Composable
fun SelectSpaceScreen(
    spaces: List<SelectableSpaceItem>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSpaceSelected: (SelectableSpaceItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background_primary))
            .padding(top = 20.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.select_space),
            style = BodyBold,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Search Bar
        DefaultSearchBar(
            value = searchQuery,
            onQueryChanged = onSearchQueryChanged,
            hint = R.string.search,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Space Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = spaces,
                key = { it.id }
            ) { space ->
                SpaceGridItem(
                    icon = space.icon,
                    name = space.name,
                    isSelected = space.isSelected,
                    onClick = { onSpaceSelected(space) }
                )
            }
        }
    }
}

/**
 * Individual space item in the grid
 *
 * @param icon Space icon to display
 * @param name Space name
 * @param isSelected Whether this space is currently selected
 * @param onClick Callback when item is clicked
 * @param modifier Modifier for the item
 */
@Composable
private fun SpaceGridItem(
    icon: SpaceIcon,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .noRippleClickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = colorResource(id = R.color.glyph_active),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Space Icon
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            SpaceIconView(
                icon = icon,
                mainSize = 64.dp,
                onSpaceIconClick = onClick
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Space Name
        Text(
            text = name,
            style = Caption1Medium,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )
    }
}

// ============================================
// PREVIEW
// ============================================

@DefaultPreviews
@Composable
private fun SelectSpaceScreenPreview() {
    val sampleSpaces = listOf(
        SelectableSpaceItem(
            id = "1",
            icon = SpaceIcon.DataSpace.Placeholder(
                name = "B&O Museum",
                color = SystemColor.PINK
            ),
            name = "B&O Museum",
            isSelected = false
        ),
        SelectableSpaceItem(
            id = "2",
            icon = SpaceIcon.DataSpace.Placeholder(
                name = "Imaginary Space",
                color = SystemColor.YELLOW
            ),
            name = "Imaginary Space",
            isSelected = true
        ),
        SelectableSpaceItem(
            id = "3",
            icon = SpaceIcon.DataSpace.Placeholder(
                name = "Berlin Reading Club for Expats",
                color = SystemColor.BLUE
            ),
            name = "Berlin Reading Club for Expats",
            isSelected = false
        ),
        SelectableSpaceItem(
            id = "4",
            icon = SpaceIcon.DataSpace.Placeholder(
                name = "Box for Cards",
                color = SystemColor.RED
            ),
            name = "Box for Cards",
            isSelected = false
        ),
        SelectableSpaceItem(
            id = "5",
            icon = SpaceIcon.DataSpace.Placeholder(
                name = "Anytype Design",
                color = SystemColor.TEAL
            ),
            name = "Anytype Design",
            isSelected = false
        ),
        SelectableSpaceItem(
            id = "6",
            icon = SpaceIcon.DataSpace.Placeholder(
                name = "Space Name",
                color = SystemColor.RED
            ),
            name = "Space Name",
            isSelected = false
        ),
        SelectableSpaceItem(
            id = "7",
            icon = SpaceIcon.DataSpace.Placeholder(
                name = "Go Team",
                color = SystemColor.AMBER
            ),
            name = "Go Team",
            isSelected = false
        ),
        SelectableSpaceItem(
            id = "8",
            icon = SpaceIcon.DataSpace.Placeholder(
                name = "The New Yorker",
                color = SystemColor.PURPLE
            ),
            name = "The New Yorker",
            isSelected = false
        ),
        SelectableSpaceItem(
            id = "9",
            icon = SpaceIcon.DataSpace.Placeholder(
                name = "Diary",
                color = SystemColor.SKY
            ),
            name = "Diary",
            isSelected = false
        )
    )

    SelectSpaceScreen(
        spaces = sampleSpaces,
        searchQuery = "",
        onSearchQueryChanged = {},
        onSpaceSelected = {}
    )
}
