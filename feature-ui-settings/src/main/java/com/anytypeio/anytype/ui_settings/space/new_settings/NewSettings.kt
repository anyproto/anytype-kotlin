package com.anytypeio.anytype.ui_settings.space.new_settings

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Medium
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.presentation.spaces.ChatNotificationItem
import com.anytypeio.anytype.presentation.spaces.UiEvent
import com.anytypeio.anytype.presentation.spaces.UiEvent.OnChangeSpaceType.ToChat
import com.anytypeio.anytype.presentation.spaces.UiEvent.OnChangeSpaceType.ToSpace
import com.anytypeio.anytype.presentation.spaces.UiEvent.OnDefaultObjectTypeClicked
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsItem
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsState
import com.anytypeio.anytype.presentation.wallpaper.WallpaperView
import com.anytypeio.anytype.ui_settings.BuildConfig
import com.anytypeio.anytype.ui_settings.R
import java.util.Locale
import timber.log.Timber


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSpaceSettingsScreen(
    uiState: UiSpaceSettingsState,
    uiWallpaperState: List<WallpaperView>,
    chatsWithCustomNotifications: List<ChatNotificationItem>,
    locale: Locale?,
    uiEvent: (UiEvent) -> Unit
) {

    if (uiState !is UiSpaceSettingsState.SpaceSettings) return

    val initialName = uiState.name
    val initialDescription = uiState.description

    var showEditDescription by remember { mutableStateOf(false) }
    var showEditTitle by remember { mutableStateOf(false) }
    var showTechInfo by remember { mutableStateOf(false) }
    var showNotificationsSettings by remember { mutableStateOf(false) }
    var showChangeTypeSheet by remember { mutableStateOf(false) }
    var showChangeTypeConfirmation by remember { mutableStateOf(false) }
    var selectedSpaceType by remember {
        mutableStateOf<UiSpaceSettingsItem.ChangeType>(
            UiSpaceSettingsItem.ChangeType.Data()
        )
    }
    val showWallpaperPicker = remember { mutableStateOf(false) }
    val wallpaperSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colorResource(id = R.color.background_primary),
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .fillMaxHeight()
                        .noRippleThrottledClickable {
                            uiEvent(UiEvent.OnBackPressed)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier
                            .wrapContentSize(),
                        painter = painterResource(R.drawable.ic_default_top_back),
                        contentDescription = stringResource(R.string.content_desc_back_button)
                    )
                }
                if (uiState.isEditEnabled) {
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .fillMaxHeight()
                            .noRippleThrottledClickable {
                                showEditTitle = true
                            }
                            .align(Alignment.CenterEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 16.dp),
                            text = stringResource(id = R.string.edit),
                            color = colorResource(id = R.color.glyph_active),
                            style = BodyRegular
                        )
                    }
                }
            }
        },
        content = { paddingValues ->

            val lazyListState = rememberLazyListState()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp),
                state = lazyListState,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(
                    items = uiState.items,
                    key = { _, item -> item.key },
                ) { _, item ->
                    when (item) {
                        is UiSpaceSettingsItem.Icon -> {
                            NewSpaceIcon(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                icon = item.icon,
                                isEditEnabled = uiState.isEditEnabled,
                                uiEvent = uiEvent
                            )
                        }

                        is UiSpaceSettingsItem.Name -> {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .animateItem()
                                    .noRippleClickable {
                                        if (uiState.isEditEnabled) {
                                            showEditTitle = true
                                        }
                                    },
                                text = item.name,
                                style = HeadlineHeading,
                                color = colorResource(id = R.color.text_primary),
                                textAlign = TextAlign.Center
                            )
                        }

                        is UiSpaceSettingsItem.ParticipantIdentity -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Name row with membership badge
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 32.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.name,
                                        style = HeadlineHeading,
                                        color = colorResource(id = R.color.text_primary),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (!item.globalName.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        // Blue circle badge with white checkmark
                                        Image(
                                            modifier = Modifier.size(18.dp),
                                            painter = painterResource(id = R.drawable.ic_membership_badge_18),
                                            contentDescription = "membership badge"
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                // Global name or identity below
                                val displayIdentity =
                                    item.globalName?.takeIf { it.isNotEmpty() }
                                        ?: item.identity
                                if (!displayIdentity.isNullOrEmpty()) {
                                    Text(
                                        text = displayIdentity,
                                        style = Caption1Regular,
                                        color = colorResource(id = R.color.text_secondary),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.MiddleEllipsis,
                                        modifier = Modifier.width(108.dp)
                                    )
                                }
                            }
                        }

                        is UiSpaceSettingsItem.MembersSmall -> {
                            val text = if (locale != null && item.count > 0) {
                                pluralStringResource(
                                    id = R.plurals.multiplayer_number_of_space_members,
                                    item.count,
                                    item.count
                                )
                            } else {
                                if (locale == null) {
                                    Timber.e("Error getting the locale")
                                }
                                stringResource(id = R.string.three_dots_text_placeholder)
                            }
                            Text(
                                modifier = Modifier
                                    .padding(horizontal = 32.dp)
                                    .fillMaxWidth()
                                    .animateItem(),
                                text = text,
                                style = Caption1Regular,
                                color = colorResource(id = R.color.text_secondary),
                                textAlign = TextAlign.Center
                            )
                        }

                        is UiSpaceSettingsItem.EntrySpace -> {
                            Text(
                                modifier = Modifier
                                    .padding(horizontal = 32.dp)
                                    .fillMaxWidth()
                                    .animateItem(),
                                text = stringResource(id = R.string.default_space),
                                style = Caption1Regular,
                                color = colorResource(id = R.color.text_secondary),
                                textAlign = TextAlign.Center
                            )
                        }

                        is UiSpaceSettingsItem.Description -> {
                            NewSpaceDescriptionBlock(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        shape = RoundedCornerShape(16.dp),
                                        width = 0.5.dp,
                                        color = colorResource(id = R.color.shape_primary)
                                    )
                                    .padding(vertical = 12.dp, horizontal = 16.dp)
                                    .animateItem()
                                    .noRippleClickable {
                                        showEditDescription = true
                                    },
                                isEditEnabled = false,
                                description = initialDescription
                            )
                        }

                        is UiSpaceSettingsItem.InviteLink -> {
                            MultiplayerButtons(
                                link = item.link,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                uiEvent = uiEvent
                            )
                        }

                        is UiSpaceSettingsItem.Chat -> {
                            // TODO
                        }

                        is UiSpaceSettingsItem.DefaultObjectType -> {
                            DefaultTypeItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { uiEvent(OnDefaultObjectTypeClicked(item.id)) },
                                name = item.name,
                                icon = item.icon
                            )
                        }

                        UiSpaceSettingsItem.DeleteSpace -> {
                            DeleteSpaceItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { uiEvent(UiEvent.OnDeleteSpaceClicked) },
                            )
                        }

                        UiSpaceSettingsItem.LeaveSpace -> {
                            LeaveSpaceItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { uiEvent(UiEvent.OnLeaveSpaceClicked) },
                            )
                        }

                        is UiSpaceSettingsItem.Members -> {
                            MembersItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { uiEvent(UiEvent.OnSpaceMembersClicked) },
                                item = item
                            )
                        }

                        is UiSpaceSettingsItem.InviteMembers -> {
                            BaseButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        uiEvent(UiEvent.OnInviteClicked)
                                    },
                                title = stringResource(id = R.string.space_settings_invite_members),
                                icon = R.drawable.ic_space_settings_invite_members
                            )
                        }

                        UiSpaceSettingsItem.ObjectTypes -> {
                            ObjectTypesItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        uiEvent(UiEvent.OnObjectTypesClicked)
                                    }
                            )
                        }

                        UiSpaceSettingsItem.Fields -> {
                            FieldsItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        uiEvent(UiEvent.OnPropertiesClicked)
                                    }
                            )
                        }

                        is UiSpaceSettingsItem.RemoteStorage -> {
                            RemoteStorageItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { uiEvent(UiEvent.OnRemoteStorageClick) }
                            )
                        }

                        is UiSpaceSettingsItem.Bin -> {
                            BinItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { uiEvent(UiEvent.OnBinClick) }
                            )
                        }

                        is UiSpaceSettingsItem.Section -> {
                            SpaceSettingsSection(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                item = item
                            )
                        }

                        UiSpaceSettingsItem.SpaceInfo -> {
                            SpaceInfoItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        showTechInfo = true
                                    }
                            )
                        }

                        is UiSpaceSettingsItem.Wallpapers -> {
                            WallpaperItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        showWallpaperPicker.value = true
                                    },
                                item = item
                            )
                        }

                        is UiSpaceSettingsItem.Spacer -> {
                            Spacer(modifier = Modifier.height(item.height.dp))
                        }

                        is UiSpaceSettingsItem.Notifications -> {
                            val (icon, supportText) = when (uiState.notificationState) {
                                NotificationState.ALL -> R.drawable.ic_bell_24 to stringResource(id = R.string.notifications_all_short)
                                NotificationState.MENTIONS -> R.drawable.ic_bell_24 to stringResource(
                                    id = R.string.notifications_mentions_short
                                )

                                NotificationState.DISABLE -> R.drawable.ic_bell_cross_24 to stringResource(
                                    id = R.string.notifications_disable_short
                                )
                            }
                            NotificationsItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        showNotificationsSettings = true
                                    },
                                icon = icon,
                                supportText = supportText
                            )
                        }

                        is UiSpaceSettingsItem.ChangeType -> {
                            ChangeTypeItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (item.isEnabled) {
                                            showChangeTypeSheet = true
                                        }
                                    },
                                currentType = item
                            )
                        }
                    }
                }
            }

            // Wallpaper Selection Modal
            if (showWallpaperPicker.value) {
                ModalBottomSheet(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .fillMaxSize(),
                    onDismissRequest = {
                        showWallpaperPicker.value = false
                    },
                    sheetState = wallpaperSheetState,
                    containerColor = colorResource(id = R.color.background_secondary),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    dragHandle = {
                        Dragger(
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                ) {
                    WallpaperSelectBottomSheet(
                        state = uiWallpaperState,
                        onWallpaperSelected = { selectedWallpaper ->
                            showWallpaperPicker.value = false
                            uiEvent(
                                UiEvent.OnUpdateWallpaperClicked(
                                    wallpaperView = selectedWallpaper
                                )
                            )
                        }
                    )
                }
            }
        }
    )

    if (showEditDescription) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            modifier = Modifier.padding(top = 48.dp),
            containerColor = colorResource(R.color.background_secondary),
            onDismissRequest = {
                showEditDescription = false
            },
            dragHandle = {
                Dragger(
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            },
        ) {
            EditDescriptionField(
                initialInput = initialDescription,
                onSaveFieldValueClicked = {
                    uiEvent(UiEvent.OnSaveDescriptionClicked(it)).also {
                        showEditDescription = false
                    }
                }
            )
        }
    }
    if (showEditTitle) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            modifier = Modifier.padding(top = 48.dp),
            containerColor = colorResource(R.color.background_secondary),
            onDismissRequest = {
                showEditTitle = false
            },
            dragHandle = {
                Dragger(
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        ) {
            EditNameField(
                initialInput = initialName,
                onSaveFieldValueClicked = {
                    uiEvent(UiEvent.OnSaveTitleClicked(it)).also {
                        showEditTitle = false
                    }
                }
            )
        }
    }

    if (showTechInfo) {
        ModalBottomSheet(
            containerColor = colorResource(R.color.background_secondary),
            onDismissRequest = {
                showTechInfo = false
            },
            dragHandle = {
                Dragger(
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            },
            content = {
                SpaceInfoScreen(
                    spaceTechInfo = uiState.spaceTechInfo,
                    isDebugBuild = BuildConfig.DEBUG,
                    onTitleClick = {
                        uiEvent(UiEvent.OnSpaceInfoTitleClicked)
                    },
                    onDebugClick = {
                        showTechInfo = false
                        uiEvent(UiEvent.OnDebugClicked)
                    }
                )
            }
        )
    }

    if (showNotificationsSettings) {
        NotificationsPreferenceSheet(
            targetSpaceId = uiState.targetSpaceId,
            currentState = uiState.notificationState,
            chatsWithCustomNotifications = chatsWithCustomNotifications,
            uiEvent = uiEvent,
            onDismiss = {
                showNotificationsSettings = false
            }
        )
    }

    if (showChangeTypeSheet) {
        val changeTypeItem =
            uiState.items.filterIsInstance<UiSpaceSettingsItem.ChangeType>().firstOrNull()
        if (changeTypeItem != null) {
            ChannelTypeBottomSheet(
                currentType = changeTypeItem,
                onTypeSelected = { selectedType ->
                    selectedSpaceType = selectedType
                    showChangeTypeSheet = false
                    showChangeTypeConfirmation = true
                },
                onDismiss = {
                    showChangeTypeSheet = false
                }
            )
        }
    }

    if (showChangeTypeConfirmation) {
        ChangeTypeConfirmationDialog(
            onConfirm = {
                showChangeTypeConfirmation = false
                when (selectedSpaceType) {
                    is UiSpaceSettingsItem.ChangeType.Chat -> uiEvent(ToChat)
                    is UiSpaceSettingsItem.ChangeType.Data -> uiEvent(ToSpace)
                }
            },
            onCancel = {
                showChangeTypeConfirmation = false
            },
            onDismiss = {
                showChangeTypeConfirmation = false
            }
        )
    }
}

