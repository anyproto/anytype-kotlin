package com.anytypeio.anytype.ui_settings.space

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.PERSONAL_SPACE_TYPE
import com.anytypeio.anytype.core_models.PRIVATE_SPACE_TYPE
import com.anytypeio.anytype.core_models.SHARED_SPACE_TYPE
import com.anytypeio.anytype.core_models.SpaceType
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Option
import com.anytypeio.anytype.core_ui.foundation.Section
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarning
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_utils.const.DateConst
import com.anytypeio.anytype.core_utils.ext.formatTimeInMillis
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel
import com.anytypeio.anytype.ui_settings.R
import com.anytypeio.anytype.ui_settings.main.SpaceHeader

@Composable
fun SpaceSettingsScreen(
    spaceData: ViewState<SpaceSettingsViewModel.SpaceData>,
    onNameSet: (String) -> Unit,
    onDeleteSpaceClicked: () -> Unit,
    onFileStorageClick: () -> Unit,
    onPersonalizationClicked: () -> Unit,
    onSpaceIdClicked: (Id) -> Unit,
    onNetworkIdClicked: (Id) -> Unit,
    onCreatedByClicked: (Id) -> Unit,
    onDebugClicked: () -> Unit,
    onRandomGradientClicked: () -> Unit,
    onSharePrivateSpaceClicked: () -> Unit,
    onManageSharedSpaceClicked: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .nestedScroll(rememberNestedScrollInteropConnection())
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            SpaceHeader(
                modifier = Modifier,
                name = when (spaceData) {
                    is ViewState.Success -> spaceData.data.name.ifEmpty {
                        stringResource(id = R.string.untitled)
                    }
                    else -> null
                },
                icon = when (spaceData) {
                    is ViewState.Success -> spaceData.data.icon
                    else -> null
                },
                onNameSet = onNameSet,
                onRandomGradientClicked = onRandomGradientClicked
            )
        }
        item { Divider() }
        item {
            if (spaceData is ViewState.Success) {
                if (spaceData.data.spaceType == PERSONAL_SPACE_TYPE) {
                    Section(title = stringResource(id = R.string.type))
                } else {
                    Section(title = stringResource(id = R.string.multiplayer_sharing))
                }
            } else {
                Section(title = EMPTY_STRING_VALUE)
            }
        }
        item {
            if (spaceData is ViewState.Success) {
                when(spaceData.data.spaceType) {
                    PERSONAL_SPACE_TYPE -> {
                        TypeOfSpace(spaceData.data.spaceType)
                    }
                    PRIVATE_SPACE_TYPE -> {
                        PrivateSpaceSharing(
                            onSharePrivateSpaceClicked = onSharePrivateSpaceClicked
                        )
                    }
                    SHARED_SPACE_TYPE -> {
                        SharedSpaceSharing(
                            onManageSharedSpaceClicked = onManageSharedSpaceClicked
                        )
                    }
                }
            }
        }
        item {
            Divider()
        }
        item {
            Section(title = stringResource(id = R.string.settings))
        }
        item {
            Option(image = R.drawable.ic_file_storage,
                text = stringResource(R.string.remote_storage),
                onClick = throttledClick(onFileStorageClick)
            )
        }
        item {
            Divider(paddingStart = 60.dp)
        }
        item {
            Option(image = R.drawable.ic_personalization,
                text = stringResource(R.string.personalization),
                onClick = throttledClick(onPersonalizationClicked)
            )
        }
        item {
            Divider(paddingStart = 60.dp)
        }
        item {
            Option(image = R.drawable.ic_debug,
                text = stringResource(R.string.debug),
                onClick = throttledClick(onDebugClicked)
            )
        }
        item {
            Divider(
                paddingStart = 60.dp
            )
        }
        item {
            Section(title = stringResource(id = R.string.space_info))
        }
        item {
            Box(
                modifier = Modifier
                    .height(92.dp)
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.space_id),
                    style = Title1,
                    modifier = Modifier.padding(top = 12.dp),
                    color = colorResource(id = R.color.text_primary)
                )
                if (spaceData is ViewState.Success) {
                    Text(
                        text = spaceData.data.spaceId ?: stringResource(id = R.string.unknown),
                        style = PreviewTitle2Regular,
                        maxLines = 2,
                        color = colorResource(id = R.color.text_primary),
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 12.dp, end = 50.dp)
                            .noRippleClickable {
                                onSpaceIdClicked(spaceData.data.spaceId.orEmpty())
                            }
                    )
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .height(92.dp)
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.created_by),
                    style = Title1,
                    modifier = Modifier.padding(top = 12.dp),
                    color = colorResource(id = R.color.text_primary)
                )
                if (spaceData is ViewState.Success) {
                    Text(
                        text = spaceData.data.createdBy ?: stringResource(id = R.string.unknown),
                        style = PreviewTitle2Regular,
                        maxLines = 2,
                        color = colorResource(id = R.color.text_primary),
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 12.dp, end = 50.dp)
                            .noRippleClickable {
                                onCreatedByClicked(spaceData.data.createdBy.orEmpty())
                            }
                    )
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .height(72.dp)
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.creation_date),
                    style = Title1,
                    modifier = Modifier.padding(top = 12.dp),
                    color = colorResource(id = R.color.text_primary)
                )
                if (spaceData is ViewState.Success) {
                    val formattedDate = spaceData.data.createdDateInMillis?.formatTimeInMillis(
                        DateConst.DEFAULT_DATE_FORMAT
                    ) ?: stringResource(id = R.string.unknown)
                    Text(
                        text = formattedDate,
                        style = PreviewTitle2Regular,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 12.dp, end = 50.dp),
                        maxLines = 1,
                        color = colorResource(id = R.color.text_primary)
                    )
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .height(92.dp)
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.network_id),
                    style = Title1,
                    modifier = Modifier.padding(top = 12.dp),
                    color = colorResource(id = R.color.text_primary)
                )
                if (spaceData is ViewState.Success) {
                    Text(
                        text = spaceData.data.network ?: stringResource(id = R.string.unknown),
                        style = PreviewTitle2Regular,
                        maxLines = 2,
                        color = colorResource(id = R.color.text_primary),
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 12.dp, end = 50.dp)
                            .noRippleClickable {
                                onNetworkIdClicked(spaceData.data.network.orEmpty())
                            }
                    )
                }
            }
        }
        if (spaceData is ViewState.Success && spaceData.data.isDeletable) {
            item {
                val label = when(spaceData.data.permissions) {
                    SpaceMemberPermissions.OWNER -> stringResource(R.string.delete_space)
                    else -> stringResource(R.string.multiplayer_leave_space)
                }
                Box(modifier = Modifier.height(78.dp)) {
                    ButtonWarning(
                        onClick = { onDeleteSpaceClicked() },
                        text = label,
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp, bottom = 10.dp)
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        size = ButtonSize.Large
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
@Preview
fun SpaceSettingsScreenPreview() {
    SpaceSettingsScreen(
        spaceData = ViewState.Success(
            data = SpaceSettingsViewModel.SpaceData(
                spaceId = "ID",
                createdDateInMillis = null,
                createdBy = "1235",
                network = "332311313131",
                name = "Dream team",
                icon = SpaceIconView.Placeholder,
                isDeletable = true,
                spaceType = PERSONAL_SPACE_TYPE,
                permissions = SpaceMemberPermissions.OWNER
            )
        ),
        onNameSet = {},
        onDeleteSpaceClicked = {},
        onFileStorageClick = {},
        onPersonalizationClicked = {},
        onSpaceIdClicked = {},
        onNetworkIdClicked = {} ,
        onCreatedByClicked = {},
        onDebugClicked = {},
        onRandomGradientClicked = {},
        onManageSharedSpaceClicked = {},
        onSharePrivateSpaceClicked = {}
    )
}

@Composable
fun PrivateSpaceSharing(
    onSharePrivateSpaceClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier
                .padding(start = 20.dp)
                .align(Alignment.CenterStart),
            text = stringResource(id = R.string.space_type_private_space),
            color = colorResource(id = R.color.text_primary),
            style = BodyRegular
        )
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .noRippleClickable(
                    onClick = throttledClick(
                        onClick = { onSharePrivateSpaceClicked() }
                    )
                )
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
}

@Composable
fun SharedSpaceSharing(
    onManageSharedSpaceClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
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
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .noRippleClickable(
                    onClick = throttledClick(
                        onClick = { onManageSharedSpaceClicked() }
                    )
                )
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = stringResource(id = R.string.multiplayer_manage),
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

@Preview
@Composable
fun PrivateSpaceSharingPreview() {
    PrivateSpaceSharing(
        onSharePrivateSpaceClicked = {}
    )
}

@Preview
@Composable
fun SharedSpaceSharingPreview() {
    SharedSpaceSharing(
        onManageSharedSpaceClicked = {}
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
                PERSONAL_SPACE_TYPE -> stringResource(id = R.string.space_type_personal)
                PRIVATE_SPACE_TYPE -> stringResource(id = R.string.space_type_private)
                SHARED_SPACE_TYPE -> stringResource(id = R.string.space_type_shared)
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