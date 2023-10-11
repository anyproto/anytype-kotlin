package com.anytypeio.anytype.ui_settings.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.foundation.Arrow
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Option
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.presentation.settings.MainSettingsViewModel
import com.anytypeio.anytype.ui_settings.R

@Composable
fun MainSettingScreen(
    workspace: MainSettingsViewModel.WorkspaceAndAccount,
    onSpaceIconClick: () -> Unit,
    onProfileClicked: () -> Unit,
    onAboutAppClicked: () -> Unit,
    onDebugClicked: () -> Unit,
    onPersonalizationClicked: () -> Unit,
    onAppearanceClicked: () -> Unit,
    onNameSet: (String) -> Unit,
    onFileStorageClick: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        SpaceHeader(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            workspace = workspace,
            onSpaceIconClick = onSpaceIconClick,
            onNameSet = onNameSet
        )
        Spacer(
            modifier = Modifier
                .height(10.dp)
                .padding(top = 4.dp)
        )
        Divider()
        Spacer(modifier = Modifier.height(26.dp))
        Settings(
            onProfileClicked = onProfileClicked,
            onPersonalizationClicked = onPersonalizationClicked,
            onAppearanceClicked = onAppearanceClicked,
            onAboutAppClicked = onAboutAppClicked,
            onDebugClicked = onDebugClicked,
            accountData = workspace,
            onFileStorageClick = onFileStorageClick
        )
        Box(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun Settings(
    onProfileClicked: () -> Unit,
    onPersonalizationClicked: () -> Unit,
    onAppearanceClicked: () -> Unit,
    onAboutAppClicked: () -> Unit,
    onDebugClicked: () -> Unit,
    accountData: MainSettingsViewModel.WorkspaceAndAccount,
    onFileStorageClick: () -> Unit
) {
    Section(
        modifier = Modifier.padding(start = 20.dp, bottom = 4.dp),
        title = stringResource(id = R.string.settings)
    )
    ProfileOption(
        data = (accountData as? MainSettingsViewModel.WorkspaceAndAccount.Account)?.profile,
        text = stringResource(R.string.profile),
        onClick = onProfileClicked
    )
    Divider(paddingStart = 60.dp)
    Option(
        image = R.drawable.ic_personalization,
        text = stringResource(R.string.personalization),
        onClick = onPersonalizationClicked
    )
    Divider(paddingStart = 60.dp)
    Option(
        image = R.drawable.ic_appearance,
        text = stringResource(R.string.appearance),
        onClick = onAppearanceClicked
    )
    Divider(paddingStart = 60.dp)
    Option(
        image = R.drawable.ic_file_storage,
        text = stringResource(R.string.file_storage),
        onClick = onFileStorageClick
    )
    Divider(paddingStart = 60.dp)
    Option(
        image = R.drawable.ic_debug,
        text = stringResource(R.string.space_debug),
        onClick = onDebugClicked
    )
    Divider(paddingStart = 60.dp)
    Option(
        image = R.drawable.ic_about,
        text = stringResource(R.string.about),
        onClick = onAboutAppClicked
    )
    Divider(paddingStart = 60.dp)
}

@Composable
private fun SpaceHeader(
    modifier: Modifier = Modifier,
    workspace: MainSettingsViewModel.WorkspaceAndAccount,
    onSpaceIconClick: () -> Unit,
    onNameSet: (String) -> Unit
) {
    when (workspace) {
        is MainSettingsViewModel.WorkspaceAndAccount.Account -> {
            Box(modifier = modifier.padding(vertical = 6.dp)) {
                Dragger()
            }
            Box(modifier = modifier.padding(top = 12.dp, bottom = 28.dp)) {
                SpaceNameBlock()
            }
            Box(modifier = modifier.padding(bottom = 16.dp)) {
                workspace.space?.icon?.let {
                    SpaceImageBlock(
                        icon = it,
                        onSpaceIconClick = onSpaceIconClick
                    )
                }
            }
            workspace.space?.name?.let {
                SpaceNameBlock(
                    name = it,
                    onNameSet = onNameSet
                )
            }
        }
        is MainSettingsViewModel.WorkspaceAndAccount.Idle -> {}
    }
}

@Composable
fun ProfileOption(
    data: MainSettingsViewModel.WorkspaceAndAccount.ProfileData?,
    text: String,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(52.dp)
            .clickable(onClick = onClick)

    ) {
        data?.let {
            when (val icon = it.icon) {
                is ProfileIconView.Image -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = icon.url,
                            error = painterResource(id = R.drawable.ic_home_widget_space)
                        ),
                        contentDescription = "Custom image profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .size(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }
                is ProfileIconView.Gradient -> {
                    val gradient = Brush.radialGradient(
                        colors = listOf(
                            Color(icon.from.toColorInt()),
                            Color(icon.to.toColorInt())
                        )
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .size(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(gradient)
                    )
                }
                else -> {
                    val nameFirstChar = if (data.name.isEmpty()) {
                        stringResource(id = R.string.account_default_name)
                    } else {
                        data.name.first().uppercaseChar().toString()
                    }
                    Box(
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .size(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colorResource(id = R.color.shape_primary))
                    ) {
                        Text(
                            text = nameFirstChar,
                            style = MaterialTheme.typography.h3.copy(
                                color = colorResource(id = R.color.text_white),
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                }
            }
        } ?: kotlin.run {
            Image(
                painterResource(R.drawable.ic_account_and_data),
                contentDescription = "Option icon",
                modifier = Modifier.padding(
                    start = 20.dp
                )
            )
        }

        Text(
            text = text,
            color = colorResource(R.color.text_primary),
            modifier = Modifier.padding(
                start = 12.dp
            ),
            style = BodyRegular
        )
        Box(
            modifier = Modifier.weight(1.0f, true),
            contentAlignment = Alignment.CenterEnd
        ) {
            Arrow()
        }
    }
}

@Composable
fun GradientComposeView(
    modifier: Modifier,
    from: String,
    to: String,
    size: Dp
) {
    val gradient = Brush.radialGradient(
        colors = listOf(
            Color(from.toColorInt()),
            Color(to.toColorInt())
        )
    )
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(gradient)
    )
}