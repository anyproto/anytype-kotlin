package com.anytypeio.anytype.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Header

import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyRegular

@Composable
fun DebugScreen(
    onExportAllClicked: () -> Unit,
    onReadAllChats: () -> Unit,
    onDebugStackGoroutines: () -> Unit = { /* Default no-op */ },
    onDebugStat: () -> Unit = { /* Default no-op */ },
    onDebugSpaceSummary: () -> Unit = { /* Default no-op */ },
    onDebugExportLog: () -> Unit = { /* Default no-op */ }
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(
                color = colorResource(R.color.background_secondary),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp
                )
            )

    ) {
        Dragger(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )
        Header(
            text = stringResource(R.string.debug)
        )

        Spacer(modifier = Modifier.height(10.dp))

        ActionItem(
            title = "Export work directory",
            onClick = onExportAllClicked
        )

        Divider()

        ActionItem(
            title = "Read all (chats)",
            onClick = onReadAllChats
        )

        Divider()

        ActionItem(
            title = "Debug stack Goroutines",
            onClick = onDebugStackGoroutines
        )

        Divider()

        ActionItem(
            title = "Debug Stat",
            onClick = onDebugStat
        )

        Divider()

        ActionItem(
            title = "Debug Space Summary",
            onClick = onDebugSpaceSummary
        )

        Divider()

        ActionItem(
            title = "Debug Export Log",
            onClick = onDebugExportLog
        )

        Divider()

    }
}

@Composable
private fun ActionItem(
    title: String,
    onClick: () -> Unit
) {
    Text(
        text = title,
        style = BodyRegular,
        color = colorResource(R.color.text_primary),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(20.dp)
    )
}

@DefaultPreviews
@Composable
fun DebugScreenPreview() {
    DebugScreen(
        onExportAllClicked = {},
        onReadAllChats = {}
    )
}