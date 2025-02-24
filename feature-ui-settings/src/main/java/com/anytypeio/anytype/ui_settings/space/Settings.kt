package com.anytypeio.anytype.ui_settings.space

import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.DEFAULT_SPACE_TYPE
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.PRIVATE_SPACE_TYPE
import com.anytypeio.anytype.core_models.SHARED_SPACE_TYPE
import com.anytypeio.anytype.core_models.SpaceType
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.Section
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonUpgrade
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsItem
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsState
import com.anytypeio.anytype.ui_settings.R
import com.anytypeio.anytype.ui_settings.main.SpaceDescriptionBlock
import com.anytypeio.anytype.ui_settings.main.SpaceIcon
import com.anytypeio.anytype.ui_settings.main.SpaceNameBlock

@Composable
fun SpaceSettingsContainer(
    uiState: UiSpaceSettingsState,
    onSaveClicked: (String, String) -> Unit,
    onDeleteSpaceClicked: () -> Unit,
    onFileStorageClick: () -> Unit,
    onPersonalizationClicked: () -> Unit,
    onSpaceIdClicked: (Id) -> Unit,
    onNetworkIdClicked: (Id) -> Unit,
    onCreatedByClicked: (Id) -> Unit,
    onDebugClicked: () -> Unit,
    onRemoveIconClicked: () -> Unit,
    onSharePrivateSpaceClicked: () -> Unit,
    onManageSharedSpaceClicked: () -> Unit,
    onAddMoreSpacesClicked: () -> Unit,
    onSpaceImagePicked: (Uri) -> Unit
) {
    if (uiState is UiSpaceSettingsState.SpaceSettings) {
        SpaceSettingsScreen(
            uiState = uiState,
            onSaveClicked = onSaveClicked,
            onDeleteSpaceClicked = onDeleteSpaceClicked,
            onFileStorageClick = onFileStorageClick,
            onPersonalizationClicked = onPersonalizationClicked,
            onSpaceIdClicked = onSpaceIdClicked,
            onNetworkIdClicked = onNetworkIdClicked,
            onCreatedByClicked = onCreatedByClicked,
            onDebugClicked = onDebugClicked,
            onRemoveIconClicked = onRemoveIconClicked,
            onSharePrivateSpaceClicked = onSharePrivateSpaceClicked,
            onManageSharedSpaceClicked = onManageSharedSpaceClicked,
            onAddMoreSpacesClicked = onAddMoreSpacesClicked,
            onSpaceImagePicked = onSpaceImagePicked
        )
    }
}

