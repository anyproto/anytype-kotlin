package com.anytypeio.anytype.core_ui.features.history

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.editor.BlockAdapter
import com.anytypeio.anytype.presentation.history.VersionHistoryPreviewScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionHistoryPreviewScreen(
    state: VersionHistoryPreviewScreen,
    onDismiss: () -> Unit,
    onRestore: () -> Unit,
    editorAdapter: BlockAdapter
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (state is VersionHistoryPreviewScreen.Success) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxSize(),
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            containerColor = Color.Transparent,
            content = {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        LayoutInflater.from(context)
                            .inflate(R.layout.version_history_screen, null).apply {
                                findViewById<RecyclerView>(R.id.recycler).apply {
                                    adapter = editorAdapter
                                }
                                findViewById<View>(R.id.btnCancel).setOnClickListener {
                                    onDismiss()
                                }
                                findViewById<View>(R.id.btnRestore).setOnClickListener {
                                    onRestore()
                                }
                            }
                    },
                    update = {
                        editorAdapter.updateWithDiffUtil(state.blocks)
                        it.findViewById<TextView>(R.id.tvVersionDate).apply {
                            text = "${state.dateFormatted}, ${state.timeFormatted}"
                        }
                    }
                )
            },
            shape = RoundedCornerShape(16.dp),
            dragHandle = null,
            windowInsets = WindowInsets(0, 0, 0, 0)
        )
    }
}