package com.anytypeio.anytype.core_ui.features.sharing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_ui.widgets.SearchField
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.confgs.ChatConfig.MAX_MESSAGE_CHARACTER_LIMIT

/**
 * Data model for destination object items in the list
 */
data class DestinationObjectItem(
    val id: String,
    val name: String,
    val icon: ObjectIcon,
    val typeName: String,
    val isSelected: Boolean = false,
    val isChatOption: Boolean = false
)

/**
 * Screen for selecting destination objects within a space.
 * Supports multi-selection of up to 5 destinations.
 * Used for Flow 2 (Data Space without chat) and Flow 3 (Data Space with chat).
 *
 * @param spaceName Name of the selected space
 * @param objects List of objects to display
 * @param chatObjects List of chat objects (CHAT_DERIVED layout) in the space
 * @param searchQuery Current search query
 * @param selectedObjectIds Set of selected object IDs (multi-select)
 * @param commentText Current comment text (shown when any chat is selected)
 * @param showCommentInput Whether to show the comment input field (true when any chat selected)
 * @param onSearchQueryChanged Callback when search query changes
 * @param onObjectSelected Callback when an object is selected/deselected
 * @param onCommentChanged Callback when comment text changes
 * @param onSendClicked Callback when Send/Save button is clicked
 * @param onBackPressed Callback when back button is pressed
 */
