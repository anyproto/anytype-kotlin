package com.anytypeio.anytype.ui_settings.space.new_settings

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Medium
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.presentation.spaces.UiEvent
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsItem
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsState
import com.anytypeio.anytype.ui_settings.R

@Composable
fun SpaceSettingsContainer(
    uiState: UiSpaceSettingsState,
    uiEvent: (UiEvent) -> Unit
) {
    if (uiState is UiSpaceSettingsState.SpaceSettings) {
        NewSpaceSettingsScreen(
            uiState = uiState,
            uiEvent = uiEvent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSpaceSettingsScreen(
    uiState: UiSpaceSettingsState.SpaceSettings,
    uiEvent: (UiEvent) -> Unit
) {
    val localContext = LocalContext.current
    val initialName = uiState.name
    val initialDescription = uiState.description

    var showEditDescription by remember { mutableStateOf(false) }
    var showEditTitle by remember { mutableStateOf(false) }
    var showTechInfo by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        containerColor = colorResource(id = R.color.background_primary),
        topBar = {
            Box(
                modifier = if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .fillMaxWidth()
                        .height(48.dp)
                else
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_home_top_toolbar_back),
                    contentDescription = "Back button",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(48.dp)
                        .align(Alignment.CenterStart)
                        .noRippleClickable {
                            uiEvent(UiEvent.OnBackPressed)
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
            val lazyListState = rememberLazyListState()

            LazyColumn(
                modifier = contentModifier
                    .padding(horizontal = 16.dp),
                state = lazyListState,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                uiState.items.forEach { item ->
                    when (item) {
                        is UiSpaceSettingsItem.Icon -> {
                            item {
                                NewSpaceIcon(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem(),
                                    icon = item.icon,
                                    isEditEnabled = uiState.isEditEnabled,
                                    uiEvent = uiEvent
                                )
                            }
                        }

                        is UiSpaceSettingsItem.Name -> {
                            item {
                                NewSpaceNameInputField(
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
                                            showEditTitle = true
                                        },
                                    name = item.name,
                                    isEditEnabled = false
                                )
                            }
                        }

                        is UiSpaceSettingsItem.Description -> {
                            item {
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
                        }

                        UiSpaceSettingsItem.Multiplayer -> {
                            item {
                                MultiplayerButtons(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    uiEvent = uiEvent
                                )
                            }
                        }

                        is UiSpaceSettingsItem.Chat -> {
                            // TODO
                        }

                        is UiSpaceSettingsItem.DefaultObjectType -> {
                            item {
                                DefaultTypeItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { uiEvent(UiEvent.OnDefaultObjectTypeClicked(item.id)) },
                                    name = item.name,
                                    icon = item.icon
                                )
                            }
                        }

                        UiSpaceSettingsItem.DeleteSpace -> {
                            item {
                                DeleteSpaceItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { uiEvent(UiEvent.OnDeleteSpaceClicked) },
                                )
                            }
                        }

                        is UiSpaceSettingsItem.Members -> {
                            item {
                                MembersItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { uiEvent(UiEvent.OnSpaceMembersClicked) },
                                    item = item
                                )
                            }
                        }
                        is UiSpaceSettingsItem.InviteMembers -> {
                            item {
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
                        }
                        UiSpaceSettingsItem.ObjectTypes -> {
                            item {
                                ObjectTypesItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            localContext.toast("TODO")
                                        }
                                )
                            }
                        }

                        UiSpaceSettingsItem.Fields -> {
                            item {
                                FieldsItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            localContext.toast("TODO")
                                        }
                                )
                            }
                        }

                        is UiSpaceSettingsItem.RemoteStorage -> {
                            item {
                                RemoteStorageItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { uiEvent(UiEvent.OnRemoteStorageClick) }
                                )
                            }
                        }

                        is UiSpaceSettingsItem.Bin -> {
                            item {
                                BinItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { uiEvent(UiEvent.OnBinClick) }
                                )
                            }
                        }

                        is UiSpaceSettingsItem.Section -> {
                            item {
                                SpaceSettingsSection(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem(),
                                    item = item
                                )
                            }
                        }

                        UiSpaceSettingsItem.SpaceInfo -> {
                            item {
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
                        }
                        is UiSpaceSettingsItem.Wallpapers -> {
                            item {
                                WallpaperItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            uiEvent(UiEvent.OnSelectWallpaperClicked)
                                        },
                                    item = item
                                )
                            }
                        }

                        is UiSpaceSettingsItem.Spacer -> {
                            item {
                                Spacer(modifier = Modifier.height(item.height.dp))
                            }
                        }
                    }

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
                    spaceTechInfo = uiState.spaceTechInfo
                )
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