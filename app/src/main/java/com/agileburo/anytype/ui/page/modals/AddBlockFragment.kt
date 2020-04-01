package com.agileburo.anytype.ui.page.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockView
import com.agileburo.anytype.core_utils.ui.BaseBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_add_block.*

class AddBlockFragment : BaseBottomSheetFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_block, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
    }

    private fun setupAdapter() {
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = AddBlockOrTurnIntoAdapter(
                views = listOf(
                    AddBlockView.Item(1),
                    AddBlockView.Item(1),
                    AddBlockView.Item(1),
                    AddBlockView.Item(1),
                    AddBlockView.Item(1),
                    AddBlockView.Item(1),
                    AddBlockView.Item(1),
                    AddBlockView.Item(1),
                    AddBlockView.Item(1),
                    AddBlockView.Item(1),
                    AddBlockView.Item(1),
                    AddBlockView.Item(1)
                )
            )
        }
    }

    override fun injectDependencies() {

    }

    override fun releaseDependencies() {
    }
}