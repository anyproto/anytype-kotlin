package com.anytypeio.anytype.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.presentation.settings.DebugViewModel.ProfilerState
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
    onDebugExportLog: () -> Unit = { /* Default no-op */ },
    isProfilerOnStartupEnabled: Boolean = false,
    onProfilerOnStartupToggled: (Boolean) -> Unit = { /* Default no-op */ },
    profilerState: ProfilerState = ProfilerState.Idle,
    profilerDurationSeconds: Int = 60,
    onRunProfilerNowClicked: () -> Unit = { /* Default no-op */ },
    onShareProfilerResultClicked: () -> Unit = { /* Default no-op */ }
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

        ToggleItem(
            title = "Run profiler on startup",
            isEnabled = isProfilerOnStartupEnabled,
            onToggle = onProfilerOnStartupToggled
        )

        Divider()

        // Profiler immediate run section
        when (profilerState) {
            is ProfilerState.Idle -> {
                ActionItem(
                    title = "Run profiler now (${profilerDurationSeconds}s)",
                    onClick = onRunProfilerNowClicked
                )
            }
            is ProfilerState.Running -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colorResource(R.color.palette_system_amber_125)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Profiling in progress...",
                        style = BodyRegular,
                        color = colorResource(R.color.text_primary)
                    )
                }
            }
            is ProfilerState.Completed -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Profiler completed!",
                        style = BodyRegular,
                        color = colorResource(R.color.palette_system_green)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = profilerState.filePath,
                        style = BodyRegular,
                        color = colorResource(R.color.text_secondary),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ActionItem(
                        title = "Share profiler result",
                        onClick = onShareProfilerResultClicked
                    )
                }
            }
            is ProfilerState.Error -> {
                Text(
                    text = "Error: ${profilerState.message}",
                    style = BodyRegular,
                    color = colorResource(R.color.palette_system_red),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                )
            }
        }

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

@Composable
private fun ToggleItem(
    title: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isEnabled) }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = BodyRegular,
            color = colorResource(R.color.text_primary),
            modifier = Modifier.weight(1f)
        )
        Image(
            painter = painterResource(
                if (isEnabled) R.drawable.ic_data_view_grid_checkbox_checked
                else R.drawable.ic_data_view_grid_checkbox
            ),
            contentDescription = null
        )
    }
}

@DefaultPreviews
@Composable
fun DebugScreenPreview() {
    DebugScreen(
        onExportAllClicked = {},
        onReadAllChats = {}
    )
}