@Composable
private fun EditNameField(
    initialInput: String,
    onSaveFieldValueClicked: (String) -> Unit
) {

    var fieldInput by remember { mutableStateOf(initialInput) }

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        containerColor = colorResource(id = R.color.background_secondary),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(horizontal = 16.dp)
                        .noRippleClickable {
                            onSaveFieldValueClicked(fieldInput)
                        },
                    text = "Done",
                    style = PreviewTitle1Medium,
                    color = if (fieldInput != initialInput) {
                        colorResource(id = R.color.text_primary)
                    } else {
                        colorResource(id = R.color.text_tertiary)
                    }
                )
            }
        },
        content = { paddingValues ->
            val contentModifier =
                if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .fillMaxSize()
                else
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)

            Box(
                modifier = contentModifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                NewSpaceNameInputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            shape = RoundedCornerShape(16.dp),
                            width = 2.dp,
                            color = colorResource(id = R.color.palette_system_amber_50)
                        )
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    name = fieldInput,
                    onNameSet = { newName ->
                        fieldInput = newName
                    },
                    isEditEnabled = true
                )
            }
        }
    )
}

@Composable
private fun EditDescriptionField(
    initialInput: String,
    onSaveFieldValueClicked: (String) -> Unit
) {
    var fieldInput by remember { mutableStateOf(initialInput) }

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        containerColor = colorResource(id = R.color.background_secondary),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(horizontal = 16.dp)
                        .noRippleClickable {
                            onSaveFieldValueClicked(fieldInput)
                        },
                    text = stringResource(R.string.done),
                    style = PreviewTitle1Medium,
                    color = if (fieldInput != initialInput) {
                        colorResource(id = R.color.text_primary)
                    } else {
                        colorResource(id = R.color.text_tertiary)
                    }
                )
            }
        },
        content = { paddingValues ->
            val contentModifier =
                if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                else
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)

            Box(
                modifier = contentModifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                NewSpaceDescriptionBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            shape = RoundedCornerShape(16.dp),
                            width = 2.dp,
                            color = colorResource(id = R.color.palette_system_amber_50)
                        )
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    description = fieldInput,
                    isEditEnabled = true,
                    onDescriptionSet = {
                        fieldInput = it
                    }
                )
            }
        }
    )
}