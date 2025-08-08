package com.anytypeio.anytype.ui.vault

import android.os.Build.VERSION.SDK_INT
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.common.ReorderHapticFeedbackType
import com.anytypeio.anytype.core_ui.common.rememberReorderHapticFeedback
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.presentation.profile.AccountProfile
import com.anytypeio.anytype.presentation.vault.VaultSpaceView
import com.anytypeio.anytype.presentation.vault.VaultUiState
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun VaultScreen(
    profile: AccountProfile,
    uiState: VaultUiState,
    showNotificationBadge: Boolean = false,
    onSpaceClicked: (VaultSpaceView) -> Unit,
    onCreateSpaceClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onMuteSpace: (Id) -> Unit,
    onUnmuteSpace: (Id) -> Unit,
    onPinSpace: (Id) -> Unit,
    onUnpinSpace: (Id) -> Unit,
    onOrderChanged: (String, String) -> Unit,
    onDragEnd: () -> Unit = { /* No-op */ },
    onSpaceSettings: (Id) -> Unit
) {

    var searchQuery by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection())
            .background(color = colorResource(id = R.color.background_primary))
            .then(
                if (SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier.windowInsetsPadding(WindowInsets.systemBars)
                else
                    Modifier
            ),
        backgroundColor = colorResource(id = R.color.background_primary),
        topBar = {
            VaultScreenTopToolbar(
                profile = profile,
                uiState = uiState,
                searchQuery = searchQuery,
                showNotificationBadge = showNotificationBadge,
                onCreateSpaceClicked = onCreateSpaceClicked,
                onSettingsClicked = onSettingsClicked,
                onUpdateSearchQuery = { query ->
                    searchQuery = query
                }
            )
        }
    ) { paddings ->
        when (uiState) {
            VaultUiState.Loading -> {}

            is VaultUiState.Sections -> {
                VaultScreenContent(
                    sections = uiState,
                    lazyListState = lazyListState,
                    paddings = paddings,
                    searchQuery = searchQuery,
                    onSpaceClicked = onSpaceClicked,
                    onCreateSpaceClicked = onCreateSpaceClicked,
                    onMuteSpace = onMuteSpace,
                    onUnmuteSpace = onUnmuteSpace,
                    onPinSpace = onPinSpace,
                    onUnpinSpace = onUnpinSpace,
                    onOrderChanged = onOrderChanged,
                    onDragEnd = onDragEnd,
                    onSpaceSettings = onSpaceSettings
                )
            }
        }
    }
}

