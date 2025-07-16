package com.anytypeio.anytype.ui.vault

import android.content.res.Configuration
import android.os.Build.VERSION.SDK_INT
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.common.ReorderHapticFeedbackType
import com.anytypeio.anytype.core_ui.common.ShimmerEffect
import com.anytypeio.anytype.core_ui.common.rememberReorderHapticFeedback
import com.anytypeio.anytype.core_ui.foundation.DefaultSearchBar
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.presentation.objects.ObjectIcon.Profile.Avatar
import com.anytypeio.anytype.presentation.profile.AccountProfile
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.presentation.spaces.SelectSpaceViewModel
import com.anytypeio.anytype.presentation.vault.VaultSectionView
import com.anytypeio.anytype.presentation.vault.VaultSpaceView
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@Composable
fun VaultScreenToolbar(
    profile: AccountProfile,
    spaceCountLimitReached: Boolean = false,
    showNotificationBadge: Boolean = false,
    onPlusClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    isLoading: Boolean
) {

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight()
                    .noRippleThrottledClickable {
                        onSettingsClicked()
                    },
                contentAlignment = Alignment.Center

            ) {
                ProfileIconWithBadge(
                    modifier = Modifier.size(34.dp),
                    profile = profile,
                    showBadge = showNotificationBadge
                )
            }

            Text(
                modifier = Modifier.align(Alignment.Center),
                style = Title1,
                text = stringResource(R.string.vault_my_spaces),
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!spaceCountLimitReached) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(64.dp)
                        .fillMaxHeight()
                        .noRippleThrottledClickable {
                            onPlusClicked()
                        },
                    contentAlignment = Alignment.Center

                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_plus_18),
                        contentDescription = stringResource(R.string.content_description_plus_button),
                        modifier = Modifier.size(28.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        if (isLoading) {
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ProfileIcon(
    modifier: Modifier,
    profile: AccountProfile
) {
    when (profile) {
        is AccountProfile.Data -> {
            Box(modifier) {
                when (val icon = profile.icon) {
                    is ProfileIconView.Image -> {
                        Image(
                            painter = rememberAsyncImagePainter(icon.url),
                            contentDescription = "Custom image profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    else -> {
                        val nameFirstChar = if (profile.name.isEmpty()) {
                            stringResource(id = R.string.account_default_name)
                        } else {
                            profile.name.first().uppercaseChar().toString()
                        }
                        ListWidgetObjectIcon(
                            modifier = Modifier.fillMaxSize(),
                            icon = Avatar(
                                name = nameFirstChar
                            ),
                            iconSize = 28.dp
                        )
                    }
                }
            }
        }

        AccountProfile.Idle -> {
            // Draw nothing
        }
    }
}

@Composable
private fun ProfileIconWithBadge(
    modifier: Modifier,
    profile: AccountProfile,
    showBadge: Boolean = false
) {
    Box(
        modifier = modifier
    ) {
        // Main profile icon
        ProfileIcon(
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.Center),
            profile = profile
        )

        // Badge positioned in top-right corner
        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(12.dp)
                    .background(
                        color = colorResource(id = R.color.background_primary),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Spacer(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = colorResource(id = R.color.palette_system_red),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode - Not Scrolled"
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode - Not Scrolled"
)
fun VaultScreenToolbarNotScrolledPreview() {
    VaultScreenToolbar(
        onPlusClicked = {},
        onSettingsClicked = {},
        profile = AccountProfile.Data(
            name = "John Doe",
            icon = ProfileIconView.Placeholder(name = "Jd")
        ),
        isLoading = false,
    )
}

@Composable
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode - Scrolled"
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode - Scrolled"
)
fun VaultScreenToolbarScrolledPreview() {
    VaultScreenToolbar(
        onPlusClicked = {},
        onSettingsClicked = {},
        profile = AccountProfile.Data(
            name = "John Doe",
            icon = ProfileIconView.Placeholder(name = "Jd")
        ),
        isLoading = false,
        showNotificationBadge = true
    )
}

@Composable
fun SpaceActionsDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    isMuted: Boolean?,
    isOwner: Boolean,
    isPinned: Boolean,
    pinnedSpacesCount: Int,
    maxPinnedSpaces: Int,
    onMuteToggle: () -> Unit,
    onPinToggle: () -> Unit,
    onSpaceSettings: () -> Unit
) {
    DropdownMenu(
        modifier = Modifier.width(254.dp),
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = colorResource(R.color.background_secondary),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 8.dp,
        offset = DpOffset(
            x = 16.dp,
            y = 8.dp
        )
    ) {
        // Pin/Unpin or Info message
        if (!isPinned && pinnedSpacesCount >= maxPinnedSpaces) {
            // Show info message instead of Pin
            Row(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.vault_pinned_limit_message, maxPinnedSpaces),
                    style = PreviewTitle2Regular,
                    color = colorResource(id = R.color.text_secondary)
                )
            }
        } else {
            DropdownMenuItem(
                onClick = {
                    onPinToggle()
                    onDismiss()
                },
                text = {
                    val stringRes = if (isPinned) {
                        R.string.vault_unpin_space
                    } else {
                        R.string.vault_pin_space
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringResource(id = stringRes),
                            style = BodyRegular,
                            color = colorResource(id = R.color.text_primary)
                        )
                        Image(
                            painter = painterResource(id = if (isPinned) R.drawable.ic_unpin_24 else R.drawable.ic_pin_24),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(24.dp)
                        )
                    }
                }
            )
        }

        // Mute/Unmute only if chat enabled
        if (isMuted != null) {
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            DropdownMenuItem(
                onClick = {
                    onMuteToggle()
                    onDismiss()
                },
                text = {
                    val (stringRes, iconRes) = if (isMuted) {
                        R.string.space_notify_unmute to R.drawable.ic_notifications
                    } else {
                        R.string.space_notify_mute to R.drawable.ic_notifications_off
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringResource(id = stringRes),
                            style = BodyRegular,
                            color = colorResource(id = R.color.text_primary)
                        )
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(24.dp)
                        )
                    }
                }
            )
        }
        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
        // Space Settings (always shown)
        DropdownMenuItem(
            onClick = {
                onSpaceSettings()
                onDismiss()
            },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(1f),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary),
                        text = stringResource(R.string.vault_space_settings)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_space_settings_24),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(24.dp)
                    )
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "SpaceActionsDropdownMenu - Muted Owner")
@Composable
fun PreviewSpaceActionsDropdownMenu_MutedOwner() {
    var expanded by remember { mutableStateOf(true) }
    Box(Modifier.fillMaxSize()) {
        SpaceActionsDropdownMenu(
            expanded = expanded,
            onDismiss = { expanded = false },
            isMuted = true,
            isOwner = true,
            isPinned = false,
            pinnedSpacesCount = 0,
            maxPinnedSpaces = VaultSectionView.MAX_PINNED_SPACES,
            onMuteToggle = {},
            onPinToggle = {},
            onSpaceSettings = {}
        )
    }
}

