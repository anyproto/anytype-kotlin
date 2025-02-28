package com.anytypeio.anytype.ui_settings.space.new_settings

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Medium
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

@Composable
fun NewSpaceSettingsScreen(
    uiState: UiSpaceSettingsState.SpaceSettings,
    uiEvent: (UiEvent) -> Unit
) {

    // Get the initial values from your uiState items.
    val initialName = uiState.items.filterIsInstance<UiSpaceSettingsItem.Name>()
        .firstOrNull()?.name ?: ""
    val initialDescription = uiState.items.filterIsInstance<UiSpaceSettingsItem.Description>()
        .firstOrNull()?.description ?: ""

    // Keep state of the current (edited) values.
    var nameInput by remember { mutableStateOf(initialName) }
    var descriptionInput by remember { mutableStateOf(initialDescription) }

    // Compare against the initial values to know if something has changed.
    val isDirty = nameInput != initialName || descriptionInput != initialDescription

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
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .clickable(enabled = isDirty) {
                            uiEvent(UiEvent.OnSavedClicked(nameInput, descriptionInput))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(horizontal = 16.dp),
                        text = stringResource(R.string.space_settings_save_button),
                        style = PreviewTitle1Medium,
                        color = if (isDirty) {
                            colorResource(id = R.color.text_primary)
                        } else {
                            colorResource(id = R.color.text_tertiary)
                        }
                    )
                }
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
                                NewSpaceNameBlock(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            shape = RoundedCornerShape(16.dp),
                                            width = 0.5.dp,
                                            color = colorResource(id = R.color.shape_primary)
                                        )
                                        .padding(vertical = 12.dp, horizontal = 16.dp)
                                        .animateItem(),
                                    name = nameInput,
                                    onNameSet = { newName ->
                                        nameInput = newName
                                    },
                                    isEditEnabled = uiState.isEditEnabled
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
                                        .animateItem(),
                                    isEditEnabled = uiState.isEditEnabled,
                                    description = descriptionInput,
                                    onDescriptionSet = { newDescription ->
                                        descriptionInput = newDescription
                                    }
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

                        is UiSpaceSettingsItem.Chat -> TODO()
                        is UiSpaceSettingsItem.DefaultObjectType -> {
                            item {
                                DefaultTypeItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem(),
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
                                )
                            }
                        }

                        is UiSpaceSettingsItem.Members -> {
                            item {
                                MembersItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem(),
                                    item = item
                                )
                            }
                        }

                        UiSpaceSettingsItem.ObjectTypes -> {
                            item {
                                ObjectTypesItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                )
                            }
                        }

                        is UiSpaceSettingsItem.RemoteStorage -> TODO()
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
                                )
                            }
                        }

                        is UiSpaceSettingsItem.Wallpapers -> {
                            item {
                                WallpaperItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem(),
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

}