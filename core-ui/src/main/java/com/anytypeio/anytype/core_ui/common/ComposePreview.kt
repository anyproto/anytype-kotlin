package com.anytypeio.anytype.core_ui.common

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    backgroundColor = 0xFFFFFFFF,
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO,
    name = "Light Mode",
    apiLevel = 34,
    showSystemUi = true
)
@Preview(
    backgroundColor = 0xFF121212,
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    apiLevel = 34,
    showSystemUi = true
)
annotation class DefaultPreviews