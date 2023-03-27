package com.anytypeio.anytype.ui_settings.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Option
import com.anytypeio.anytype.presentation.settings.MainSettingsViewModel
import com.anytypeio.anytype.ui_settings.R

@Composable
fun MainSettingScreen(
    workspace: MainSettingsViewModel.WorkspaceData,
    onSpaceIconClick: () -> Unit,
    onAccountAndDataClicked: () -> Unit,
    onAboutAppClicked: () -> Unit,
    onDebugClicked: () -> Unit,
    onPersonalizationClicked: () -> Unit,
    onAppearanceClicked: () -> Unit,
    onNameSet: (String) -> Unit,
    showDebugMenu: Boolean
) {
    Column(Modifier.fillMaxSize()) {
        Header(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            workspace = workspace,
            onSpaceIconClick = onSpaceIconClick,
            onNameSet = onNameSet
        )
        Spacer(modifier = Modifier.height(10.dp).padding(top = 4.dp))
        Divider()
        Spacer(modifier = Modifier.height(26.dp))
        Settings(
            onAccountAndDataClicked = onAccountAndDataClicked,
            onPersonalizationClicked = onPersonalizationClicked,
            onAppearanceClicked = onAppearanceClicked,
            onAboutAppClicked = onAboutAppClicked,
            showDebugMenu = showDebugMenu,
            onDebugClicked = onDebugClicked
        )
        Box(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun Settings(
    onAccountAndDataClicked: () -> Unit,
    onPersonalizationClicked: () -> Unit,
    onAppearanceClicked: () -> Unit,
    onAboutAppClicked: () -> Unit,
    showDebugMenu: Boolean,
    onDebugClicked: () -> Unit
) {
    Section(
        modifier = Modifier.padding(start = 20.dp, bottom = 4.dp),
        title = "Settings"
    )
    Option(
        image = R.drawable.ic_account_and_data,
        text = stringResource(R.string.account_and_data),
        onClick = onAccountAndDataClicked
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
        image = R.drawable.ic_about,
        text = stringResource(R.string.about),
        onClick = onAboutAppClicked
    )
    Divider(paddingStart = 60.dp)
    if (showDebugMenu) {
        Option(
            image = R.drawable.ic_debug,
            text = stringResource(R.string.debug),
            onClick = onDebugClicked
        )
        Divider(paddingStart = 60.dp)
    }
}

@Composable
private fun Header(
    modifier: Modifier = Modifier,
    workspace: MainSettingsViewModel.WorkspaceData,
    onSpaceIconClick: () -> Unit,
    onNameSet: (String) -> Unit
) {
    when (workspace) {
        is MainSettingsViewModel.WorkspaceData.Data -> {
            Box(modifier = modifier.padding(vertical = 6.dp)) {
                Dragger()
            }
            Box(modifier = modifier.padding(top = 12.dp, bottom = 28.dp)) {
                SpaceNameBlock()
            }
            Box(modifier = modifier.padding(bottom = 16.dp)) {
                SpaceImageBlock(
                    icon = workspace.icon,
                    onSpaceIconClick = onSpaceIconClick
                )
            }
            NameBlock(name = workspace.name, onNameSet = onNameSet)
        }
        is MainSettingsViewModel.WorkspaceData.Idle -> {}
    }
}