package com.anytypeio.anytype.core_ui.features.sharing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.presentation.sharing.SelectableObjectView
import com.anytypeio.anytype.presentation.sharing.SelectableSpaceView
import com.anytypeio.anytype.presentation.sharing.SharedContent
import com.anytypeio.anytype.presentation.sharing.SharingFlowType
import com.anytypeio.anytype.presentation.sharing.SharingScreenState
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

/**
 * Main sharing screen that orchestrates the different UI states.
 * Acts as a state machine, rendering the appropriate screen based on [SharingScreenState].
 */
@Composable
fun SharingScreen(
    state: SharingScreenState,
    onSpaceSelected: (SelectableSpaceView) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onCommentChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onObjectSelected: (SelectableObjectView) -> Unit,
    onBackPressed: () -> Unit,
    onCancelClicked: () -> Unit,
    onRetryClicked: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize()
            .background(
                color = colorResource(R.color.background_primary),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ),
    ) {
        SelectSpaceScreenHeader(
            modifier = Modifier.fillMaxWidth().height(64.dp)
        )
        when (state) {
            SharingScreenState.Loading -> {
                LoadingScreen()
            }

            is SharingScreenState.SpaceSelection -> {
                // Check if any chat space is selected to enable comment input and send
                val hasChatSpaceSelected = state.spaces.any {
                    it.isSelected && it.flowType == SharingFlowType.CHAT
                }

                val sendAction: () -> Unit = if (hasChatSpaceSelected) onSendClicked else {
                    {}
                }
                val commentAction: (String) -> Unit =
                    if (hasChatSpaceSelected) onCommentChanged else {
                        { _: String -> }
                    }

                SelectSpaceScreen(
                    spaces = state.spaces.map { it.toSelectableSpaceItem() },
                    searchQuery = state.searchQuery,
                    commentText = if (hasChatSpaceSelected) state.commentText else "",
                    onSearchQueryChanged = onSearchQueryChanged,
                    onCommentChanged = commentAction,
                    onSpaceSelected = { item ->
                        state.spaces.find { it.id == item.id }?.let { spaceView ->
                            onSpaceSelected(spaceView)
                        }
                    },
                    onSendClicked = sendAction
                )

            }

            is SharingScreenState.ChatInput -> TODO()
            is SharingScreenState.Error -> TODO()
            is SharingScreenState.ObjectSelection -> TODO()
            is SharingScreenState.Sending -> TODO()
            is SharingScreenState.Success -> TODO()
        }
//        when (state) {
//            is SharingScreenState.Loading -> {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth(),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    SelectSpaceScreenHeader()
//                    LoadingScreen()
//                }
//            }
//
//            is SharingScreenState.SpaceSelection -> {
//                // Check if any chat space is selected to enable comment input and send
//                val hasChatSpaceSelected = state.spaces.any {
//                    it.isSelected && it.flowType == SharingFlowType.CHAT
//                }
//
//                val sendAction: () -> Unit = if (hasChatSpaceSelected) onSendClicked else {{}}
//                val commentAction: (String) -> Unit = if (hasChatSpaceSelected) onCommentChanged else {{ _: String -> }}
//
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth(),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    SelectSpaceScreenHeader()
//                    SelectSpaceScreen(
//                        spaces = state.spaces.map { it.toSelectableSpaceItem() },
//                        searchQuery = state.searchQuery,
//                        commentText = if (hasChatSpaceSelected) state.commentText else "",
//                        onSearchQueryChanged = onSearchQueryChanged,
//                        onCommentChanged = commentAction,
//                        onSpaceSelected = { item ->
//                            state.spaces.find { it.id == item.id }?.let { spaceView ->
//                                onSpaceSelected(spaceView)
//                            }
//                        },
//                        onSendClicked = sendAction
//                    )
//                }
//            }
//
//            is SharingScreenState.ChatInput -> {
//                ChatInputScreen(
//                    selectedSpaces = state.selectedSpaces,
//                    commentText = state.commentText,
//                    onCommentChanged = onCommentChanged,
//                    onSendClicked = onSendClicked,
//                    onBackPressed = onBackPressed
//                )
//            }
//
//            is SharingScreenState.ObjectSelection -> {
//                SelectDestinationObjectScreen(
//                    spaceName = state.space.name,
//                    objects = state.objects.map { it.toDestinationObjectItem() },
//                    chatObjects = state.chatObjects.map { it.toDestinationObjectItem() },
//                    searchQuery = state.searchQuery,
//                    selectedObjectIds = state.selectedObjectIds,
//                    commentText = state.commentText,
//                    showCommentInput = state.hasAnyChatSelected,
//                    onSearchQueryChanged = onSearchQueryChanged,
//                    onObjectSelected = { item ->
//                        if (item.isChatOption) {
//                            // Find the chat object from the dynamically discovered chatObjects
//                            state.chatObjects.find { it.id == item.id }?.let { chatObj ->
//                                onObjectSelected(chatObj)
//                            }
//                        } else {
//                            state.objects.find { it.id == item.id }?.let { objView ->
//                                onObjectSelected(objView)
//                            }
//                        }
//                    },
//                    onCommentChanged = onCommentChanged,
//                    onSendClicked = onSendClicked,
//                    onBackPressed = onBackPressed
//                )
//            }
//
//            is SharingScreenState.Sending -> {
//                SendingScreen(
//                    progress = state.progress,
//                    message = state.message
//                )
//            }
//
//            is SharingScreenState.Success -> {
//                SuccessScreen(
//                    spaceName = state.spaceName,
//                    canOpenObject = state.canOpenObject,
//                    onDoneClicked = onCancelClicked
//                )
//            }
//
//            is SharingScreenState.Error -> {
//                ErrorScreen(
//                    message = state.message,
//                    canRetry = state.canRetry,
//                    onRetryClicked = onRetryClicked,
//                    onCancelClicked = onCancelClicked
//                )
//            }
//        }
    }
}

