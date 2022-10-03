package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.text.Editable
import android.view.View
import androidx.annotation.CallSuper
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.applyMovementMethod
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.TextBlockHolder
import com.anytypeio.anytype.core_ui.features.editor.performInEditMode
import com.anytypeio.anytype.core_ui.features.editor.provide
import com.anytypeio.anytype.core_ui.features.editor.withBlock
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.Checkable
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent

abstract class Text<BlockTextType : BlockView.Text>(
    view: View,
    protected val clicked: (ListenerType) -> Unit
) : BlockViewHolder(view), TextBlockHolder, BlockViewHolder.IndentableHolder,
    BlockViewHolder.DragAndDropHolder {

    private val defTextColor: Int = itemView.context.resources.getColor(R.color.text_primary, null)

    open fun bind(
        item: BlockTextType,
    ) {
        indentize(item)
        select(item)
        inputAction(item)

        if (item.mode == BlockView.Mode.READ) {
            enableReadMode()
            setContent(
                item = item,
                clicked = clicked
            )
            setStyle(item)
        } else {

            content.pauseTextWatchers {
                enableEditMode()
            }

            content.applyMovementMethod(item)

            setContent(
                item = item,
                clicked = clicked
            )
            setStyle(item)

            if (item.isFocused) setCursor(item)

            setFocus(item)
        }
    }

    private fun setContent(item: BlockView.TextBlockProps, clicked: (ListenerType) -> Unit) {
        content.pauseTextWatchers {
            setBlockText(
                text = item.text,
                markup = item,
                clicked = clicked,
                textColor = resolveTextBlockThemedColor(item.color)
            )
        }
        if (item is BlockView.Searchable) {
            applySearchHighlight(item)
        }
        if (item is BlockView.SupportGhostEditorSelection) {
            applyGhostEditorSelection(item)
        }
        if (item is Checkable) {
            applyCheckedCheckboxColorSpan(item.isChecked)
        }
    }

    private fun setStyle(item: BlockView.TextBlockProps) {
        setTextColor(item.color)
        setBackgroundColor(background = item.background)
    }

    private fun observe(
        onTextChanged: (Editable) -> Unit,
        onBackPressedCallback: (() -> Boolean)? = null
    ) {
        content.apply {
            addTextChangedListener(
                DefaultTextWatcher { text ->
                    provide<BlockView.Text>().performInEditMode { item ->
                        onTextChanged(text)
                    }
                }
            )
            backButtonWatcher = onBackPressedCallback
        }
    }

    fun selection(item: BlockView.Selectable) {
        select(item)
    }

    fun inputAction(item: BlockView.TextBlockProps) {
        content.setInputAction(item.inputAction)
    }

    override fun getDefaultTextColor(): Int = defTextColor

    @CallSuper
    open fun setupViewHolder(
        onTextChanged: (Editable) -> Unit,
        onSplitLineEnterClicked: (String, Editable, IntRange) -> Unit,
        onEmptyBlockBackspaceClicked: (String) -> Unit,
        onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit,
        onMentionEvent: ((MentionEvent) -> Unit),
        onSlashEvent: (SlashEvent) -> Unit,
        onBackPressedCallback: (() -> Boolean)? = null
    ) {
        observe(onTextChanged, onBackPressedCallback)

        content.apply {
            enableEnterKeyDetector(
                onEnterClicked = { range ->
                    val id = provide<BlockView.Text>()?.id ?: return@enableEnterKeyDetector
                    val editable = content.text ?: return@enableEnterKeyDetector
                    onSplitLineEnterClicked(
                        id,
                        editable,
                        range
                    )
                }
            )
            enableBackspaceDetector(
                onEmptyBlockBackspaceClicked = {
                    val id = provide<BlockView.Text>()?.id ?: return@enableBackspaceDetector
                    onEmptyBlockBackspaceClicked(id)
                },
                onNonEmptyBlockBackspaceClicked = {
                    val id = provide<BlockView.Text>()?.id ?: return@enableBackspaceDetector
                    val editable = content.text ?: return@enableBackspaceDetector
                    onNonEmptyBlockBackspaceClicked(
                        id,
                        editable
                    )
                }
            )
        }
        setupMentionWatcher(onMentionEvent) { provide() }
        setupSlashWatcher(onSlashEvent) { provide() }
    }
}