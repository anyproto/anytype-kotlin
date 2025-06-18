package com.anytypeio.anytype.core_ui.features.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize

@Composable
fun AttachToChatToolbar(
    onAttachClicked: () -> Unit = {},
    onCancelClicked: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(R.color.background_primary)
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ){
        ButtonPrimary(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.cancel),
            onClick = onCancelClicked,
            size = ButtonSize.Large
        )
        Spacer(modifier = Modifier.width(8.dp))
        ButtonPrimary(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.chat_attach),
            onClick = onAttachClicked,
            size = ButtonSize.Large
        )
    }
}

@DefaultPreviews
@Composable
fun AttachToChatToolbarPreview() {
    AttachToChatToolbar()
}