@Composable
fun SpaceSettingsScreen(
    uiState: UiSpaceSettingsState.SpaceSettings,
    onSaveClicked: (String, String) -> Unit,
    onDeleteSpaceClicked: () -> Unit,
    onFileStorageClick: () -> Unit,
    onPersonalizationClicked: () -> Unit,
    onSpaceIdClicked: (Id) -> Unit,
    onNetworkIdClicked: (Id) -> Unit,
    onCreatedByClicked: (Id) -> Unit,
    onDebugClicked: () -> Unit,
    onRemoveIconClicked: () -> Unit,
    onSharePrivateSpaceClicked: () -> Unit,
    onManageSharedSpaceClicked: () -> Unit,
    onAddMoreSpacesClicked: () -> Unit,
    onSpaceImagePicked: (Uri) -> Unit
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

                        }
                )
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .clickable(enabled = isDirty) {
                            // Call onSaveClicked with the updated values.
                            onSaveClicked(nameInput, descriptionInput)
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
                                SpaceIcon(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem(),
                                    icon = item.icon,
                                    onRemoveIconClicked = onRemoveIconClicked,
                                    onSpaceImagePicked = onSpaceImagePicked,
                                    isEditEnabled = uiState.isEditEnabled
                                )
                            }
                        }

                        is UiSpaceSettingsItem.Name -> {
                            item {
                                SpaceNameBlock(
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
                                SpaceDescriptionBlock(
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
                                    onInviteClicked = {},
                                    onQRCodeClicked = {}
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


//        item {
//            SpaceIcon(
//                modifier = Modifier,
//                icon = state.icon,
//                onRemoveIconClicked = onRemoveIconClicked,
//                onSpaceImagePicked = onSpaceImagePicked,
//                isEditEnabled = state.isEditEnabled
//            )
//        }
//        item {
//            SpaceNameBlock(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .border(
//                        shape = RoundedCornerShape(16.dp),
//                        width = 0.5.dp,
//                        color = colorResource(id = R.color.shape_primary)
//                    )
//                    .padding(vertical = 12.dp, horizontal = 16.dp),
//                name = state.name,
//                onNameSet = onNameSet,
//                isEditEnabled = state.isEditEnabled
//            )
//        }
//        item {
//            Spacer(modifier = Modifier.height(12.dp))
//            SpaceDescriptionBlock(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .border(
//                        shape = RoundedCornerShape(16.dp),
//                        width = 0.5.dp,
//                        color = colorResource(id = R.color.shape_primary)
//                    )
//                    .padding(vertical = 12.dp, horizontal = 16.dp),
//                isEditEnabled = state.isEditEnabled,
//                description = state.description,
//                onDescriptionSet = {},
//            )
//        }
//        item {
//            Divider()
//        }
//        item {
//            Section(title = stringResource(id = R.string.multiplayer_space_type))
//        }
//        item {
//            when (state.spaceType) {
//                DEFAULT_SPACE_TYPE -> {
//                    TypeOfSpace(state.spaceType)
//                }
//
//                PRIVATE_SPACE_TYPE -> {
//                    PrivateSpaceSharing(
//                        onSharePrivateSpaceClicked = onSharePrivateSpaceClicked,
//                        shareLimitStateState = state.shareLimitReached,
//                        onAddMoreSpacesClicked = onAddMoreSpacesClicked
//                    )
//                }
//
//                SHARED_SPACE_TYPE -> {
//                    SharedSpaceSharing(
//                        onManageSharedSpaceClicked = onManageSharedSpaceClicked,
//                        isUserOwner = state.isUserOwner,
//                        requests = state.requests
//                    )
//                }
//            }
//        }
//        item {
//            Divider()
//        }
//        item {
//            Section(title = stringResource(id = R.string.settings))
//        }
//        if (state.isUserOwner) {
//            item {
//                Option(
//                    image = R.drawable.ic_file_storage,
//                    text = stringResource(R.string.remote_storage),
//                    onClick = throttledClick(onFileStorageClick)
//                )
//            }
//            item {
//                Divider(paddingStart = 60.dp)
//            }
//        }
//        item {
//            Option(
//                image = R.drawable.ic_personalization,
//                text = stringResource(R.string.personalization),
//                onClick = throttledClick(onPersonalizationClicked)
//            )
//        }
//        item {
//            Divider(paddingStart = 60.dp)
//        }
//        item {
//            Option(
//                image = R.drawable.ic_debug,
//                text = stringResource(R.string.debug),
//                onClick = throttledClick(onDebugClicked)
//            )
//        }
//        item {
//            Divider(
//                paddingStart = 60.dp
//            )
//        }
//        item {
//            Section(title = stringResource(id = R.string.space_info))
//        }
//        item {
//            SettingsItem(
//                title = stringResource(id = R.string.space_id),
//                value = state.spaceId.orEmpty().ifEmpty {
//                    stringResource(id = R.string.unknown)
//                },
//                onClick = { onSpaceIdClicked(it) }
//            )
//        }
//        item {
//            SettingsItem(
//                title = stringResource(id = R.string.network_id),
//                value = state.network.orEmpty().ifEmpty {
//                    stringResource(id = R.string.unknown)
//                },
//                onClick = { onNetworkIdClicked(it) }
//            )
//        }
//        item {
//            SettingsItem(
//                title = stringResource(id = R.string.created_by),
//                value = state.createdBy.orEmpty().ifEmpty {
//                    stringResource(id = R.string.unknown)
//                },
//                onClick = { onCreatedByClicked(it) }
//            )
//        }
//        item {
//            SettingsItem(
//                title = stringResource(id = R.string.creation_date),
//                value = state.createdDateInMillis?.formatTimeInMillis(
//                    DateConst.DEFAULT_DATE_FORMAT
//                ) ?: stringResource(id = R.string.unknown),
//                onClick = {},
//                showIcon = false
//            )
//        }
//        if (state.isDeletable) {
//            item {
//                val label = if (state.isUserOwner) {
//                    stringResource(R.string.delete_space)
//                } else {
//                    stringResource(R.string.multiplayer_leave_space)
//                }
//                Box(modifier = Modifier.height(78.dp)) {
//                    ButtonWarning(
//                        onClick = { onDeleteSpaceClicked() },
//                        text = label,
//                        modifier = Modifier
//                            .padding(start = 20.dp, end = 20.dp, bottom = 10.dp)
//                            .fillMaxWidth()
//                            .align(Alignment.BottomCenter),
//                        size = ButtonSize.Large
//                    )
//                }
//            }
//        }
//        item {
//            Spacer(modifier = Modifier.height(16.dp))
//        }
            }
        }
    )

}

