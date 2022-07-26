package com.anytypeio.anytype.ui.editor.modals

import android.content.DialogInterface
import android.os.Bundle
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.features.editor.BlockAdapter
import com.anytypeio.anytype.core_ui.features.editor.DragAndDropAdapterDelegate
import com.anytypeio.anytype.core_ui.features.editor.marks
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetImeOffsetFragment
import com.anytypeio.anytype.databinding.FragmentSetBlockTextValueBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ext.extractMarks
import com.anytypeio.anytype.presentation.objects.block.SetBlockTextValueViewModel
import com.anytypeio.anytype.ui.editor.OnFragmentInteractionListener
import java.util.*
import javax.inject.Inject

class SetBlockTextValueFragment :
    BaseBottomSheetImeOffsetFragment<FragmentSetBlockTextValueBinding>(), ClipboardInterceptor,
    View.OnDragListener {

    private val vm: SetBlockTextValueViewModel by viewModels { factory }

    var onDismissListener: (() -> Unit)? = null

    @Inject
    lateinit var factory: SetBlockTextValueViewModel.Factory

    private val blockAdapter by lazy {
        BlockAdapter(
            restore = LinkedList(),
            initialBlock = mutableListOf(),
            onTextChanged = { _, _ -> },
            onTitleBlockTextChanged = { _, _ -> },
            onSelectionChanged = { _, _ -> },
            onCheckboxClicked = {},
            onTitleCheckboxClicked = {},
            onFocusChanged = { _, _ -> },
            onSplitLineEnterClicked = { id, editable, _ ->
                vm.onKeyboardDoneKeyClicked(
                    ctx = ctx,
                    tableId = table,
                    targetId = id,
                    text = editable.toString(),
                    marks = editable.marks(),
                    markup = editable.extractMarks()
                )
            },
            onSplitDescription = { _, _, _ -> },
            onEmptyBlockBackspaceClicked = {},
            onNonEmptyBlockBackspaceClicked = { _, _ -> },
            onTextInputClicked = {},
            onPageIconClicked = {},
            onCoverClicked = {},
            onTogglePlaceholderClicked = {},
            onToggleClicked = {},
            onTitleTextInputClicked = {},
            onTextBlockTextChanged = {},
            onClickListener = vm::onClickListener,
            onMentionEvent = {},
            onSlashEvent = {},
            onBackPressedCallback = { false },
            onKeyPressedEvent = {},
            onDragAndDropTrigger = { _, _ -> false },
            lifecycle = lifecycle,
            dragAndDropSelector = DragAndDropAdapterDelegate(),
            clipboardInterceptor = this,
            onDragListener = this
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = blockAdapter
        }
    }

    override fun onStart() {
        with(lifecycleScope) {
            jobs += subscribe(vm.state) { render(it) }
            jobs += subscribe(vm.toasts) { toast(it) }
        }
        vm.onStart(tableId = table, blockId = block)
        super.onStart()
    }

    override fun onStop() {
        vm.onStop()
        super.onStop()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }

    private fun render(state: SetBlockTextValueViewModel.ViewState) {
        when (state) {
            SetBlockTextValueViewModel.ViewState.Exit -> {
                withParent<OnFragmentInteractionListener> { onSetTextBlockValue() }
                dismiss()
            }
            SetBlockTextValueViewModel.ViewState.Loading -> {
            }
            is SetBlockTextValueViewModel.ViewState.Success -> {
                blockAdapter.updateWithDiffUtil(state.data)
            }
            is SetBlockTextValueViewModel.ViewState.OnMention -> {
                withParent<OnFragmentInteractionListener> { onMentionClicked(state.targetId) }
                dismiss()
            }
        }
    }

    override fun injectDependencies() {
        componentManager().setTextBlockValueComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().setTextBlockValueComponent.release(ctx)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSetBlockTextValueBinding {
        return FragmentSetBlockTextValueBinding.inflate(inflater, container, false)
    }

    override fun onClipboardAction(action: ClipboardInterceptor.Action) {}
    override fun onUrlPasted(url: Url) {}
    override fun onDrag(v: View?, event: DragEvent?) = false

    private val ctx: String get() = argString(CTX_KEY)
    private val block: String get() = argString(BLOCK_ID_KEY)
    private val table: String get() = argString(TABLE_ID_KEY)

    companion object {
        const val CTX_KEY = "arg.editor.block.text.value.ctx"
        const val TABLE_ID_KEY = "arg.editor.block.text.value.table.id"
        const val BLOCK_ID_KEY = "arg.editor.block.text.value.block.id"
        const val DEFAULT_IME_ACTION = EditorInfo.IME_ACTION_DONE

        fun new(ctx: Id, table: Id, block: Id) = SetBlockTextValueFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                TABLE_ID_KEY to table,
                BLOCK_ID_KEY to block
            )
        }
    }
}