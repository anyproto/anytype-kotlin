package com.anytypeio.anytype.core_ui.common

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers

@Preview(
    backgroundColor = 0xFFFFFFFF,
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
    name = "Light Mode",
    showSystemUi = true
)
@Preview(
    backgroundColor = 0xFF121212,
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    name = "Dark Mode",
    showSystemUi = true,
    apiLevel = 35,
    wallpaper = Wallpapers.NONE
)
annotation class DefaultPreviews

@Preview(
    backgroundColor = 0xFFFFFFFF,
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO,
    name = "Light Mode",
    showSystemUi = true,
    device = Devices.NEXUS_5
)
annotation class OldDevicesPreview