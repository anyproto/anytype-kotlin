package com.anytypeio.anytype.ui.media.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierInfo
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews


@Composable
fun MediaActionToolbar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onOpenClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .background(
                color = colorResource(id = R.color.home_screen_toolbar_button),
                shape = RoundedCornerShape(16.dp)
            )
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Image(
            painter = painterResource(R.drawable.ic_nav_panel_back),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 20.dp)
                .clickable { onBackClick() }
        )

        Image(
            modifier = Modifier.clickable { onDownloadClick() },
            painter = painterResource(R.drawable.ic_object_action_download),
            contentDescription = null
        )

        Image(
            modifier = Modifier.clickable { onOpenClick() },
            painter = painterResource(R.drawable.ic_open_as_object),
            contentDescription = null
        )

        Image(
            painter = painterResource(R.drawable.icon_delete_red),
            contentDescription = null,
            modifier = Modifier
                .padding(end = 20.dp)
                .clickable { onDeleteClick() }
        )
    }
}

@DefaultPreviews
@Composable
fun MediaActionToolbarPreview() {
    MediaActionToolbar()
}

