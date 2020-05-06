package com.agileburo.anytype.ui.page.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.TurnIntoActionReceiver
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter
import com.agileburo.anytype.core_ui.layout.SpacingItemDecoration
import com.agileburo.anytype.core_ui.model.UiBlock
import com.agileburo.anytype.core_utils.ext.dimen
import com.agileburo.anytype.core_utils.ui.BaseBottomSheetFragment
import com.agileburo.anytype.domain.common.Id
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_add_block.*

class TurnIntoFragment : BaseBottomSheetFragment() {

    private val target: String
        get() = requireArguments()
            .getString(ARG_TARGET_KEY)
            ?: throw IllegalStateException(MISSING_TARGET_ERROR)

    private val isMultiSelectMode: Boolean
        get() = requireArguments()
            .getBoolean(ARG_MULTI_SELECT_MODE_KEY, false)

    companion object {

        fun single(target: Id): TurnIntoFragment = TurnIntoFragment().apply {
            arguments = bundleOf(ARG_TARGET_KEY to target)
        }

        fun multiple(): TurnIntoFragment = TurnIntoFragment().apply {
            arguments = bundleOf(ARG_MULTI_SELECT_MODE_KEY to true)
        }

        private const val ARG_TARGET_KEY = "arg.turn-into.target"
        private const val ARG_MULTI_SELECT_MODE_KEY = "arg.turn-into.is-multi-select"
        private const val MISSING_TARGET_ERROR = "Target missing in args"
    }

    private val addBlockOrTurnIntoAdapter = AddBlockOrTurnIntoAdapter(
        views = AddBlockOrTurnIntoAdapter.turnIntoAdapterData(),
        onUiBlockClicked = { type -> dispatchAndExit(type) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_turn_into, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        close.setOnClickListener { dismiss() }
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
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = addBlockOrTurnIntoAdapter
            setHasFixedSize(true)
            addItemDecoration(SpacingItemDecoration(firstItemSpacingTop = dimen(R.dimen.dp_16)))
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
}