@Composable
private fun SettingsItem(
    title: String,
    value: String?,
    onClick: (String) -> Unit,
    showIcon: Boolean = true
) {
    Column(
        modifier = Modifier
            .height(90.dp)
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .noRippleClickable {
                if (showIcon) onClick(value.orEmpty())
            }
    ) {
        Text(
            text = title,
            style = BodyCalloutMedium,
            modifier = Modifier.padding(top = 12.dp),
            color = colorResource(id = R.color.text_secondary)
        )
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .wrapContentHeight()
                    .weight(1.0f, true)
                    .padding(top = 4.dp, end = 27.dp),
                text = value ?: stringResource(id = R.string.unknown),
                style = PreviewTitle2Regular,
                maxLines = 2,
                minLines = 2,
                color = colorResource(id = R.color.text_primary),
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )
            if (showIcon) {
                Image(
                    painterResource(id = R.drawable.ic_copy_24),
                    contentDescription = "Option icon",
                )
            }
        }
    }
}

@Composable
private fun MultiplayerButtons(
    modifier: Modifier = Modifier,
    onInviteClicked: () -> Unit = {},
    onQRCodeClicked: () -> Unit = {}
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .border(
                    shape = RoundedCornerShape(16.dp),
                    width = 0.5.dp,
                    color = colorResource(id = R.color.shape_primary)
                )
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(32.dp),
                painter = painterResource(id = R.drawable.ic_add_member_32),
                contentDescription = "Invite new member icon"
            )
            Text(
                modifier = Modifier.wrapContentSize(),
                text = stringResource(id = R.string.space_settings_invite),
                style = Caption1Regular,
                color = colorResource(id = R.color.text_primary)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .border(
                    shape = RoundedCornerShape(16.dp),
                    width = 0.5.dp,
                    color = colorResource(id = R.color.shape_primary)
                )
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(32.dp),
                painter = painterResource(id = R.drawable.ic_qr_code_32),
                contentDescription = "Share QR code icon"
            )
            Text(
                modifier = Modifier.wrapContentSize(),
                text = stringResource(id = R.string.space_settings_qrcode),
                style = Caption1Regular,
                color = colorResource(id = R.color.text_primary)
            )
        }
    }
}

