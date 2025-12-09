package com.anytypeio.anytype.core_ui.features.sharing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.DefaultSearchBar
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon

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
fun SelectDestinationObjectScreen(
    spaceName: String,
    objects: List<DestinationObjectItem>,
    chatObjects: List<DestinationObjectItem> = emptyList(),
    searchQuery: String,
    selectedObjectIds: Set<String>,
    commentText: String,
    showCommentInput: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onObjectSelected: (DestinationObjectItem) -> Unit,
    onCommentChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background_primary))
    ) {
        // Header with back button
        HeaderSection(
            spaceName = spaceName,
            onBackPressed = onBackPressed
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        DefaultSearchBar(
            value = searchQuery,
            onQueryChanged = onSearchQueryChanged,
            hint = R.string.search,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Object list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            item {
                Text(
                    text = stringResource(R.string.sharing_select_dest),
                    style = Caption1Medium,
                    color = colorResource(id = R.color.text_secondary),
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
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
                }
                item(key = "chats_divider") {
                    Divider(
                        color = colorResource(id = R.color.shape_primary),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
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
                Divider(
                    color = colorResource(id = R.color.shape_primary),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Bottom section
        BottomSection(
            showCommentInput = showCommentInput,
            commentText = commentText,
            onCommentChanged = onCommentChanged,
            onSendClicked = onSendClicked,
            buttonText = if (showCommentInput) {
                stringResource(R.string.send)
            } else {
                stringResource(R.string.save)
            }
        )
    }
}

@Composable
private fun HeaderSection(
    spaceName: String,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackPressed) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colorResource(id = R.color.glyph_active)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.sharing_select_destination),
                style = BodyBold,
                color = colorResource(id = R.color.text_primary),
                textAlign = TextAlign.Center
            )
            Text(
                text = spaceName,
                style = Caption1Regular,
                color = colorResource(id = R.color.text_secondary),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

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
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                style = PreviewTitle2Regular,
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
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_checkbox_unchecked),
                contentDescription = "Selected",
            )
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

@Composable
private fun BottomSection(
    showCommentInput: Boolean,
    commentText: String,
    onCommentChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    buttonText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = colorResource(id = R.color.background_primary))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Comment input (only shown when "Send to chat" is selected)
        if (showCommentInput) {
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
        }

        // Send/Save button
        ButtonPrimary(
            text = buttonText,
            onClick = onSendClicked,
            size = ButtonSize.Large,
            modifier = Modifier.fillMaxWidth()
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
        onCommentChanged = {},
        onSendClicked = {},
        onBackPressed = {}
    )
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
        onCommentChanged = {},
        onSendClicked = {},
        onBackPressed = {}
    )
}
