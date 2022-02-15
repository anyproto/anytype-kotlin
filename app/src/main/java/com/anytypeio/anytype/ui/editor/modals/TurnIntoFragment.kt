package com.anytypeio.anytype.ui.editor.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.editor.TurnIntoActionReceiver
import com.anytypeio.anytype.core_ui.features.editor.modal.AddBlockOrTurnIntoAdapter
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentTurnIntoBinding
import com.anytypeio.anytype.presentation.editor.editor.model.UiBlock
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

@Deprecated("To be deleted")
class TurnIntoFragment : BaseBottomSheetFragment<FragmentTurnIntoBinding>() {

    private val excludedCategories: List<UiBlock.Category>
        get() {
            val names = arguments?.getStringArrayList(ARG_EXCLUDED_CATEGORIES) ?: emptyList()
            return names.map { UiBlock.Category.valueOf(it) }
        }

    private val excludedTypes: List<UiBlock>
        get() {
            val names = arguments?.getStringArrayList(ARG_EXCLUDED_TYPES) ?: emptyList()
            return names.map { UiBlock.valueOf(it) }
        }

    private val target: String
        get() = requireArguments()
            .getString(ARG_TARGET_KEY)
            ?: throw IllegalStateException(MISSING_TARGET_ERROR)

    private val isMultiSelectMode: Boolean
        get() = requireArguments()
            .getBoolean(ARG_MULTI_SELECT_MODE_KEY, false)


    private val addBlockOrTurnIntoAdapter by lazy {
        AddBlockOrTurnIntoAdapter(
            views = AddBlockOrTurnIntoAdapter.turnIntoAdapterData(
                excludedCategories = excludedCategories,
                excludedTypes = excludedTypes
            ),
            onUiBlockClicked = { type -> dispatchAndExit(type) },
            onObjectClicked = { /*TODO Turn into objects will be implemented later */ }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_turn_into, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        binding.close.setOnClickListener { dismiss() }
        skipCollapsedState()
    }

    private fun skipCollapsedState() {
        dialog?.setOnShowListener { dg ->
            val bottomSheet = (dg as? BottomSheetDialog)?.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.skipCollapsed = true
            }
        }
    }

    private fun setupAdapter() {
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = addBlockOrTurnIntoAdapter
            setHasFixedSize(true)
        }
    }

    private fun dispatchAndExit(block: UiBlock) {
        if (isMultiSelectMode) {
            (parentFragment as? TurnIntoActionReceiver)?.onTurnIntoMultiSelectBlockClicked(
                block = block
            )
        } else {
            (parentFragment as? TurnIntoActionReceiver)?.onTurnIntoBlockClicked(
                block = block,
                target = target
            )
        }
        dismiss()
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTurnIntoBinding = FragmentTurnIntoBinding.inflate(
        inflater, container, false
    )

    companion object {

        fun single(
            target: Id,
            excludedCategories: List<String> = emptyList(),
            excludedTypes: List<String> = emptyList()
        ): TurnIntoFragment = TurnIntoFragment().apply {
            arguments = bundleOf(
                ARG_TARGET_KEY to target,
                ARG_EXCLUDED_TYPES to ArrayList(excludedTypes),
                ARG_EXCLUDED_CATEGORIES to ArrayList(excludedCategories)
            )
        }

        fun multiple(
            excludedCategories: List<String> = emptyList(),
            excludedTypes: List<String> = emptyList()
        ): TurnIntoFragment = TurnIntoFragment().apply {
            arguments = bundleOf(
                ARG_MULTI_SELECT_MODE_KEY to true,
                ARG_EXCLUDED_TYPES to ArrayList(excludedTypes),
                ARG_EXCLUDED_CATEGORIES to ArrayList(excludedCategories)
            )
        }

        private const val ARG_TARGET_KEY = "arg.turn-into.target"
        private const val ARG_EXCLUDED_CATEGORIES = "arg.turn-into.excluded_categories"
        private const val ARG_EXCLUDED_TYPES = "arg.turn-into.excluded_types"
        private const val ARG_MULTI_SELECT_MODE_KEY = "arg.turn-into.is-multi-select"
        private const val MISSING_TARGET_ERROR = "Target missing in args"
    }
}