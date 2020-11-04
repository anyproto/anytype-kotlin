package com.anytypeio.anytype.ui.page.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.presentation.page.editor.model.UiBlock
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_add_block.*

class AddBlockFragment : BaseBottomSheetFragment() {

    companion object {
        fun newInstance(): AddBlockFragment = AddBlockFragment()
    }

    private val addBlockOrTurnIntoAdapter = AddBlockOrTurnIntoAdapter(
        views = AddBlockOrTurnIntoAdapter.addBlockAdapterData(),
        onUiBlockClicked = { type -> dispatchAndExit(type) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_block, container, false)

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
        }
    }

    private fun dispatchAndExit(block: UiBlock) {
        (parentFragment as? AddBlockActionReceiver)?.onAddBlockClicked(block)
        dismiss()
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    interface AddBlockActionReceiver {
        fun onAddBlockClicked(block: UiBlock)
    }
}