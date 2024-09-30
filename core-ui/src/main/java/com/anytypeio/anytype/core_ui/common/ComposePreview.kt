package com.anytypeio.anytype.core_ui.common

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    backgroundColor = 0xFFFFFFFF,
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO,
    name = "Light Mode"
)
@Preview(
    backgroundColor = 0x000000,
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
annotation class DefaultPreviews