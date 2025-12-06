package com.anytypeio.anytype.core_ui.features.sharing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
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
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
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
 * Full screen for space selection with search, grid layout, and comment section
 *
 * @param spaces List of selectable space items
 * @param searchQuery Current search query text
 * @param commentText Current comment text
 * @param onSearchQueryChanged Callback when search query changes
 * @param onCommentChanged Callback when comment text changes
 * @param onSpaceSelected Callback when a space is selected
 * @param onSendClicked Callback when Send button is clicked
 * @param modifier Modifier for the screen
 */
@Composable
fun SelectSpaceScreen(
    spaces: List<SelectableSpaceItem>,
    searchQuery: String,
    commentText: String,
    onSearchQueryChanged: (String) -> Unit,
    onCommentChanged: (String) -> Unit,
    onSpaceSelected: (SelectableSpaceItem) -> Unit,
    onSendClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasSelectedSpace = spaces.any { it.isSelected }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background_primary))
    ) {
        // Main content (Header + Search + Grid or Empty State)
        Column(
            modifier = Modifier
                .weight(1f)
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

            // Show empty state if no spaces, otherwise show search and grid
            if (spaces.isEmpty()) {
                EmptySpaceState(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
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

        // Bottom section (Comment + Send button) - only shown when space is selected
        if (hasSelectedSpace) {
            CommentSection(
                commentText = commentText,
                onCommentChanged = onCommentChanged,
                onSendClicked = onSendClicked,
                modifier = Modifier.fillMaxWidth()
            )
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
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Space Icon with checkmark overlay
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            SpaceIconView(
                icon = icon,
                mainSize = 64.dp,
                onSpaceIconClick = onClick
            )

            // Blue checkmark overlay for selected state
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-2).dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(color = colorResource(id = R.color.glyph_active)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_tick_24),
                        contentDescription = "Selected",
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
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

/**
 * Comment section with text field and Send button
 *
 * @param commentText Current comment text
 * @param onCommentChanged Callback when comment changes
 * @param onSendClicked Callback when Send button is clicked
 * @param modifier Modifier for the section
 */
@Composable
private fun CommentSection(
    commentText: String,
    onCommentChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(color = colorResource(id = R.color.background_primary))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Comment Text Field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(id = R.color.shape_primary),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = commentText,
                onValueChange = onCommentChanged,
                textStyle = BodyRegular.copy(
                    color = colorResource(id = R.color.text_primary)
                ),
                cursorBrush = SolidColor(colorResource(id = R.color.glyph_active)),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (commentText.isEmpty()) {
                        Text(
                            text = stringResource(R.string.add_a_comment),
                            style = BodyRegular,
                            color = colorResource(id = R.color.text_secondary)
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Send Button
        ButtonPrimary(
            text = stringResource(R.string.send),
            onClick = onSendClicked,
            size = ButtonSize.Large,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Empty state when user has no spaces
 *
 * @param modifier Modifier for the empty state
 */
@Composable
private fun EmptySpaceState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Coffee icon
        Image(
            painter = painterResource(id = R.drawable.ic_popup_coffee_56),
            contentDescription = null,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Empty state message
        Text(
            text = stringResource(R.string.you_dont_have_any_spaces_yet),
            style = BodyRegular,
            color = colorResource(id = R.color.text_secondary),
            textAlign = TextAlign.Center
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
            isSelected = true
        ),
        SelectableSpaceItem(
            id = "2",
            icon = SpaceIcon.DataSpace.Placeholder(
                name = "Imaginary Space",
                color = SystemColor.YELLOW
            ),
            name = "Imaginary Space",
            isSelected = false
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
        commentText = "",
        onSearchQueryChanged = {},
        onCommentChanged = {},
        onSpaceSelected = {},
        onSendClicked = {}
    )
}

@DefaultPreviews
@Composable
private fun SelectSpaceScreenEmptyPreview() {
    SelectSpaceScreen(
        spaces = emptyList(),
        searchQuery = "",
        commentText = "",
        onSearchQueryChanged = {},
        onCommentChanged = {},
        onSpaceSelected = {},
        onSendClicked = {}
    )
}