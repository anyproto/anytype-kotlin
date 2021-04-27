package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.text.Editable
import android.view.View
import android.widget.ImageView
import androidx.core.view.updatePadding
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.page.SupportNesting
import com.anytypeio.anytype.core_ui.features.page.marks
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.page.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.page.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import kotlinx.android.synthetic.main.item_block_checkbox.view.*

class Checkbox(
    view: View,
    onContextMenuStyleClick: (IntRange) -> Unit
) : Text(view), SupportNesting {

    var mode = BlockView.Mode.EDIT

    val checkbox: ImageView = itemView.checkboxIcon
    private val container = itemView.checkboxBlockContentContainer
    override val content: TextInputWidget = itemView.checkboxContent
    override val root: View = itemView

    init {
        setup(onContextMenuStyleClick)
    }

    fun bind(
        item: BlockView.Text.Checkbox,
        onTextBlockTextChanged: (BlockView.Text) -> Unit,
        onCheckboxClicked: (BlockView.Text.Checkbox) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        clicked: (ListenerType) -> Unit,
        onMentionEvent: (MentionEvent) -> Unit,
        onSplitLineEnterClicked: (String, Editable, IntRange) -> Unit,
        onEmptyBlockBackspaceClicked: (String) -> Unit,
        onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit,
        onTextInputClicked: (String) -> Unit,
        onBackPressedCallback: () -> Boolean
    ) = super.bind(
        item = item,
        onTextChanged = { _, editable ->
            item.apply {
                text = editable.toString()
                marks = editable.marks()
            }
            onTextBlockTextChanged(item)
        },
        onSelectionChanged = onSelectionChanged,
        clicked = clicked,
        onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
        onSplitLineEnterClicked = onSplitLineEnterClicked,
        onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
        onTextInputClicked = onTextInputClicked,
        onBackPressedCallback = onBackPressedCallback
    ).also {
        checkbox.isActivated = item.isChecked
        setCheckboxClickListener(item, onCheckboxClicked)
        setupMentionWatcher(onMentionEvent)
    }

    private fun setCheckboxClickListener(
        item: BlockView.Text.Checkbox,
        onCheckboxClicked: (BlockView.Text.Checkbox) -> Unit
    ) {
        checkbox.setOnClickListener {
            if (mode == BlockView.Mode.EDIT) {
                item.isChecked = !item.isChecked
                checkbox.isActivated = !checkbox.isActivated
                applyCheckedCheckboxColorSpan(item.isChecked)
                onCheckboxClicked(item)
            }
        }
    }

    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = with(itemView) {
        Pair(
            first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_default),
            second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
        )
    }

    override fun indentize(item: BlockView.Indentable) {
        checkbox.updatePadding(left = item.indent * dimen(R.dimen.indent))
    }

    override fun enableEditMode() {
        super.enableEditMode()
        mode = BlockView.Mode.EDIT
    }

    override fun enableReadMode() {
        super.enableReadMode()
        mode = BlockView.Mode.READ
    }

    override fun select(item: BlockView.Selectable) {
        container.isSelected = item.isSelected
    }
}