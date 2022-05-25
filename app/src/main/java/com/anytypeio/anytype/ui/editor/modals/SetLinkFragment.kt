package com.anytypeio.anytype.ui.editor.modals

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.multilineIme
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentLinkBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.editor.LinkAddViewModel
import com.anytypeio.anytype.presentation.editor.LinkAddViewModelFactory
import com.anytypeio.anytype.presentation.editor.LinkViewState
import com.anytypeio.anytype.ui.editor.OnFragmentInteractionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import javax.inject.Inject

class SetLinkFragment : BaseBottomSheetFragment<FragmentLinkBinding>() {

    companion object {
        const val ARG_URL = "arg.link.url"
        const val ARG_TEXT = "arg.link.text"
        const val ARG_RANGE_START = "arg.link.range.start"
        const val ARG_RANGE_END = "arg.link.range.end"
        const val ARG_BLOCK_ID = "arg.link.block.id"

        fun newInstance(
            text: String,
            initUrl: String?,
            rangeStart: Int,
            rangeEnd: Int,
            blockId: String
        ) =
            SetLinkFragment().apply {
                arguments = bundleOf(
                    ARG_TEXT to text,
                    ARG_URL to initUrl,
                    ARG_RANGE_START to rangeStart,
                    ARG_RANGE_END to rangeEnd,
                    ARG_BLOCK_ID to blockId
                )
            }
    }

    @Inject
    lateinit var factory: LinkAddViewModelFactory
    private val vm by viewModels<LinkAddViewModel> { factory }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        setModalToFullScreenState(dialog)
        return dialog
    }

    private fun setModalToFullScreenState(dialog: BottomSheetDialog) =
        dialog.setOnShowListener { dialogInterface ->
            (dialogInterface as? BottomSheetDialog)?.let { bottomSheetDialog ->
                bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                    ?.let { bottomSheetView ->
                        BottomSheetBehavior.from(bottomSheetView).apply {
                            state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, Observer { state -> render(state) })
        arguments?.let {
            vm.onViewCreated(
                text = it.getString(ARG_TEXT, ""),
                initUrl = it.getString(ARG_URL),
                range = IntRange(it.getInt(ARG_RANGE_START), it.getInt(ARG_RANGE_END))
            )
        }
    }

    private fun render(state: LinkViewState) {
        when (state) {
            is LinkViewState.Init -> {
                binding.text.text = state.text
                binding.link.setText(state.url)
                if (state.url.isNullOrBlank()) {
                    enableEditMode()
                    binding.buttonLink.visible()
                    binding.buttonUnlink.invisible()
                } else {
                    enableReadMode()
                    binding.buttonLink.invisible()
                    binding.buttonUnlink.visible()
                }
                binding.buttonCancel.setOnClickListener {
                    vm.onCancelClicked()
                }
                binding.buttonLink.setOnClickListener {
                    vm.onLinkButtonClicked(binding.link.text.toString())
                }
                binding.buttonUnlink.setOnClickListener {
                    vm.onUnlinkButtonClicked()
                }
            }
            is LinkViewState.AddLink -> {
                (parentFragment as? OnFragmentInteractionListener)?.onAddMarkupLinkClicked(
                    link = state.link,
                    range = state.range,
                    blockId = arguments?.getString(ARG_BLOCK_ID, "").orEmpty()
                )
                dismiss()
            }
            is LinkViewState.Unlink -> {
                (parentFragment as? OnFragmentInteractionListener)?.onRemoveMarkupLinkClicked(
                    range = state.range,
                    blockId = arguments?.getString(ARG_BLOCK_ID, "").orEmpty()
                )
                dismiss()
            }
            is LinkViewState.Cancel -> {
                dismiss()
            }
        }
    }

    private fun enableEditMode() {
        with(binding.link) {
            setTextColor(requireContext().color(R.color.black))
            multilineIme(action = EditorInfo.IME_ACTION_DONE)
            setTextIsSelectable(true)
        }
    }

    private fun enableReadMode() {
        with(binding.link) {
            setTextColor(requireContext().color(R.color.hint_color))
            inputType = InputType.TYPE_NULL
            setRawInputType(InputType.TYPE_NULL)
            maxLines = Integer.MAX_VALUE
            setHorizontallyScrolling(false)
            setTextIsSelectable(false)
        }
    }

    override fun injectDependencies() {
        componentManager().linkAddComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().linkAddComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLinkBinding = FragmentLinkBinding.inflate(
        inflater, container, false
    )
}