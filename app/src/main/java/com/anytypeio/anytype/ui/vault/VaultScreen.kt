package com.anytypeio.anytype.ui.vault

import android.content.res.Configuration
import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.util.DraggableItem
import com.anytypeio.anytype.core_ui.foundation.util.dragContainer
import com.anytypeio.anytype.core_ui.foundation.util.rememberDragDropState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import com.anytypeio.anytype.core_ui.views.AvatarTitle
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.animations.conditionalBackground
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.presentation.profile.AccountProfile
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.presentation.spaces.SelectSpaceViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.vault.VaultSpaceView
import com.anytypeio.anytype.presentation.vault.VaultSectionView
import com.anytypeio.anytype.ui.settings.typography
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi


@Composable
fun VaultScreen(
    profile: AccountProfile,
    sections: VaultSectionView,
    onSpaceClicked: (VaultSpaceView) -> Unit,
    onCreateSpaceClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onOrderChanged: (List<Id>) -> Unit
) {
    var mainSpaceList by remember {
        mutableStateOf<List<VaultSpaceView>>(sections.mainSpaces)
    }

    mainSpaceList = sections.mainSpaces

    // Simple drag drop state - only for main spaces LazyColumn
    val mainSpacesLazyListState = rememberLazyListState()
    val isScrolled = remember {
        derivedStateOf {
            mainSpacesLazyListState.firstVisibleItemIndex > 0 || mainSpacesLazyListState.firstVisibleItemScrollOffset > 0
        }
    }
    val dragDropState = rememberDragDropState(
        lazyListState = mainSpacesLazyListState,
        onDragEnd = {
            onOrderChanged(
                mainSpaceList.map { it.space.id }
            )
        },
        onMove = { fromIndex, toIndex ->
            // Calculate offset due to unread section items
            val unreadSectionOffset = if (sections.hasUnreadSpaces) {
                1 + sections.unreadSpaces.size + (if (sections.mainSpaces.isNotEmpty()) 1 else 0) // header + unread items + divider
            } else {
                0
            }
            
            // Adjust indices to only affect main section items
            val adjustedFromIndex = fromIndex - unreadSectionOffset
            val adjustedToIndex = toIndex - unreadSectionOffset
            
            // Only process if both indices are within main section
            if (adjustedFromIndex >= 0 && adjustedToIndex >= 0 && 
                adjustedFromIndex < mainSpaceList.size && adjustedToIndex <= mainSpaceList.size) {
                mainSpaceList = mainSpaceList.toMutableList().apply { 
                    add(adjustedToIndex, removeAt(adjustedFromIndex)) 
                }
            }
        }
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background_primary))
            .then(
                if (SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier.windowInsetsPadding(WindowInsets.systemBars)
                else
                    Modifier
            ),
        backgroundColor = colorResource(id = R.color.background_primary),
        topBar = {
            VaultScreenToolbar(
                profile = profile,
                onPlusClicked = onCreateSpaceClicked,
                onSettingsClicked = onSettingsClicked,
                spaceCountLimitReached = sections.allSpaces.size >= SelectSpaceViewModel.MAX_SPACE_COUNT,
                isScrolled = isScrolled.value
            )
        }
    ) { paddings ->
        if (sections.allSpaces.isEmpty()) {
            VaultEmptyState(
                modifier = Modifier.padding(paddings),
                onCreateSpaceClicked = onCreateSpaceClicked
            )
                } else {
            // Single LazyColumn with combined content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddings)
                    .dragContainer(dragDropState),
                state = mainSpacesLazyListState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 4.dp)
            ) {
                // Unread Section Header and Items
                if (sections.hasUnreadSpaces) {
                    item(key = "unread_header") {
                        UnreadSectionHeader()
                    }
                    
                    itemsIndexed(
                        items = sections.unreadSpaces,
                        key = { _, item -> "unread_${item.space.id}" },
                        contentType = { _, item ->
                            when (item) {
                                is VaultSpaceView.Chat -> TYPE_CHAT
                                is VaultSpaceView.Space -> TYPE_SPACE
                                is VaultSpaceView.Loading -> TYPE_LOADING
                            }
                        }
                    ) { _, item ->
                        // Unread items are not draggable
                        when (item) {
                            is VaultSpaceView.Chat -> {
                                VaultChatCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .padding(horizontal = 16.dp)
                                        .clickable {
                                            onSpaceClicked(item)
                                        },
                                    title = item.space.name.orEmpty(),
                                    icon = item.icon,
                                    previewText = item.previewText,
                                    creatorName = item.creatorName,
                                    messageText = item.messageText,
                                    messageTime = item.messageTime,
                                    chatPreview = item.chatPreview,
                                    unreadMessageCount = item.unreadMessageCount,
                                    unreadMentionCount = item.unreadMentionCount,
                                    attachmentPreviews = item.attachmentPreviews
                                )
                            }
                            is VaultSpaceView.Loading -> {
                                LoadingSpaceCard()
                            }
                            is VaultSpaceView.Space -> {
                                VaultSpaceCard(
                                    title = item.space.name.orEmpty(),
                                    subtitle = item.accessType,
                                    onCardClicked = {
                                        onSpaceClicked(item)
                                    },
                                    icon = item.icon
                                )
                            }
                        }
                    }
                    
                    // Divider between sections
                    if (sections.mainSpaces.isNotEmpty()) {
                        item(key = "divider") {
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = colorResource(id = R.color.shape_primary),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }

                // Main Section Items (Draggable)
                if (sections.mainSpaces.isNotEmpty()) {
                    itemsIndexed(
                        items = mainSpaceList,
                        key = { _, item -> "main_${item.space.id}" },
                        contentType = { _, item ->
                            when (item) {
                                is VaultSpaceView.Chat -> TYPE_CHAT
                                is VaultSpaceView.Space -> TYPE_SPACE
                                is VaultSpaceView.Loading -> TYPE_LOADING
                            }
                        }
                    ) { idx, item ->
                        when (item) {
                            is VaultSpaceView.Chat -> {
                                DraggableItem(dragDropState = dragDropState, index = idx) {
                                    VaultChatCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .padding(horizontal = 16.dp)
                                            .clickable {
                                                onSpaceClicked(item)
                                            },
                                        title = item.space.name.orEmpty(),
                                        icon = item.icon,
                                        previewText = item.previewText,
                                        creatorName = item.creatorName,
                                        messageText = item.messageText,
                                        messageTime = item.messageTime,
                                        chatPreview = item.chatPreview,
                                        unreadMessageCount = item.unreadMessageCount,
                                        unreadMentionCount = item.unreadMentionCount,
                                        attachmentPreviews = item.attachmentPreviews
                                    )
                                }
                            }
                            is VaultSpaceView.Loading -> {
                                DraggableItem(dragDropState = dragDropState, index = idx) {
                                    LoadingSpaceCard()
                                }
                            }
                            is VaultSpaceView.Space -> {
                                DraggableItem(dragDropState = dragDropState, index = idx) {
                                    VaultSpaceCard(
                                        title = item.space.name.orEmpty(),
                                        subtitle = item.accessType,
                                        onCardClicked = {
                                            onSpaceClicked(item)
                                        },
                                        icon = item.icon
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnreadSectionHeader() {
    Text(
        text = stringResource(R.string.vault_unread_section_title),
        style = HeadlineTitle,
        color = colorResource(id = R.color.text_secondary),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

// Original VaultScreen function for backward compatibility
@Composable
fun VaultScreen(
    profile: AccountProfile,
    spaces: List<VaultSpaceView>,
    onSpaceClicked: (VaultSpaceView) -> Unit,
    onCreateSpaceClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onOrderChanged: (List<Id>) -> Unit
) {
    // Convert to VaultSectionView for compatibility
    val sections = VaultSectionView(
        unreadSpaces = emptyList(),
        mainSpaces = spaces
    )

    // Call the new VaultScreen function
    VaultScreen(
        profile = profile,
        sections = sections,
        onSpaceClicked = onSpaceClicked,
        onCreateSpaceClicked = onCreateSpaceClicked,
        onSettingsClicked = onSettingsClicked,
        onOrderChanged = onOrderChanged
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun VaultScreenToolbar(
    profile: AccountProfile,
    spaceCountLimitReached: Boolean = false,
    onPlusClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    isScrolled: Boolean = false
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .conditionalBackground(
                    condition = isScrolled,
                ) {
                    background(
                        color = colorResource(R.color.navigation_panel),
                    )
                }
                .height(44.dp)
        ) {
            if (isScrolled) {
                Text(
                    text = stringResource(R.string.vault_my_spaces),
                    style = Title1,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            when (profile) {
                is AccountProfile.Data -> {
                    Box(
                        Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp)
                            .size(28.dp)
                            .noRippleClickable {
                                onSettingsClicked()
                            }
                    ) {
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
                                    stringResource(id = com.anytypeio.anytype.ui_settings.R.string.account_default_name)
                                } else {
                                    profile.name.first().uppercaseChar().toString()
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(colorResource(id = com.anytypeio.anytype.ui_settings.R.color.text_tertiary))
                                ) {
                                    Text(
                                        text = nameFirstChar,
                                        style = AvatarTitle.copy(
                                            fontSize = 20.sp
                                        ),
                                        color = colorResource(id = R.color.text_white),
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }

                AccountProfile.Idle -> {
                    // Draw nothing
                }
            }

            if (!spaceCountLimitReached) {
                Image(
                    painter = painterResource(id = R.drawable.ic_plus_18),
                    contentDescription = "Plus button",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .size(32.dp)
                        .noRippleClickable {
                            onPlusClicked()
                        }
                )
            }
        }

        if (!isScrolled) {
            Text(
                modifier = Modifier.padding(top = 3.dp, bottom = 8.dp, start = 16.dp),
                text = stringResource(R.string.vault_my_spaces),
                style = HeadlineTitle.copy(
                    fontSize = 34.sp
                ),
                color = colorResource(id = R.color.text_primary),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun VaultSpaceAddCard(
    onCreateSpaceClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                color = colorResource(id = R.color.shape_tertiary),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable {
                onCreateSpaceClicked()
            }
    ) {
        Image(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(vertical = 32.dp),
            painter = painterResource(id = R.drawable.ic_vault_create_space_card_button_plus),
            contentDescription = "Plus icon"
        )
    }
}

@Composable
fun LoadingSpaceCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 8.dp)
            .background(
                color = colorResource(R.color.shape_tertiary),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(64.dp)
                .align(Alignment.CenterStart)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colorResource(R.color.shape_primary),
                            Color.Transparent,
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
        )

        Box(
            modifier = Modifier
                .padding(start = 96.dp, top = 30.dp)
                .height(12.dp)
                .width(160.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colorResource(R.color.shape_primary),
                            Color.Transparent,
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .padding(start = 96.dp, bottom = 30.dp)
                .height(8.dp)
                .width(96.dp)
                .align(Alignment.BottomStart)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colorResource(R.color.shape_primary),
                            Color.Transparent,
                        )
                    )
                )
        )
    }
}

@DefaultPreviews
@Composable
fun LoadingSpaceCardPreview() {
    LoadingSpaceCard()
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
        isScrolled = false
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
        isScrolled = true
    )
}

//@Composable
//@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
//@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
//fun VaultScreenPreview() {
//    VaultScreen(
//        spaces = buildList {
//            add(
//                VaultSpaceView(
//                    space = ObjectWrapper.SpaceView(
//                        mapOf(
//                            Relations.NAME to "B&O Museum",
//                            Relations.SPACE_ACCESS_TYPE to SpaceAccessType.SHARED.code.toDouble()
//                        )
//                    ),
//                    icon = SpaceIconView.Placeholder()
//                )
//            )
//        },
//        onSpaceClicked = {},
//        onCreateSpaceClicked = {},
//        onSettingsClicked = {},
//        onOrderChanged = {},
//        profile = AccountProfile.Idle
//    )
//}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VaultScreenAnotherWay(
    profile: AccountProfile,
    sections: VaultSectionView,
    onSpaceClicked: (VaultSpaceView) -> Unit,
    onCreateSpaceClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onOrderChanged: (List<Id>) -> Unit
) {
    var mainSpaceList by remember {
        mutableStateOf<List<VaultSpaceView>>(sections.mainSpaces)
    }

    mainSpaceList = sections.mainSpaces

    // Reorderable LazyColumn state for single list approach
    val lazyListState = rememberLazyListState()
    val isScrolled = remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
        }
    }
    
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // Calculate offset due to unread section items
        val unreadSectionOffset = if (sections.hasUnreadSpaces) {
            1 + sections.unreadSpaces.size + (if (sections.mainSpaces.isNotEmpty()) 1 else 0) // header + unread items + divider
        } else {
            0
        }
        
        // Adjust indices to only affect main section items
        val adjustedFromIndex = (from.index - unreadSectionOffset).coerceAtLeast(0)
        val adjustedToIndex = (to.index - unreadSectionOffset).coerceAtLeast(0)
        
        // Only process if both indices are within main section
        if (adjustedFromIndex < mainSpaceList.size && adjustedToIndex < mainSpaceList.size) {
            mainSpaceList = mainSpaceList.toMutableList().apply { 
                add(adjustedToIndex, removeAt(adjustedFromIndex)) 
            }
        }
    }

    LaunchedEffect(reorderableLazyListState.isAnyItemDragging) {
        if (!reorderableLazyListState.isAnyItemDragging) {
            onOrderChanged(mainSpaceList.map { it.space.id })
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background_primary))
            .then(
                if (SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier.windowInsetsPadding(WindowInsets.systemBars)
                else
                    Modifier
            ),
        backgroundColor = colorResource(id = R.color.background_primary),
        topBar = {
            VaultScreenToolbar(
                profile = profile,
                onPlusClicked = onCreateSpaceClicked,
                onSettingsClicked = onSettingsClicked,
                spaceCountLimitReached = sections.allSpaces.size >= SelectSpaceViewModel.MAX_SPACE_COUNT,
                isScrolled = isScrolled.value
            )
        }
    ) { paddings ->
        if (sections.allSpaces.isEmpty()) {
            VaultEmptyState(
                modifier = Modifier.padding(paddings),
                onCreateSpaceClicked = onCreateSpaceClicked
            )
        } else {
            // Single LazyColumn with reorderable approach
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddings),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 4.dp)
            ) {
                // Unread Section Header and Items (Non-draggable)
                if (sections.hasUnreadSpaces) {
                    item(key = "unread_header") {
                        UnreadSectionHeader()
                    }
                    
                    itemsIndexed(
                        items = sections.unreadSpaces,
                        key = { _, item -> "unread_${item.space.id}" },
                        contentType = { _, item ->
                            when (item) {
                                is VaultSpaceView.Chat -> TYPE_CHAT
                                is VaultSpaceView.Space -> TYPE_SPACE
                                is VaultSpaceView.Loading -> TYPE_LOADING
                            }
                        }
                    ) { _, item ->
                        // Unread items are not draggable
                        when (item) {
                            is VaultSpaceView.Chat -> {
                                VaultChatCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .padding(horizontal = 16.dp)
                                        .clickable {
                                            onSpaceClicked(item)
                                        },
                                    title = item.space.name.orEmpty(),
                                    icon = item.icon,
                                    previewText = item.previewText,
                                    creatorName = item.creatorName,
                                    messageText = item.messageText,
                                    messageTime = item.messageTime,
                                    chatPreview = item.chatPreview,
                                    unreadMessageCount = item.unreadMessageCount,
                                    unreadMentionCount = item.unreadMentionCount,
                                    attachmentPreviews = item.attachmentPreviews
                                )
                            }
                            is VaultSpaceView.Loading -> {
                                LoadingSpaceCard()
                            }
                            is VaultSpaceView.Space -> {
                                VaultSpaceCard(
                                    title = item.space.name.orEmpty(),
                                    subtitle = item.accessType,
                                    onCardClicked = {
                                        onSpaceClicked(item)
                                    },
                                    icon = item.icon
                                )
                            }
                        }
                    }
                    
                    // Divider between sections
                    if (sections.mainSpaces.isNotEmpty()) {
                        item(key = "divider") {
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = colorResource(id = R.color.shape_primary),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }

                // Main Section Items (Draggable using ReorderableItem)
                if (sections.mainSpaces.isNotEmpty()) {
                    itemsIndexed(
                        items = mainSpaceList,
                        key = { _, item -> "main_${item.space.id}" },
                        contentType = { _, item ->
                            when (item) {
                                is VaultSpaceView.Chat -> TYPE_CHAT
                                is VaultSpaceView.Space -> TYPE_SPACE
                                is VaultSpaceView.Loading -> TYPE_LOADING
                            }
                        }
                    ) { idx, item ->
                        ReorderableItem(
                            state = reorderableLazyListState,
                            key = "main_${item.space.id}"
                        ) { isDragging ->
                            when (item) {
                                is VaultSpaceView.Chat -> {
                                    VaultChatCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .padding(horizontal = 16.dp)
                                            .conditionalBackground(isDragging)
                                            .clickable {
                                                onSpaceClicked(item)
                                            },
                                        title = item.space.name.orEmpty(),
                                        icon = item.icon,
                                        previewText = item.previewText,
                                        creatorName = item.creatorName,
                                        messageText = item.messageText,
                                        messageTime = item.messageTime,
                                        chatPreview = item.chatPreview,
                                        unreadMessageCount = item.unreadMessageCount,
                                        unreadMentionCount = item.unreadMentionCount,
                                        attachmentPreviews = item.attachmentPreviews
                                    )
                                }
                                is VaultSpaceView.Loading -> {
                                    Box(modifier = Modifier.conditionalBackground(isDragging)) {
                                        LoadingSpaceCard()
                                    }
                                }
                                is VaultSpaceView.Space -> {
                                    Box(modifier = Modifier.conditionalBackground(isDragging)) {
                                        VaultSpaceCard(
                                            title = item.space.name.orEmpty(),
                                            subtitle = item.accessType,
                                            onCardClicked = {
                                                onSpaceClicked(item)
                                            },
                                            icon = item.icon
                                        )
                                    }
                                }
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
const val TYPE_LOADING = "loading"

// Preview functions for the new unread section logic
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode - Unread Section"
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode - Unread Section"
)
@Composable
fun VaultScreenUnreadSectionPreview() {
    MaterialTheme(typography = typography) {
        VaultScreen(
            profile = AccountProfile.Data(
                name = "John Doe",
                icon = ProfileIconView.Placeholder(name = "JD")
            ),
            sections = VaultSectionView(
                unreadSpaces = listOf(
                    VaultSpaceView.Chat(
                        space = ObjectWrapper.SpaceView(
                            mapOf(
                                Relations.ID to "chat1",
                                Relations.NAME to "Design Team Chat",
                                Relations.SPACE_ACCESS_TYPE to SpaceAccessType.SHARED.code.toDouble()
                            )
                        ),
                        icon = SpaceIconView.Placeholder(name = "DT"),
                        chatPreview = Chat.Preview(
                            space = SpaceId("space-id"),
                            chat = "chat-id",
                            message = Chat.Message(
                                id = "message-id",
                                createdAt = System.currentTimeMillis(),
                                modifiedAt = 0L,
                                attachments = emptyList(),
                                reactions = emptyMap(),
                                creator = "creator-id",
                                replyToMessageId = "",
                                content = Chat.Message.Content(
                                    text = "Hello, this is a preview message.",
                                    marks = emptyList(),
                                    style = Block.Content.Text.Style.P
                                ),
                                order = "order-id"
                            )
                        ),
                        previewText = "Alice: Hey team, the new designs are ready for review!",
                        creatorName = "Alice",
                        messageText = "Hey team, the new designs are ready for review!",
                        messageTime = "5m",
                        unreadMessageCount = 3,
                        unreadMentionCount = 1,
                        attachmentPreviews = emptyList()
                    ),
                    VaultSpaceView.Chat(
                        space = ObjectWrapper.SpaceView(
                            mapOf(
                                Relations.ID to "chat2",
                                Relations.NAME to "Product Planning",
                                Relations.SPACE_ACCESS_TYPE to SpaceAccessType.PRIVATE.code.toDouble()
                            )
                        ),
                        icon = SpaceIconView.Placeholder(name = "PP"),
                        chatPreview = Chat.Preview(
                            space = SpaceId("space-id"),
                            chat = "chat-id",
                            message = Chat.Message(
                                id = "message-id",
                                createdAt = System.currentTimeMillis(),
                                modifiedAt = 0L,
                                attachments = emptyList(),
                                reactions = emptyMap(),
                                creator = "creator-id",
                                replyToMessageId = "",
                                content = Chat.Message.Content(
                                    text = "Hello, this is a preview message.",
                                    marks = emptyList(),
                                    style = Block.Content.Text.Style.P
                                ),
                                order = "order-id"
                            )
                        ),
                        previewText = "Bob: Let's schedule the sprint planning meeting",
                        creatorName = "Bob",
                        messageText = "Let's schedule the sprint planning meeting",
                        messageTime = "15m",
                        unreadMessageCount = 1,
                        unreadMentionCount = 0,
                        attachmentPreviews = emptyList()
                    )
                ),
                mainSpaces = listOf(
                    VaultSpaceView.Space(
                        space = ObjectWrapper.SpaceView(
                            mapOf(
                                Relations.ID to "space1",
                                Relations.NAME to "Personal Notes",
                                Relations.SPACE_ACCESS_TYPE to SpaceAccessType.PRIVATE.code.toDouble()
                            )
                        ),
                        icon = SpaceIconView.Placeholder(name = "PN"),
                        accessType = "Private"
                    ),
                    VaultSpaceView.Chat(
                        space = ObjectWrapper.SpaceView(
                            mapOf(
                                Relations.ID to "chat3",
                                Relations.NAME to "General Discussion",
                                Relations.SPACE_ACCESS_TYPE to SpaceAccessType.SHARED.code.toDouble()
                            )
                        ),
                        icon = SpaceIconView.Placeholder(name = "GD"),
                        chatPreview = Chat.Preview(
                            space = SpaceId("space-id"),
                            chat = "chat-id",
                            message = Chat.Message(
                                id = "message-id",
                                createdAt = System.currentTimeMillis(),
                                modifiedAt = 0L,
                                attachments = emptyList(),
                                reactions = emptyMap(),
                                creator = "creator-id",
                                replyToMessageId = "",
                                content = Chat.Message.Content(
                                    text = "Hello, this is a preview message.",
                                    marks = emptyList(),
                                    style = Block.Content.Text.Style.P
                                ),
                                order = "order-id"
                            )
                        ),
                        previewText = "Charlie: Thanks for the update!",
                        creatorName = "Charlie",
                        messageText = "Thanks for the update!",
                        messageTime = "2h",
                        unreadMessageCount = 0, // No unread messages
                        unreadMentionCount = 0,
                        attachmentPreviews = emptyList()
                    ),
                    VaultSpaceView.Space(
                        space = ObjectWrapper.SpaceView(
                            mapOf(
                                Relations.ID to "space2",
                                Relations.NAME to "Work Projects",
                                Relations.SPACE_ACCESS_TYPE to SpaceAccessType.PRIVATE.code.toDouble()
                            )
                        ),
                        icon = SpaceIconView.Placeholder(name = "WP"),
                        accessType = "Private"
                    )
                )
            ),
            onSpaceClicked = {},
            onCreateSpaceClicked = {},
            onSettingsClicked = {},
            onOrderChanged = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode - No Unread Messages"
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode - No Unread Messages"
)
@Composable
fun VaultScreenNoUnreadPreview() {
    MaterialTheme(typography = typography) {
        VaultScreen(
            profile = AccountProfile.Data(
                name = "Jane Smith",
                icon = ProfileIconView.Placeholder(name = "JS")
            ),
            sections = VaultSectionView(
                unreadSpaces = emptyList(), // No unread messages
                mainSpaces = listOf(
                    VaultSpaceView.Space(
                        space = ObjectWrapper.SpaceView(
                            mapOf(
                                Relations.ID to "space1",
                                Relations.NAME to "Personal Notes",
                                Relations.SPACE_ACCESS_TYPE to SpaceAccessType.PRIVATE.code.toDouble()
                            )
                        ),
                        icon = SpaceIconView.Placeholder(name = "PN"),
                        accessType = "Private"
                    ),
                    VaultSpaceView.Chat(
                        space = ObjectWrapper.SpaceView(
                            mapOf(
                                Relations.ID to "chat1",
                                Relations.NAME to "Team Chat",
                                Relations.SPACE_ACCESS_TYPE to SpaceAccessType.SHARED.code.toDouble()
                            )
                        ),
                        icon = SpaceIconView.Placeholder(name = "TC"),
                        chatPreview = Chat.Preview(
                            space = SpaceId("space-id"),
                            chat = "chat-id",
                            message = Chat.Message(
                                id = "message-id",
                                createdAt = System.currentTimeMillis(),
                                modifiedAt = 0L,
                                attachments = emptyList(),
                                reactions = emptyMap(),
                                creator = "creator-id",
                                replyToMessageId = "",
                                content = Chat.Message.Content(
                                    text = "Hello, this is a preview message.",
                                    marks = emptyList(),
                                    style = Block.Content.Text.Style.P
                                ),
                                order = "order-id"
                            )
                        ),
                        previewText = "Alice: All caught up!",
                        creatorName = "Alice",
                        messageText = "All caught up!",
                        messageTime = "1h",
                        unreadMessageCount = 0, // No unread messages
                        unreadMentionCount = 0,
                        attachmentPreviews = emptyList()
                    )
                )
            ),
            onSpaceClicked = {},
            onCreateSpaceClicked = {},
            onSettingsClicked = {},
            onOrderChanged = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode - Unread Section Header Only"
)
@Composable
fun UnreadSectionHeaderPreview() {
    MaterialTheme(typography = typography) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.background_primary))
                .padding(vertical = 16.dp)
        ) {
            UnreadSectionHeader()
        }
    }
}