@Composable
fun VaultScreenContent(
    sections: VaultUiState.Sections,
    lazyListState: LazyListState,
    paddings: PaddingValues,
    searchQuery: String,
    onSpaceClicked: (VaultSpaceView) -> Unit,
    onCreateSpaceClicked: () -> Unit,
    onMuteSpace: (Id) -> Unit,
    onUnmuteSpace: (Id) -> Unit,
    onPinSpace: (Id) -> Unit,
    onUnpinSpace: (Id) -> Unit,
    onOrderChanged: (String, String) -> Unit,
    onDragEnd: () -> Unit = { /* No-op */ },
    onSpaceSettings: (Id) -> Unit
) {
    var expandedSpaceId by remember { mutableStateOf<String?>(null) }

    val hapticFeedback = rememberReorderHapticFeedback()

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // Extract space IDs from keys (remove prefix) before passing to ViewModel
        val fromSpaceId = from.key as String
        val toSpaceId = to.key as String

        onOrderChanged(fromSpaceId, toSpaceId)
        hapticFeedback.performHapticFeedback(ReorderHapticFeedbackType.MOVE)
    }

    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(reorderableLazyListState.isAnyItemDragging) {
        if (reorderableLazyListState.isAnyItemDragging) {
            isDragging = true
            // Optional: Add a small delay to avoid triggering on very short drags
            delay(1000)
            expandedSpaceId = null
        } else if (isDragging) {
            isDragging = false
            onDragEnd()
            hapticFeedback.performHapticFeedback(ReorderHapticFeedbackType.MOVE)
        }
    }

    val filteredMainSpaces = remember(searchQuery, sections.mainSpaces) {
        if (searchQuery.isBlank()) {
            sections.mainSpaces
        } else {
            sections.mainSpaces.filter { space ->
                space.space.name?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    val filteredPinnedSpaces = remember(searchQuery, sections.pinnedSpaces) {
        if (searchQuery.isBlank()) {
            sections.pinnedSpaces
        } else {
            sections.pinnedSpaces.filter { space ->
                space.space.name?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    val hasAnyFilteredSpaces = filteredMainSpaces.isNotEmpty() || filteredPinnedSpaces.isNotEmpty()

    if (sections.mainSpaces.isEmpty() && sections.pinnedSpaces.isEmpty()) {
        VaultEmptyState(
            modifier = Modifier.padding(paddings),
            onCreateSpaceClicked = onCreateSpaceClicked
        )
    } else if (!hasAnyFilteredSpaces) {
        VaultEmptyState(
            modifier = Modifier.padding(paddings),
            textRes = R.string.vault_empty_search_state_text,
            showButton = false,
            onCreateSpaceClicked = onCreateSpaceClicked
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddings),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 4.dp)
        ) {
            // Pinned Spaces Section
            if (filteredPinnedSpaces.isNotEmpty()) {
                itemsIndexed(
                    items = filteredPinnedSpaces,
                    key = { _, item -> item.space.id },
                    contentType = { _, item ->
                        when (item) {
                            is VaultSpaceView.Chat -> TYPE_CHAT
                            is VaultSpaceView.Space -> TYPE_SPACE
                        }
                    }
                ) { index, item ->
                    ReorderableItem(
                        state = reorderableLazyListState,
                        key = item.space.id
                    ) { isItemDragging ->
                        val alpha = animateFloatAsState(if (isItemDragging) 0.8f else 1.0f)

                        when (item) {
                            is VaultSpaceView.Chat -> {
                                VaultChatCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .combinedClickable(
                                            onClick = {
                                                onSpaceClicked(item)
                                            },
                                            onLongClick = {
                                                expandedSpaceId = item.space.id
                                            }
                                        )
                                        .padding(horizontal = 16.dp)
                                        .graphicsLayer(alpha = alpha.value)
                                        .animateItem()
                                        .longPressDraggableHandle(
                                            onDragStarted = {
                                                hapticFeedback.performHapticFeedback(
                                                    ReorderHapticFeedbackType.START
                                                )
                                            },
                                            onDragStopped = {
                                                hapticFeedback.performHapticFeedback(
                                                    ReorderHapticFeedbackType.MOVE
                                                )
                                            }
                                        ),
                                    title = item.space.name.orEmpty(),
                                    icon = item.icon,
                                    creatorName = item.creatorName,
                                    messageText = item.messageText,
                                    messageTime = item.messageTime,
                                    chatPreview = item.chatPreview,
                                    unreadMessageCount = item.unreadMessageCount,
                                    unreadMentionCount = item.unreadMentionCount,
                                    attachmentPreviews = item.attachmentPreviews,
                                    isMuted = item.isMuted,
                                    isPinned = item.isPinned,
                                    spaceView = item,
                                    expandedSpaceId = expandedSpaceId,
                                    onDismissMenu = { expandedSpaceId = null },
                                    onMuteSpace = onMuteSpace,
                                    onUnmuteSpace = onUnmuteSpace,
                                    onPinSpace = onPinSpace,
                                    onUnpinSpace = onUnpinSpace,
                                    onSpaceSettings = onSpaceSettings,
                                    maxPinnedSpaces = VaultUiState.MAX_PINNED_SPACES,
                                    currentPinnedCount = sections.pinnedSpaces.size
                                )
                            }

                            is VaultSpaceView.Space -> {
                                VaultSpaceCard(
                                    modifier = Modifier
                                        .animateItem()
                                        .graphicsLayer(alpha = alpha.value)
                                        .combinedClickable(
                                            onClick = {
                                                onSpaceClicked(item)
                                            },
                                            onLongClick = {
                                                expandedSpaceId = item.space.id
                                            }
                                        )
                                        .longPressDraggableHandle(
                                            onDragStarted = {
                                                hapticFeedback.performHapticFeedback(
                                                    ReorderHapticFeedbackType.START
                                                )
                                            },
                                            onDragStopped = {
                                                hapticFeedback.performHapticFeedback(
                                                    ReorderHapticFeedbackType.MOVE
                                                )
                                            }
                                        ),
                                    title = item.space.name.orEmpty(),
                                    subtitle = item.accessType,
                                    icon = item.icon,
                                    isPinned = item.isPinned,
                                    spaceView = item,
                                    expandedSpaceId = expandedSpaceId,
                                    onDismissMenu = { expandedSpaceId = null },
                                    onMuteSpace = onMuteSpace,
                                    onUnmuteSpace = onUnmuteSpace,
                                    onPinSpace = onPinSpace,
                                    onUnpinSpace = onUnpinSpace,
                                    onSpaceSettings = onSpaceSettings,
                                    currentPinnedCount = sections.pinnedSpaces.size
                                )
                            }
                        }
                    }
                }
            }

            // Not pinned Spaces Section
            if (filteredMainSpaces.isNotEmpty()) {
                itemsIndexed(
                    items = filteredMainSpaces,
                    key = { _, item -> item.space.id },
                    contentType = { _, item ->
                        when (item) {
                            is VaultSpaceView.Chat -> TYPE_CHAT
                            is VaultSpaceView.Space -> TYPE_SPACE
                        }
                    }
                ) { _, item ->
                    when (item) {
                        is VaultSpaceView.Chat -> {
                            VaultChatCard(
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .then(
                                        createCombinedClickableModifier(
                                            onClick = { onSpaceClicked(item) },
                                            onLongClick = { expandedSpaceId = item.space.id }
                                        ))
                                    .padding(horizontal = 16.dp),
                                title = item.space.name.orEmpty(),
                                icon = item.icon,
                                creatorName = item.creatorName,
                                messageText = item.messageText,
                                messageTime = item.messageTime,
                                chatPreview = item.chatPreview,
                                unreadMessageCount = item.unreadMessageCount,
                                unreadMentionCount = item.unreadMentionCount,
                                attachmentPreviews = item.attachmentPreviews,
                                isMuted = item.isMuted,
                                isPinned = item.isPinned,
                                spaceView = item,
                                expandedSpaceId = expandedSpaceId,
                                onDismissMenu = { expandedSpaceId = null },
                                onMuteSpace = onMuteSpace,
                                onUnmuteSpace = onUnmuteSpace,
                                onPinSpace = onPinSpace,
                                onUnpinSpace = onUnpinSpace,
                                maxPinnedSpaces = VaultUiState.MAX_PINNED_SPACES,
                                onSpaceSettings = onSpaceSettings,
                                currentPinnedCount = sections.pinnedSpaces.size
                            )
                        }

                        is VaultSpaceView.Space -> {
                            VaultSpaceCard(
                                modifier = Modifier
                                    .animateItem()
                                    .then(
                                        createCombinedClickableModifier(
                                            onClick = { onSpaceClicked(item) },
                                            onLongClick = {
                                                expandedSpaceId = item.space.id
                                            }
                                        )),
                                title = item.space.name.orEmpty(),
                                subtitle = item.accessType,
                                isPinned = item.isPinned,
                                icon = item.icon,
                                spaceView = item,
                                expandedSpaceId = expandedSpaceId,
                                onDismissMenu = { expandedSpaceId = null },
                                onMuteSpace = onMuteSpace,
                                onUnmuteSpace = onUnmuteSpace,
                                onPinSpace = onPinSpace,
                                onUnpinSpace = onUnpinSpace,
                                onSpaceSettings = onSpaceSettings,
                                currentPinnedCount = sections.pinnedSpaces.size
                            )
                        }
                    }
                }
            }
        }
    }
}

const val TYPE_CHAT = "chat"
const val TYPE_SPACE = "space"


fun createCombinedClickableModifier(
    onClick: () -> Unit,
    onLongClick: () -> Unit
): Modifier = Modifier.combinedClickable(
    onClick = onClick,
    onLongClick = onLongClick
)