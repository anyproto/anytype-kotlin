package com.agileburo.anytype.ui.page.modals

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ext.toast
import com.agileburo.anytype.core_utils.ui.BaseBottomSheetFragment
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.library_page_icon_picker_widget.ui.PageIconPickerAdapter
import com.agileburo.anytype.library_page_icon_picker_widget.ui.PageIconPickerViewHolder
import com.agileburo.anytype.presentation.page.picker.PageIconPickerViewModel
import com.agileburo.anytype.presentation.page.picker.PageIconPickerViewModel.Contract
import com.agileburo.anytype.presentation.page.picker.PageIconPickerViewModel.ViewState
import com.agileburo.anytype.presentation.page.picker.PageIconPickerViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_page_icon_picker.*
import javax.inject.Inject

class PageIconPickerFragment : BaseBottomSheetFragment() {

    private val target: String
        get() = requireArguments()
            .getString(ARG_TARGET_ID_KEY)
            ?: throw IllegalStateException(MISSING_TARGET_ERROR)

    private val context: String
        get() = requireArguments()
            .getString(ARG_CONTEXT_ID_KEY)
            ?: throw IllegalStateException(MISSING_CONTEXT_ERROR)

    @Inject
    lateinit var factory: PageIconPickerViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(PageIconPickerViewModel::class.java)
    }

    private val pageIconPickerAdapter by lazy {
        PageIconPickerAdapter(
            views = emptyList(),
            onUploadPhotoClicked = { toast(NOT_IMPLEMENTED_MESSAGE) },
            onFilterQueryChanged = { vm.onEvent(Contract.Event.OnFilterQueryChanged(it)) },
            onSetRandomEmojiClicked = {
                vm.onEvent(
                    Contract.Event.OnSetRandomEmojiClicked(
                        target = target,
                        context = context
                    )
                )
            },
            onEmojiClicked = { unicode, alias ->
                vm.onEvent(
                    Contract.Event.OnEmojiClicked(
                        unicode = unicode,
                        alias = alias,
                        target = target,
                        context = context
                    )
                )
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
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

        recyler.apply {
            setItemViewCacheSize(EMOJI_RECYCLER_ITEM_VIEW_CACHE_SIZE)
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int) =
                        when (val type = pageIconPickerAdapter.getItemViewType(position)) {
                            PageIconPickerViewHolder.HOLDER_UPLOAD_PHOTO -> PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT
                            PageIconPickerViewHolder.HOLDER_CHOOSE_EMOJI -> PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT
                            PageIconPickerViewHolder.HOLDER_PICK_RANDOM_EMOJI -> PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT
                            PageIconPickerViewHolder.HOLDER_EMOJI_CATEGORY_HEADER -> PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT
                            PageIconPickerViewHolder.HOLDER_EMOJI_FILTER -> PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT
                            PageIconPickerViewHolder.HOLDER_EMOJI_ITEM -> 1
                            else -> throw IllegalStateException("$UNEXPECTED_VIEW_TYPE_MESSAGE: $type")
                        }
                }
            }
            adapter = pageIconPickerAdapter.apply {
                setHasStableIds(true)
            }
        }

        remove.setOnClickListener {
            vm.onEvent(
                Contract.Event.OnRemoveEmojiSelected(
                    context = context,
                    target = target
                )
            )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.state.observe(viewLifecycleOwner, Observer { render(it) })
    }

    override fun onDestroyView() {
        dialog?.setOnShowListener { null }
        super.onDestroyView()
    }

    fun render(state: ViewState) {
        when (state) {
            is ViewState.Success -> pageIconPickerAdapter.update(state.views)
            is ViewState.Exit -> dismiss()
            is ViewState.Error -> toast(state.message)
        }
    }

    override fun injectDependencies() {
        componentManager().pageIconPickerSubComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().pageIconPickerSubComponent.release()
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

        fun newInstance(
            context: String,
            target: String
        ) = PageIconPickerFragment().apply {
            arguments = bundleOf(ARG_CONTEXT_ID_KEY to context, ARG_TARGET_ID_KEY to target)
        }

        private const val PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT = 8
        private const val EMOJI_RECYCLER_ITEM_VIEW_CACHE_SIZE = 100
        private const val ARG_CONTEXT_ID_KEY = "arg.picker.context.id"
        private const val ARG_TARGET_ID_KEY = "arg.picker.target.id"
        private const val MISSING_TARGET_ERROR = "Missing target id"
        private const val MISSING_CONTEXT_ERROR = "Missing context id"
        private const val NOT_IMPLEMENTED_MESSAGE = "Not implemented"
        private const val UNEXPECTED_VIEW_TYPE_MESSAGE = "Unexpected view type"
    }
}