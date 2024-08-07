package com.anytypeio.anytype.core_ui.features.history

import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.editor.BlockAdapter
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.history.VersionHistoryPreviewScreen
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionHistoryPreviewScreen(
    state: VersionHistoryPreviewScreen,
    onDismiss: () -> Unit,
    onRestore: () -> Unit,
    editorAdapter: BlockAdapter
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val versionTitle = remember { mutableStateOf("") }
    val versionIcon = remember { mutableStateOf<ObjectIcon?>(null) }
    val scrollState = rememberScrollState()
    val nestedScrollInteropConnection = rememberNestedScrollInteropConnection()
    if (state is VersionHistoryPreviewScreen.Success) {
        versionTitle.value = "${state.dateFormatted}, ${state.timeFormatted}"
        versionIcon.value = state.icon
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .nestedScroll(nestedScrollInteropConnection),
            ) {
                Dragger(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 6.dp)
                )
                Header(title = versionTitle.value, icon = versionIcon.value)
                AndroidView(
                    factory = { context ->
                        LayoutInflater.from(context)
                            .inflate(R.layout.version_history_screen, null).apply {
                                findViewById<RecyclerView>(R.id.recycler).apply {
                                    adapter = editorAdapter
                                }
                            }
                    },
                    update = {
                        editorAdapter.updateWithDiffUtil(state.blocks)
                    }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .border(
                        border = BorderStroke(
                            0.5.dp,
                            color = colorResource(id = R.color.shape_primary)
                        ),
                    )
                    .background(
                        color = colorResource(id = R.color.background_primary),
                    )
                    .padding(horizontal = 24.dp)
                    .align(Alignment.BottomCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ButtonSecondary(
                    text = stringResource(id = R.string.cancel),
                    onClick = onDismiss,
                    size = ButtonSize.LargeSecondary,
                    modifier = Modifier.weight(1.0f)
                )
                Spacer(modifier = Modifier.width(9.dp))
                ButtonPrimary(
                    text = stringResource(id = R.string.restore),
                    onClick = onRestore,
                    size = ButtonSize.Large,
                    modifier = Modifier.weight(1.0f)
                )
            }
        }
    }
}

@Composable
private fun Header(title: String, icon: ObjectIcon?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center),
            text = title,
            style = PreviewTitle2Medium,
            color = colorResource(id = R.color.text_primary)
        )
        if (icon != null) {
            ListWidgetObjectIcon(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(24.dp)
                    .align(Alignment.CenterEnd),
                icon = icon,
                iconSize = 24.dp,
                avatarFontSize = 16.sp,
                avatarBackgroundColor = R.color.shape_tertiary,
                avatarTextStyle = VersionHistoryAvatarTextStyle()
            )
        }
    }
}