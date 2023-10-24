package com.anytypeio.anytype.ui_settings.space

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Option
import com.anytypeio.anytype.presentation.settings.MainSettingsViewModel
import com.anytypeio.anytype.ui_settings.R
import com.anytypeio.anytype.ui_settings.main.ProfileOption
import com.anytypeio.anytype.ui_settings.main.Section

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
        title = stringResource(id = R.string.space_settings)
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