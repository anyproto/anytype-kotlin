package com.anytypeio.anytype.feature_allcontent.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.feature_allcontent.R

@Composable
fun AllContentTitle() {
    Text(text = "All Content",
        style = Title2,
        color = colorResource(id = R.color.text_primary)
    )
}