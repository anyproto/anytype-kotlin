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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.DefaultSearchBar
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.presentation.confgs.ChatConfig.MAX_MESSAGE_CHARACTER_LIMIT
import com.anytypeio.anytype.core_models.ui.SpaceIconView
as SpaceIcon

/**
 * State returned by CommentTextField to inform callers about character limit status
 */
data class CommentFieldState(
    val isOverLimit: Boolean
)

/**
 * Data model for selectable space items in the grid
 */
data class SelectableSpaceItem(
    val id: String,
    val icon: SpaceIcon,
    val name: String,
    val isSelected: Boolean = false,
    val isChatSpace: Boolean = false
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
    // Check if any chat space is selected
    val hasSelectedChatSpace = spaces.any { it.isSelected && it.isChatSpace }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Search bar - always visible
        DefaultSearchBar(
            value = searchQuery,
            onQueryChanged = onSearchQueryChanged,
            hint = R.string.search,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        // Space Grid - shows ALL spaces, scrollable
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .nestedScroll(rememberNestedScrollInteropConnection())
                .weight(1f)
                .fillMaxWidth()
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

        // Comment section - appears at bottom when chat space is selected
        if (hasSelectedChatSpace) {
            CommentSection(
                commentText = commentText,
                onCommentChanged = onCommentChanged,
                onSendClicked = onSendClicked,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SelectSpaceScreenHeader(modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.select_space),
            style = BodyBold,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )
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
            .noRippleClickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(86.dp)
                .width(92.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            SpaceIconView(
                icon = icon,
                mainSize = 80.dp,
                onSpaceIconClick = onClick
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(color = colorResource(id = R.color.glyph_active)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_checked_24),
                        contentDescription = "Selected",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Space Name
        Text(
            text = name.ifEmpty { stringResource(R.string.untitled) },
            style = Relations3,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val state = CommentTextField(
            commentText = commentText,
            onCommentChanged = onCommentChanged,
            badgeTopPadding = 20.dp,
            textFieldTopPadding = 12.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Send Button
        ButtonOnboardingPrimaryLarge(
            text = stringResource(R.string.send),
            onClick = onSendClicked,
            size = ButtonSize.Large,
            enabled = !state.isOverLimit,
            modifierBox = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Character counter badge overlaid on top of the comment text field.
 * Shows format: "currentCount / maxCount"
 * Displays in red when over limit.
 *
 * Note: This badge should only be shown when character count is at or exceeds the limit.
 * The badge is positioned at the top-center of the text field as an overlay.
 *
 * @param currentCount Current character count
 * @param maxCount Maximum allowed characters
 * @param isOverLimit Whether current count exceeds max
 * @param modifier Modifier for the badge
 */
@Composable
private fun CharacterCounterBadge(
    currentCount: Int,
    maxCount: Int,
    isOverLimit: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$currentCount / $maxCount",
            style = Caption1Medium,
            color = if (isOverLimit) {
                colorResource(id = R.color.palette_system_red)
            } else {
                colorResource(id = R.color.text_secondary)
            }
        )
    }
}

/**
 * Core text field component with character counter badge.
 * Extracted shared logic from CommentSection and CommentInputField.
 *
 * @param commentText Current comment text (for initial state, internal state managed via innerValue)
 * @param onCommentChanged Callback when comment text changes
 * @param badgeTopPadding Padding from top of Box to the badge (6.dp for CommentInputField, 20.dp for CommentSection)
 * @param textFieldTopPadding Padding from top of text field (0.dp for CommentInputField, 12.dp for CommentSection)
 * @param modifier Modifier for the component
 * @return CommentFieldState with isOverLimit flag for callers needing button state
 */
@Composable
internal fun CommentTextField(
    commentText: String,
    onCommentChanged: (String) -> Unit,
    badgeTopPadding: Dp = 6.dp,
    textFieldTopPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
): CommentFieldState {
    var innerValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(commentText))
    }

    // Sync external state changes (e.g., when parent clears commentText)
    LaunchedEffect(commentText) {
        if (innerValue.text != commentText) {
            innerValue = TextFieldValue(commentText)
        }
    }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Character counter badge (shown only when at or over limit)
    val characterCount = innerValue.text.length
    val maxCharacters = MAX_MESSAGE_CHARACTER_LIMIT
    val isOverLimit = characterCount > maxCharacters
    val isAtOrOverLimit = characterCount >= maxCharacters

    // Comment Text Field with overlay badge
    Box(
        modifier = modifier
    ) {
        OutlinedTextField(
            value = innerValue,
            onValueChange = {
                innerValue = it
                onCommentChanged(it.text)
            },
            textStyle = BodySemiBold.copy(
                color = colorResource(id = R.color.text_primary)
            ),
            maxLines = 5,
            enabled = true,
            colors = TextFieldDefaults.colors(
                disabledTextColor = colorResource(id = R.color.text_primary),
                cursorColor = colorResource(id = R.color.color_accent),
                focusedContainerColor = colorResource(id = R.color.shape_transparent_secondary),
                unfocusedContainerColor = colorResource(id = R.color.shape_transparent_secondary),
                errorContainerColor = colorResource(id = R.color.shape_transparent_secondary),
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                errorIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = textFieldTopPadding)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions {
                keyboardController?.hide()
                focusManager.clearFocus()
            },
            shape = RoundedCornerShape(size = 26.dp),
            placeholder = {
                Text(
                    modifier = Modifier.padding(start = 1.dp),
                    text = stringResource(id = R.string.add_a_comment),
                    style = BodyRegular,
                    color = colorResource(id = R.color.text_tertiary)
                )
            }
        )

        // Badge overlaying the text field
        if (isAtOrOverLimit) {
            CharacterCounterBadge(
                currentCount = characterCount,
                maxCount = maxCharacters,
                isOverLimit = isOverLimit,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = badgeTopPadding)
            )
        }
    }

    return CommentFieldState(isOverLimit = isOverLimit)
}

/**
 * Standalone comment input field that can be reused across screens.
 * Uses Material3 OutlinedTextField with proper keyboard handling.
 *
 * @param commentText Current comment text (for display, state managed via innerValue)
 * @param onCommentChanged Callback when comment text changes
 * @param modifier Modifier for the text field
 */
@Composable
internal fun CommentInputField(
    commentText: String,
    onCommentChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    CommentTextField(
        commentText = commentText,
        onCommentChanged = onCommentChanged,
        modifier = modifier.fillMaxWidth()
    )
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
            isSelected = true,
            isChatSpace = true
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

    Box {
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
}

@DefaultPreviews
@Composable
private fun SelectSpaceScreenEmptyPreview() {
    Box {
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
}

@DefaultPreviews
@Composable
private fun SelectSpaceScreenCharacterLimitExceededPreview() {
    val sampleSpace = SelectableSpaceItem(
        id = "1",
        icon = SpaceIcon.DataSpace.Placeholder(
            name = "Team Chat",
            color = SystemColor.PINK
        ),
        name = "Team Chat",
        isSelected = true,
        isChatSpace = true
    )

    // Generate text over 2000 characters to trigger the red badge and disabled button
    val longCommentText = "This is a very long comment that exceeds the character limit. ".repeat(40)

    Box {
        SelectSpaceScreen(
            spaces = listOf(sampleSpace),
            searchQuery = "",
            commentText = longCommentText,
            onSearchQueryChanged = {},
            onCommentChanged = {},
            onSpaceSelected = {},
            onSendClicked = {}
        )
    }
}

@DefaultPreviews
@Composable
private fun CommentSectionAtLimitPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colorResource(id = R.color.background_primary))
            .padding(16.dp)
    ) {
        Text(
            text = "At Limit (2000 chars) - Gray Badge",
            style = BodyBold,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        CommentSection(
            commentText = "x".repeat(2000),
            onCommentChanged = {},
            onSendClicked = {}
        )
    }
}

@DefaultPreviews
@Composable
private fun CommentSectionOverLimitPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colorResource(id = R.color.background_primary))
            .padding(16.dp)
    ) {
        Text(
            text = "Over Limit (2100 chars) - Red Badge, Disabled Button",
            style = BodyBold,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        CommentSection(
            commentText = "x".repeat(2100),
            onCommentChanged = {},
            onSendClicked = {}
        )
    }
}