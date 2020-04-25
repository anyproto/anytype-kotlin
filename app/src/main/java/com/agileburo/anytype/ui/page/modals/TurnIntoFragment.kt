package com.agileburo.anytype.ui.page.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.TurnIntoActionReceiver
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter
import com.agileburo.anytype.core_ui.layout.SpacingItemDecoration
import com.agileburo.anytype.core_ui.model.UiBlock
import com.agileburo.anytype.core_utils.ext.dimen
import com.agileburo.anytype.core_utils.ui.BaseBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_add_block.*

class TurnIntoFragment : BaseBottomSheetFragment() {

    companion object {
        fun newInstance(): TurnIntoFragment = TurnIntoFragment()
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
        (parentFragment as? TurnIntoActionReceiver)?.onTurnIntoBlockClicked(block)
        dismiss()
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}
}