@Composable
@DefaultPreviews
fun SpaceSettingsScreenPreview() {
    SpaceSettingsScreen(
//        state = SpaceSettingsViewModel.SpaceData.Success(
//            spaceId = "IDdflkdsl;kfldsklfkdslakfl;sdkalfkldskfl;dskal;fklflsdkl;fkdsl;akfl;dskal;fks",
//            createdDateInMillis = null,
//            createdBy = "1235",
//            network = "332311313131flsdklfksdlkfksdlkfksdlkflasd324213432432",
//            name = "Dream team",
//            description = "This is a dream team space",
//            icon = SpaceIconView.Placeholder(),
//            isDeletable = true,
//            spaceType = DEFAULT_SPACE_TYPE,
//            isUserOwner = true,
//            isEditEnabled = true,
//            shareLimitReached = SpaceSettingsViewModel.ShareLimitsState(
//                shareLimitReached = false,
//                sharedSpacesLimit = 0
//            )
//        ),
        uiState = UiSpaceSettingsState.SpaceSettings(
            items = listOf(
                UiSpaceSettingsItem.Spacer(height = 8),
                UiSpaceSettingsItem.Icon(SpaceIconView.Placeholder()),
                UiSpaceSettingsItem.Spacer(height = 16),
                UiSpaceSettingsItem.Name("Dream team"),
                UiSpaceSettingsItem.Spacer(height = 12),
                UiSpaceSettingsItem.Description("This is a dream team space"),
                UiSpaceSettingsItem.Spacer(height = 12),
                UiSpaceSettingsItem.Multiplayer,
                UiSpaceSettingsItem.Spacer(height = 8),
                UiSpaceSettingsItem.Section.Collaboration,
                UiSpaceSettingsItem.Members(5),
                UiSpaceSettingsItem.Section.ContentModel,
                UiSpaceSettingsItem.ObjectTypes,
                UiSpaceSettingsItem.Section.Preferences,
                UiSpaceSettingsItem.DefaultObjectType(
                    name = "Taskwithveryverlylongname",
                    icon = ObjectIcon.Empty.ObjectType
                ),
                UiSpaceSettingsItem.Spacer(height = 8),
                UiSpaceSettingsItem.Wallpapers(color = ThemeColor.TEAL),
                UiSpaceSettingsItem.Spacer(height = 8),
                UiSpaceSettingsItem.Section.DataManagement,
                UiSpaceSettingsItem.Spacer(height = 8),
                UiSpaceSettingsItem.Section.Misc,
                UiSpaceSettingsItem.SpaceInfo,
                UiSpaceSettingsItem.Spacer(height = 8),
                UiSpaceSettingsItem.DeleteSpace,
                UiSpaceSettingsItem.Spacer(height = 32),

                ),
            isEditEnabled = true
        ),
        onDeleteSpaceClicked = {},
        onFileStorageClick = {},
        onPersonalizationClicked = {},
        onSpaceIdClicked = {},
        onNetworkIdClicked = {},
        onCreatedByClicked = {},
        onDebugClicked = {},
        onRemoveIconClicked = {},
        onManageSharedSpaceClicked = {},
        onSharePrivateSpaceClicked = {},
        onAddMoreSpacesClicked = {},
        onSpaceImagePicked = {},
        onSaveClicked = { _, _ ->}
    )
}

@Composable
fun PrivateSpaceSharing(
    onSharePrivateSpaceClicked: () -> Unit,
    onAddMoreSpacesClicked: () -> Unit,
    shareLimitStateState: SpaceSettingsViewModel.ShareLimitsState
) {
    Column {
        Box(
            modifier = Modifier
                .height(52.dp)
                .fillMaxWidth()
                .noRippleClickable(
                    onClick = throttledClick(
                        onClick = { onSharePrivateSpaceClicked() }
                    )
                )
        ) {
            Text(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .align(Alignment.CenterStart),
                text = stringResource(id = R.string.space_type_private_space),
                color = if (shareLimitStateState.shareLimitReached)
                    colorResource(id = R.color.text_secondary)
                else
                    colorResource(id = R.color.text_primary),
                style = BodyRegular
            )
            Row(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = stringResource(id = R.string.multiplayer_share),
                    color = colorResource(id = R.color.text_secondary),
                    style = BodyRegular
                )
                Spacer(Modifier.width(10.dp))
                Image(
                    painter = painterResource(R.drawable.ic_arrow_forward),
                    contentDescription = "Arrow forward",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 20.dp)
                )
            }
        }
        if (shareLimitStateState.shareLimitReached) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp),
                text = stringResource(
                    id = R.string.membership_space_settings_share_limit,
                    shareLimitStateState.sharedSpacesLimit
                ),
                color = colorResource(id = R.color.text_primary),
                style = Caption1Regular
            )
            ButtonUpgrade(
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp)
                    .height(32.dp),
                onClick = { onAddMoreSpacesClicked() },
                text = stringResource(id = R.string.multiplayer_upgrade_spaces_button)
            )
        }
    }
}

