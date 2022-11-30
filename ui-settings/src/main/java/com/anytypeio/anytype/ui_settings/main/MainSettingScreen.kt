package com.anytypeio.anytype.ui_settings.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.anytypeio.anytype.ui_settings.R

@Composable
fun MainSettingScreen(
    onAccountAndDataClicked: () -> Unit,
    onAboutAppClicked: () -> Unit,
    onDebugClicked: () -> Unit,
    onPersonalizationClicked: () -> Unit,
    onAppearanceClicked: () -> Unit,
    showDebugMenu: Boolean
) {
    Column {
        Box(
            modifier = Modifier.padding(vertical = 6.dp).align(Alignment.CenterHorizontally)
        ) {
            Dragger()
        }
        Option(
            image = R.drawable.ic_account_and_data,
            text = stringResource(R.string.account_and_data),
            onClick = onAccountAndDataClicked
        )
        Divider()
        Option(
            image = R.drawable.ic_personalization,
            text = stringResource(R.string.personalization),
            onClick = onPersonalizationClicked
        )
        Divider()
        Option(
            image = R.drawable.ic_appearance,
            text = stringResource(R.string.appearance),
            onClick = onAppearanceClicked
        )
        Divider()
        Option(
            image = R.drawable.ic_about,
            text = stringResource(R.string.about),
            onClick = onAboutAppClicked
        )
        Divider()
        if (showDebugMenu) {
            Option(
                image = R.drawable.ic_debug,
                text = stringResource(R.string.debug),
                onClick = onDebugClicked
            )
            Divider()
        }
        Box(modifier = Modifier.height(16.dp))
    }
}