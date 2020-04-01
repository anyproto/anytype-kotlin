package com.agileburo.anytype.ui.page.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter
import com.agileburo.anytype.core_ui.layout.SpacingItemDecoration
import com.agileburo.anytype.core_utils.ext.dimen
import com.agileburo.anytype.core_utils.ext.toast
import com.agileburo.anytype.core_utils.ui.BaseBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_add_block.*

class AddBlockFragment : BaseBottomSheetFragment() {

    private val addBlockOrTurnIntoAdapter = AddBlockOrTurnIntoAdapter(
        onOptionClicked = {
            toast("Option clicked")
        }
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
    }

    private fun setupAdapter() {
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = addBlockOrTurnIntoAdapter
            setHasFixedSize(true)
            addItemDecoration(
                SpacingItemDecoration(
                    firstItemSpacingTop = dimen(R.dimen.dp_16) * 3
                )
            )
        }
    }

    override fun injectDependencies() {

    }

    override fun releaseDependencies() {
    }
}