@Composable
fun SharedSpaceSharing(
    onManageSharedSpaceClicked: () -> Unit,
    isUserOwner: Boolean,
    requests: Int = 0
) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
            .noRippleClickable(
                onClick = throttledClick(
                    onClick = { onManageSharedSpaceClicked() }
                )
            )
    ) {
        Text(
            modifier = Modifier
                .padding(start = 20.dp)
                .align(Alignment.CenterStart),
            text = stringResource(id = R.string.space_type_shared_space),
            color = colorResource(id = R.color.text_primary),
            style = BodyRegular
        )
        Row(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = if (isUserOwner) {
                    if (requests > 0) {
                        pluralStringResource(
                            R.plurals.multiplayer_number_of_join_requests,
                            requests,
                            requests,
                            requests
                        )
                    } else {
                        stringResource(id = R.string.multiplayer_manage)
                    }
                } else {
                    stringResource(id = R.string.multiplayer_members)
                },
                color = colorResource(id = R.color.text_secondary),
                style = BodyRegular
            )
            Spacer(Modifier.width(10.dp))
            Image(
                painter = painterResource(R.drawable.ic_arrow_forward),
                contentDescription = "Arrow forward",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 20.dp)
            )
        }
    }
}

@Composable
private fun SpaceSettingsSection(
    modifier: Modifier = Modifier,
    item: UiSpaceSettingsItem.Section
) {
    val text = when (item) {
        UiSpaceSettingsItem.Section.Collaboration ->
            stringResource(id = R.string.space_settings_section_collaboration)
        UiSpaceSettingsItem.Section.ContentModel ->
            stringResource(id = R.string.space_settings_section_content_model)
        UiSpaceSettingsItem.Section.DataManagement ->
            stringResource(id = R.string.space_settings_section_data_management)
        UiSpaceSettingsItem.Section.Misc ->
            stringResource(id = R.string.space_settings_section_misc)
        UiSpaceSettingsItem.Section.Preferences ->
            stringResource(id = R.string.space_settings_section_preferences)
    }
    Section(
        modifier = modifier,
        title = text,
        textPaddingStart = 0.dp
    )
}

@Composable
fun TypeOfSpace(spaceType: SpaceType?) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
    ) {
        Image(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp),
            painter = painterResource(id = R.drawable.ic_space_type_private),
            contentDescription = "Private space icon"
        )
        if (spaceType != null) {
            val spaceTypeName = when (spaceType) {
                DEFAULT_SPACE_TYPE -> stringResource(id = R.string.space_type_default_space)
                PRIVATE_SPACE_TYPE -> stringResource(id = R.string.space_type_private_space)
                SHARED_SPACE_TYPE -> stringResource(id = R.string.space_type_shared_space)
                else -> stringResource(id = R.string.space_type_unknown)
            }
            Text(
                modifier = Modifier
                    .padding(start = 42.dp)
                    .align(Alignment.CenterStart),
                text = spaceTypeName,
                color = colorResource(id = R.color.text_primary),
                style = BodyRegular
            )
        }
    }
}

@Preview
@Composable
private fun PrivateSpaceSharingPreview() {
    PrivateSpaceSharing(
        onSharePrivateSpaceClicked = {},
        shareLimitStateState = SpaceSettingsViewModel.ShareLimitsState(
            shareLimitReached = true,
            sharedSpacesLimit = 5
        ),
        onAddMoreSpacesClicked = {}
    )
}

@Preview
@Composable
private fun SharedSpaceSharingPreview() {
    SharedSpaceSharing(
        onManageSharedSpaceClicked = {},
        isUserOwner = true
    )
}