@Composable
private fun LoadingScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = colorResource(id = R.color.glyph_active),
            modifier = Modifier.size(48.dp)
        )
    }
}

/**
 * Screen for chat input when chat space(s) are selected.
 * Shows selected spaces and comment input.
 */
@Composable
private fun ChatInputScreen(
    selectedSpaces: List<SelectableSpaceView>,
    commentText: String,
    onCommentChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    // For now, use SelectSpaceScreen with all spaces selected
    // In a full implementation, this would be a dedicated screen showing selected chats
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background_primary))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (selectedSpaces.size == 1) {
                    stringResource(R.string.sharing_send_to, selectedSpaces.first().name)
                } else {
                    stringResource(R.string.sharing_send_to_multiple, selectedSpaces.size)
                },
                style = BodyBold,
                color = colorResource(id = R.color.text_primary),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Comment section (reuse from SelectSpaceScreen pattern)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.background_primary))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Comment field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colorResource(id = R.color.shape_primary),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = commentText,
                    onValueChange = onCommentChanged,
                    textStyle = BodyRegular.copy(
                        color = colorResource(id = R.color.text_primary)
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(
                        colorResource(id = R.color.glyph_active)
                    ),
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

            // Send button
            ButtonPrimary(
                text = stringResource(R.string.send),
                onClick = onSendClicked,
                size = ButtonSize.Large,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SendingScreen(
    progress: Float,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background_primary)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = colorResource(id = R.color.glyph_active),
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = message,
            style = BodyRegular,
            color = colorResource(id = R.color.text_secondary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = progress,
            color = colorResource(id = R.color.glyph_active),
            backgroundColor = colorResource(id = R.color.shape_primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
        )
    }
}

@Composable
private fun SuccessScreen(
    spaceName: String,
    canOpenObject: Boolean,
    onDoneClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background_primary))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_tick_24),
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.sharing_success),
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.sharing_added_to_space, spaceName),
            style = BodyRegular,
            color = colorResource(id = R.color.text_secondary),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        ButtonPrimary(
            text = stringResource(R.string.done),
            onClick = onDoneClicked,
            size = ButtonSize.Large,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    canRetry: Boolean,
    onRetryClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background_primary))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_popup_alert_56),
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.sharing_error),
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = BodyRegular,
            color = colorResource(id = R.color.text_secondary),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (canRetry) {
            ButtonPrimary(
                text = stringResource(R.string.retry),
                onClick = onRetryClicked,
                size = ButtonSize.Large,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        ButtonSecondary(
            text = stringResource(R.string.cancel),
            onClick = onCancelClicked,
            size = ButtonSize.Large,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Extension function to convert SelectableSpaceView to SelectableSpaceItem for the UI.
 */
private fun SelectableSpaceView.toSelectableSpaceItem(): SelectableSpaceItem {
    return SelectableSpaceItem(
        id = id,
        icon = icon,
        name = name,
        isSelected = isSelected,
        isChatSpace = flowType == SharingFlowType.CHAT
    )
}

/**
 * Extension function to convert SelectableObjectView to DestinationObjectItem for the UI.
 */
private fun SelectableObjectView.toDestinationObjectItem(): DestinationObjectItem {
    return DestinationObjectItem(
        id = id,
        name = name,
        icon = icon,
        typeName = typeName,
        isSelected = isSelected,
        isChatOption = isChatOption
    )
}

// --- PREVIEWS ---
//@Preview(name = "Loading State", showBackground = true)
//@Composable
//private fun SharingScreenPreview_Loading() {
//    MaterialTheme { // Wrap with your app's theme for consistent styling
//        SharingScreen(
//            state = SharingScreenState.Loading,
//            onSpaceSelected = {},
//            onSearchQueryChanged = {},
//            onCommentChanged = {},
//            onSendClicked = {},
//            onObjectSelected = {},
//            onBackPressed = {},
//            onCancelClicked = {},
//            onRetryClicked = {}
//        )
//    }
//}

@Preview(name = "Space Selection State", showBackground = true)
@Composable
private fun SharingScreenPreview_SpaceSelection() {
    val mockSpaces = listOf(
        StubSelectableSpaceView(id = "1", name = "Personal Space", isSelected = false),
        StubSelectableSpaceView(id = "2", name = "Team Projects", isSelected = true),
        StubSelectableSpaceView(id = "3", name = "", isSelected = false)
    )

    MaterialTheme {
        SharingScreen(
            state = SharingScreenState.SpaceSelection(
                spaces = listOf(),
                searchQuery = "",
                sharedContent = SharedContent.Url("fdsfsd")
            ),
            onSpaceSelected = {},
            onSearchQueryChanged = {},
            onCommentChanged = {},
            onSendClicked = {},
            onObjectSelected = {},
            onBackPressed = {},
            onCancelClicked = {},
            onRetryClicked = {}
        )
    }
}
//
//@Preview(name = "Object Selection State", showBackground = true)
//@Composable
//private fun SharingScreenPreview_ObjectSelection() {
//    val mockSpace = StubSelectableSpaceView(id = "2", name = "Team Projects", isSelected = true)
//    val mockObjects = listOf(
//        SelectableObjectView(id = "obj1", name = "Q1 Roadmap", typeName = "Page"),
//        SelectableObjectView(id = "obj2", name = "Sprint Planning", typeName = "Board"),
//        SelectableObjectView(id = "obj3", name = "Design Mockups", typeName = "Collection")
//    )
//
//    MaterialTheme {
//        SharingScreen(
//            screenState = SharingScreenState.ObjectSelection(
//                space = mockSpace,
//                objects = mockObjects,
//                searchQuery = "",
//                selectedObject = mockObjects[1],
//                showChatOption = true,
//                sharedContent = SharedContent.Url("www.google.com")
//            ),
//            onSpaceSelected = {},
//            onSearchQueryChanged = {},
//            onCommentChanged = {},
//            onSendClicked = {},
//            onObjectSelected = {},
//            onBackPressed = {},
//            onCancelClicked = {},
//            onRetryClicked = {}
//        )
//    }
//}
//
//@Preview(name = "Sending State", showBackground = true)
//@Composable
//private fun SharingScreenPreview_Sending() {
//    MaterialTheme {
//        SharingScreen(
//            screenState = SharingScreenState.Sending(
//                progress = 0.6f,
//                message = "Encrypting and sending..."
//            ),
//            onSpaceSelected = {},
//            onSearchQueryChanged = {},
//            onCommentChanged = {},
//            onSendClicked = {},
//            onObjectSelected = {},
//            onBackPressed = {},
//            onCancelClicked = {},
//            onRetryClicked = {}
//        )
//    }
//}
//
//@Preview(name = "Success State", showBackground = true)
//@Composable
//private fun SharingScreenPreview_Success() {
//    MaterialTheme {
//        SharingScreen(
//            screenState = SharingScreenState.Success(
//                spaceName = "Team Projects",
//                canOpenObject = true
//            ),
//            onSpaceSelected = {},
//            onSearchQueryChanged = {},
//            onCommentChanged = {},
//            onSendClicked = {},
//            onObjectSelected = {},
//            onBackPressed = {},
//            onCancelClicked = {},
//            onRetryClicked = {}
//        )
//    }
//}
//
//@Preview(name = "Error State", showBackground = true)
//@Composable
//private fun SharingScreenPreview_Error() {
//    MaterialTheme {
//        SharingScreen(
//            screenState = SharingScreenState.Error(
//                message = "Failed to connect. Please check your network and try again.",
//                canRetry = true
//            ),
//            onSpaceSelected = {},
//            onSearchQueryChanged = {},
//            onCommentChanged = {},
//            onSendClicked = {},
//            onObjectSelected = {},
//            onBackPressed = {},
//            onCancelClicked = {},
//            onRetryClicked = {}
//        )
//    }
//}
//
/**
 * Creates a stub instance of SelectableSpaceView for testing.
 */
fun StubSelectableSpaceView(
    id: Id = "stub-id",
    targetSpaceId: Id = "stub-target-id",
    name: String = "Stub Space",
    icon: SpaceIconView = SpaceIconView.DataSpace.Placeholder(name = "K"),
    uxType: SpaceUxType? = null,
    chatId: Id? = null,
    isSelected: Boolean = false
): SelectableSpaceView {
    return SelectableSpaceView(
        id = id,
        targetSpaceId = targetSpaceId,
        name = name,
        icon = icon,
        uxType = uxType,
        chatId = chatId,
        isSelected = isSelected
    )
}
