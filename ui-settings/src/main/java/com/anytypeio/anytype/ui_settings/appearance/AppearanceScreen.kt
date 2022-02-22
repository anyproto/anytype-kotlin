package com.anytypeio.anytype.ui_settings.appearance

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
import com.anytypeio.anytype.core_ui.foundation.Toolbar
import com.anytypeio.anytype.ui_settings.R

@Composable
fun AppearanceScreen(
    onWallpaperClicked: () -> Unit
) {
    Column {
        Box(Modifier.padding(vertical = 6.dp).align(Alignment.CenterHorizontally)) {
            Dragger()
        }
        Toolbar(stringResource(R.string.appearance))
        Option(
            image = R.drawable.ic_wallpaper,
            text = stringResource(R.string.wallpaper),
            onClick = onWallpaperClicked
        )
        Divider(paddingStart = 60.dp)
        Box(Modifier.height(54.dp))
    }
}