package com.anytypeio.anytype.core_ui.features.editor

import android.content.Context
import android.text.Editable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import java.util.*

open class BlockAdapterTestSetup {

    private val clipboardInterceptor: ClipboardInterceptor = object: ClipboardInterceptor {
        override fun onClipboardAction(action: ClipboardInterceptor.Action) {}
        override fun onUrlPasted(url: Url) {}
    }

    val context: Context = ApplicationProvider.getApplicationContext()

    fun buildAdapter(
        views: List<BlockView>,
        onFocusChanged: (String, Boolean) -> Unit = { _, _ -> },
        onTitleTextChanged: (Editable) -> Unit = {},
        onTextBlockTextChanged: (BlockView.Text) -> Unit = {},
        onTextInputClicked: (String) -> Unit = {},
        onSplitLineEnterClicked: (String, Editable, IntRange) -> Unit = { _, _, _ ->},
        onTextChanged: (String, Editable) -> Unit = { _, _ -> },
        onToggleClicked: (String) -> Unit = {},
        onEmptyBlockBackspaceClicked: (String) -> Unit = {},
        onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit = { _, _ -> },
        onCheckboxClicked: (BlockView.Text.Checkbox) -> Unit = {}
    ): BlockAdapter {
        return BlockAdapter(
            restore = LinkedList(),
            blocks = views,
            onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
            onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
            onSplitLineEnterClicked = onSplitLineEnterClicked,
            onSplitDescription = { _, _, _ -> },
            onTextChanged = onTextChanged,
            onCheckboxClicked = onCheckboxClicked,
            onFocusChanged = onFocusChanged,
            onSelectionChanged = { _, _ -> },
            onTextInputClicked = onTextInputClicked,
            onPageIconClicked = {},
            onTogglePlaceholderClicked = {},
            onToggleClicked = onToggleClicked,
            onTextBlockTextChanged = onTextBlockTextChanged,
            onTitleBlockTextChanged = {_, _ -> },
            onTitleTextInputClicked = {},
            onClickListener = {},
            clipboardInterceptor = clipboardInterceptor,
            onMentionEvent = {},
            onBackPressedCallback = { false },
            onCoverClicked = {},
            onSlashEvent = {},
            onKeyPressedEvent = {},
            onDragListener = EditorDragAndDropListener(
                onDragEnded = { _, _ -> },
                onDragExited = {},
                onDragLocation = { _, _ -> },
                onDrop = { _, _ -> },
                onDragStart = {}
            ),
            onDragAndDropTrigger = { _, _ -> false },
            dragAndDropSelector = DragAndDropAdapterDelegate(),
            lifecycle = object : Lifecycle() {
                override fun addObserver(observer: LifecycleObserver) {}
                override fun removeObserver(observer: LifecycleObserver) {}
                override fun getCurrentState() = State.DESTROYED
            }
        )
    }
}