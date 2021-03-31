package com.anytypeio.anytype.ui.page.modals

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.library_page_icon_picker_widget.ui.DocumentEmojiIconPickerAdapter
import com.anytypeio.anytype.presentation.page.picker.DocumentEmojiIconPickerViewModel
import com.anytypeio.anytype.presentation.page.picker.DocumentEmojiIconPickerViewModel.ViewState
import com.anytypeio.anytype.presentation.page.picker.DocumentEmojiIconPickerViewModelFactory
import com.anytypeio.anytype.presentation.page.picker.EmojiPickerView.Companion.HOLDER_EMOJI_CATEGORY_HEADER
import com.anytypeio.anytype.presentation.page.picker.EmojiPickerView.Companion.HOLDER_EMOJI_ITEM
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_page_icon_picker.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

open class DocumentEmojiIconPickerFragment : BaseBottomSheetFragment() {

    private val target: String
        get() = requireArguments()
            .getString(ARG_TARGET_ID_KEY)
            ?: throw IllegalStateException(MISSING_TARGET_ERROR)

    private val context: String
        get() = requireArguments()
            .getString(ARG_CONTEXT_ID_KEY)
            ?: throw IllegalStateException(MISSING_CONTEXT_ERROR)

    @Inject
    lateinit var factory: DocumentEmojiIconPickerViewModelFactory
    private val vm by viewModels<DocumentEmojiIconPickerViewModel> { factory }

    private val emojiPickerAdapter by lazy {
        DocumentEmojiIconPickerAdapter(
            views = emptyList(),
            onEmojiClicked = { unicode ->
                vm.onEmojiClicked(
                    unicode = unicode,
                    target = target,
                    context = context
                )
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_page_icon_picker, container, false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        setModalToFullScreenState(dialog)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        clearSearchText.setOnClickListener {
            filterInputField.setText(EMPTY_FILTER_TEXT)
            clearSearchText.invisible()
        }
        filterInputField.doAfterTextChanged { vm.onQueryChanged(it.toString()) }
    }

    private fun setupRecycler() {
        pickerRecycler.apply {
            setItemViewCacheSize(EMOJI_RECYCLER_ITEM_VIEW_CACHE_SIZE)
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int) =
                        when (val type = emojiPickerAdapter.getItemViewType(position)) {
                            HOLDER_EMOJI_ITEM -> 1
                            HOLDER_EMOJI_CATEGORY_HEADER -> PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT
                            else -> throw IllegalStateException("$UNEXPECTED_VIEW_TYPE_MESSAGE: $type")
                        }
                }
            }
            adapter = emojiPickerAdapter
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.state().onEach { state ->
            when (state) {
                is ViewState.Success -> {
                    if (filterInputField.text.isNotEmpty())
                        clearSearchText.visible()
                    else
                        clearSearchText.invisible()
                    emojiPickerAdapter.update(state.views)
                    progressBar.invisible()
                }
                is ViewState.Loading -> {
                    clearSearchText.invisible()
                    progressBar.visible()
                }
                is ViewState.Exit -> dismiss()
            }
        }.launchIn(lifecycleScope)
    }

    override fun onDestroyView() {
        dialog?.setOnShowListener(null)
        super.onDestroyView()
    }

    override fun injectDependencies() {
        componentManager().documentEmojiIconPickerComponent.get(context).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().documentEmojiIconPickerComponent.release(context)
    }

    private fun setModalToFullScreenState(dialog: BottomSheetDialog) =
        dialog.setOnShowListener { dialogInterface ->
            (dialogInterface as? BottomSheetDialog)?.let { bottomSheetDialog ->
                bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                    ?.let { bottomSheetView ->
                        BottomSheetBehavior.from(bottomSheetView).apply {
                            val lp = bottomSheetView.layoutParams
                            lp.height = activity?.window?.decorView?.measuredHeight ?: 0
                            bottomSheetView.layoutParams = lp
                            state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
            }
        }

    companion object {

        fun new(context: String, target: String) = DocumentEmojiIconPickerFragment().apply {
            arguments = bundleOf(
                ARG_CONTEXT_ID_KEY to context,
                ARG_TARGET_ID_KEY to target
            )
        }

        private const val EMPTY_FILTER_TEXT = ""
        private const val PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT = 6
        private const val EMOJI_RECYCLER_ITEM_VIEW_CACHE_SIZE = 2000
        private const val ARG_CONTEXT_ID_KEY = "arg.picker.context.id"
        private const val ARG_TARGET_ID_KEY = "arg.picker.target.id"
        private const val MISSING_TARGET_ERROR = "Missing target id"
        private const val MISSING_CONTEXT_ERROR = "Missing context id"
        private const val UNEXPECTED_VIEW_TYPE_MESSAGE = "Unexpected view type"
    }
}