@Preview(showBackground = true, name = "SpaceActionsDropdownMenu - Unmuted Not Owner")
@Composable
fun PreviewSpaceActionsDropdownMenu_UnmutedNotOwner() {
    var expanded by remember { mutableStateOf(true) }
    Box(Modifier.fillMaxSize()) {
        SpaceActionsDropdownMenu(
            expanded = expanded,
            onDismiss = { expanded = false },
            isMuted = false,
            isOwner = false,
            isPinned = false,
            pinnedSpacesCount = 0,
            maxPinnedSpaces = VaultSectionView.MAX_PINNED_SPACES,
            onMuteToggle = {},
            onPinToggle = {},
            onSpaceSettings = {}
        )
    }
}

@Composable
fun SpaceActionsDropdownMenuHost(
    spaceView: VaultSpaceView,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onMuteSpace: (Id) -> Unit,
    onUnmuteSpace: (Id) -> Unit,
    onPinSpace: (Id) -> Unit,
    onUnpinSpace: (Id) -> Unit,
    pinnedSpacesCount: Int,
    maxPinnedSpaces: Int,
    onSpaceSettings: (Id) -> Unit
) {
    SpaceActionsDropdownMenu(
        expanded = expanded,
        onDismiss = onDismiss,
        isMuted = spaceView.isMuted,
        isOwner = spaceView.isOwner,
        isPinned = spaceView.isPinned,
        pinnedSpacesCount = pinnedSpacesCount,
        maxPinnedSpaces = maxPinnedSpaces,
        onMuteToggle = {
            spaceView.space.targetSpaceId?.let {
                if (spaceView.isMuted == true) onUnmuteSpace(it) else onMuteSpace(it)
            }
        },
        onPinToggle = {
            spaceView.space.id.let {
                if (spaceView.isPinned) onUnpinSpace(it) else onPinSpace(it)
            }
        },
        onSpaceSettings = {
            spaceView.space.id.let { onSpaceSettings(it) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreenWithUnreadSection(
    profile: AccountProfile,
    sections: VaultSectionView,
    showNotificationBadge: Boolean = false,
    onSpaceClicked: (VaultSpaceView) -> Unit,
    onCreateSpaceClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    isLoading: Boolean,
    onMuteSpace: (Id) -> Unit,
    onUnmuteSpace: (Id) -> Unit,
    onDeleteSpace: (String) -> Unit,
    onLeaveSpace: (String) -> Unit,
    onPinSpace: (Id) -> Unit,
    onUnpinSpace: (Id) -> Unit,
    onOrderChanged: (String, String) -> Unit,
    onDragEnd: () -> Unit = { /* No-op */ },
    onSpaceSettings: (Id) -> Unit
) {

    var searchQuery by remember { mutableStateOf("") }
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

    val lazyListState = rememberLazyListState()
    var expandedSpaceId by remember { mutableStateOf<String?>(null) }


    val hapticFeedback = rememberReorderHapticFeedback()

    val view = LocalView.current

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
            delay(50)
        } else if (isDragging) {
            isDragging = false
            onDragEnd()
            hapticFeedback.performHapticFeedback(ReorderHapticFeedbackType.MOVE)
        }
    }

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
            Column {
                VaultScreenToolbar(
                    profile = profile,
                    showNotificationBadge = showNotificationBadge,
                    onPlusClicked = onCreateSpaceClicked,
                    onSettingsClicked = onSettingsClicked,
                    spaceCountLimitReached = sections.mainSpaces.size >= VaultSectionView.MAX_PINNED_SPACES,
                    isLoading = isLoading
                )
                DefaultSearchBar(
                    value = searchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    onQueryChanged = { query -> searchQuery = query }
                )
            }
        }
    ) { paddings ->
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
                        ) { isDragging ->
                            val alpha = animateFloatAsState(if (isDragging) 0.8f else 1.0f)

                            when (item) {
                                is VaultSpaceView.Chat -> {
                                    VaultChatCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .padding(horizontal = 16.dp)
                                            .graphicsLayer(alpha = alpha.value)
                                            .animateItem()
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
                                        icon = item.icon,
                                        previewText = item.previewText,
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
                                        pinnedSpacesCount = sections.pinnedSpaces.size,
                                        maxPinnedSpaces = VaultSectionView.MAX_PINNED_SPACES,
                                        onSpaceSettings = onSpaceSettings
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
                                        pinnedSpacesCount = sections.pinnedSpaces.size,
                                        maxPinnedSpaces = VaultSectionView.MAX_PINNED_SPACES,
                                        onSpaceSettings = onSpaceSettings
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
                                    previewText = item.previewText,
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
                                    pinnedSpacesCount = sections.pinnedSpaces.size,
                                    maxPinnedSpaces = VaultSectionView.MAX_PINNED_SPACES,
                                    onSpaceSettings = onSpaceSettings
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
                                    pinnedSpacesCount = sections.pinnedSpaces.size,
                                    maxPinnedSpaces = VaultSectionView.MAX_PINNED_SPACES,
                                    onSpaceSettings = onSpaceSettings
                                )
                            }
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