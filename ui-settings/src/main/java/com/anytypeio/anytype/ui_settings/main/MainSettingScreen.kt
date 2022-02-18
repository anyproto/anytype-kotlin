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
fun MainSettingScreen() {
    Column {
        Box(
            modifier = Modifier.padding(vertical = 6.dp).align(Alignment.CenterHorizontally)
        ) {
            Dragger()
        }
        Option(
            image = R.drawable.ic_key,
            text = stringResource(R.string.account_and_data)
        )
        Divider()
        Option(
            image = R.drawable.ic_key,
            text = stringResource(R.string.personalization)
        )
        Divider()
        Option(
            image = R.drawable.ic_key,
            text = stringResource(R.string.appearance)
        )
        Divider()
        Option(
            image = R.drawable.ic_key,
            text = stringResource(R.string.about)
        )
        Divider()
        Box(modifier = Modifier.height(16.dp))
    }
}