@Composable
fun BoxScope.SelectDestinationObjectScreen(
    spaceName: String,
    objects: List<DestinationObjectItem>,
    chatObjects: List<DestinationObjectItem> = emptyList(),
    searchQuery: String,
    selectedObjectIds: Set<String>,
    commentText: String,
    showCommentInput: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onObjectSelected: (DestinationObjectItem) -> Unit,
    onNewObjectClicked: () -> Unit,
    onCommentChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Track real-time character count for button state
    var currentCharCount by remember { mutableStateOf(commentText.length) }

    // Track if "New object" option is selected
    val isNewObjectSelected = selectedObjectIds.isEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background_primary))
    ) {
        // Header with back button
        HeaderSectionWithBack(
            spaceName = spaceName,
            onBackPressed = onBackPressed
        )

        // Search bar
        SearchField(
            horizontalPadding = 20.dp,
            query = searchQuery,
            onQueryChanged = onSearchQueryChanged,
            enabled = true,
            onFocused = {}
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Object list
        LazyColumn(
            modifier = Modifier
                .nestedScroll(rememberNestedScrollInteropConnection())
                .weight(1f)
                .fillMaxWidth()
        ) {
            // New object option - always appears first
            item(key = "new_object_option") {
                NewObjectListItem(
                    isSelected = isNewObjectSelected,
                    onClick = onNewObjectClicked
                )
            }
            item {
                Divider()
            }
            // Chat objects section (if any chats exist in the space)
            if (chatObjects.isNotEmpty()) {
                items(
                    items = chatObjects,
                    key = { "chat_${it.id}" }
                ) { chat ->
                    ObjectListItem(
                        item = chat,
                        isSelected = chat.id in selectedObjectIds,
                        onClick = { onObjectSelected(chat) }
                    )
                    Divider()
                }
            }

            // Empty state
            if (objects.isEmpty() && searchQuery.isNotEmpty()) {
                item {
                    EmptySearchState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    )
                }
            }

            // Object items
            items(
                items = objects,
                key = { it.id }
            ) { obj ->
                ObjectListItem(
                    item = obj,
                    isSelected = obj.id in selectedObjectIds,
                    onClick = { onObjectSelected(obj) }
                )
                Divider()
            }

            item {
                Spacer(modifier = Modifier.height(200.dp))
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .background(
                color = colorResource(R.color.background_primary),
                shape = RoundedCornerShape(20.dp)
                )
            .navigationBarsPadding()
            .align(Alignment.BottomCenter)
    ) {
        // Comment input field (shown above the list when chat is selected)
        if (showCommentInput) {
            CommentInputField(
                commentText = commentText,
                onCommentChanged = { text ->
                    currentCharCount = text.length
                    onCommentChanged(text)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Bottom button
        // Button enabled logic: must have selection AND comment within limit
        val hasSelection = selectedObjectIds.isNotEmpty() || isNewObjectSelected
        val isCommentOverLimit = if (showCommentInput) {
            currentCharCount > MAX_MESSAGE_CHARACTER_LIMIT
        } else {
            false
        }
        val isButtonEnabled = hasSelection && !isCommentOverLimit

        ButtonOnboardingPrimaryLarge(
            text = if (showCommentInput) {
                stringResource(R.string.send)
            } else {
                stringResource(R.string.save)
            },
            onClick = onSendClicked,
            size = ButtonSize.Large,
            enabled = isButtonEnabled,
            modifierBox = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
private fun HeaderSectionWithBack(
    spaceName: String,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .size(48.dp)
                .noRippleThrottledClickable {
                    onBackPressed()
                },
            contentScale = ContentScale.Inside,
            painter = painterResource(R.drawable.ic_back_24),
            contentDescription = "Back",
        )

        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            text = spaceName.ifEmpty { stringResource(R.string.untitled) },
            style = Title1,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Spacer to balance the back button
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun ObjectListItem(
    item: DestinationObjectItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Object icon
        ListWidgetObjectIcon(
            icon = item.icon,
            modifier = Modifier.size(48.dp),
            iconSize = 48.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.name.ifEmpty { stringResource(R.string.untitled) },
                style = Title2,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.typeName.isNotEmpty()) {
                Text(
                    text = item.typeName,
                    style = Caption1Regular,
                    color = colorResource(id = R.color.text_secondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Selection indicator
        if (isSelected) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_checkbox_checked),
                contentDescription = "Selected",
            )
        } else {
            Spacer(
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun NewObjectListItem(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Plus icon in a circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = colorResource(id = R.color.shape_primary),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_default_plus),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = stringResource(R.string.new_object),
            style = Title2,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.weight(1f)
        )

        // Selection indicator
        if (isSelected) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_checkbox_checked),
                contentDescription = "Selected"
            )
        } else {
            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun EmptySearchState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_doc_search),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.sharing_no_objects_found),
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
private fun SelectDestinationObjectScreenPreview() {
    val sampleObjects = listOf(
        DestinationObjectItem(
            id = "1",
            name = "Meeting Notes",
            icon = ObjectIcon.Basic.Emoji("📝"),
            typeName = "Note"
        ),
        DestinationObjectItem(
            id = "2",
            name = "Project Ideas",
            icon = ObjectIcon.Basic.Emoji("💡"),
            typeName = "Page"
        ),
        DestinationObjectItem(
            id = "3",
            name = "Weekly Review",
            icon = ObjectIcon.Basic.Emoji("📅"),
            typeName = "Task"
        )
    )

    val sampleChatObjects = listOf(
        DestinationObjectItem(
            id = "chat1",
            name = "Team Chat",
            icon = ObjectIcon.Basic.Emoji("💬"),
            typeName = "Chat",
            isChatOption = true
        )
    )

    Box {
        SelectDestinationObjectScreen(
            spaceName = "Work Space",
            objects = sampleObjects,
            chatObjects = sampleChatObjects,
            searchQuery = "",
            selectedObjectIds = emptySet(),
            commentText = "",
            showCommentInput = false,
            onSearchQueryChanged = {},
            onObjectSelected = {},
            onNewObjectClicked = {},
            onCommentChanged = {},
            onSendClicked = {},
            onBackPressed = {}
        )
    }
}

@DefaultPreviews
@Composable
private fun SelectDestinationObjectScreenWithChatSelectedPreview() {
    val chatObject = DestinationObjectItem(
        id = "chat",
        name = "Team Chat",
        icon = ObjectIcon.Basic.Emoji("💬"),
        typeName = "Chat",
        isChatOption = true
    )

    Box {
        SelectDestinationObjectScreen(
            spaceName = "Work Space",
            objects = emptyList(),
            chatObjects = listOf(chatObject),
            searchQuery = "",
            selectedObjectIds = setOf(chatObject.id),
            commentText = "Check this out!",
            showCommentInput = true,
            onSearchQueryChanged = {},
            onObjectSelected = {},
            onNewObjectClicked = {},
            onCommentChanged = {},
            onSendClicked = {},
            onBackPressed = {}
        )
    }
}
