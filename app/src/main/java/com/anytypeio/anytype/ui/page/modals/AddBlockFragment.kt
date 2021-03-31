package com.anytypeio.anytype.ui.page.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.page.editor.model.UiBlock
import com.anytypeio.anytype.presentation.page.picker.AddBlockView
import com.anytypeio.anytype.presentation.page.picker.DocumentAddBlockViewModel
import com.anytypeio.anytype.presentation.page.picker.DocumentAddBlockViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_add_block.*
import javax.inject.Inject

class AddBlockFragment : BaseBottomSheetFragment() {

    companion object {
        fun newInstance(ctx: Id): AddBlockFragment = AddBlockFragment().apply {
            arguments = bundleOf(CONTEXT_ID to ctx)
        }

        const val CONTEXT_ID = "arg.add-new-block.ctx"
    }

    @Inject
    lateinit var factory: DocumentAddBlockViewModelFactory
    private val vm by viewModels<DocumentAddBlockViewModel> { factory }

    private val ctx get() = argString(CONTEXT_ID)

    private val addBlockOrTurnIntoAdapter = AddBlockOrTurnIntoAdapter(
        views = mutableListOf(),
        onUiBlockClicked = { type -> dispatchAndExit(type) },
        onObjectClicked = { vm.onObjectTypeClicked(it) }
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

    override fun onStart() {
        with(lifecycleScope) {
            jobs += subscribe(vm.views) { observeViews(it) }
            jobs += subscribe(vm.commands) { observeCommands(it) }
        }
        super.onStart()
        vm.onStart()
    }

    private fun observeViews(views: List<AddBlockView>) {
        addBlockOrTurnIntoAdapter.update(views)
    }

    private fun observeCommands(commands: DocumentAddBlockViewModel.Commands) {
        when (commands) {
            is DocumentAddBlockViewModel.Commands.NotifyOnObjectTypeClicked -> {
                withParent<AddBlockActionReceiver> {
                    onAddObjectClicked(
                        commands.url,
                        commands.layout
                    )
                }
                dismiss()
            }
        }
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

    override fun injectDependencies() {
        componentManager().documentAddNewBlockComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().documentAddNewBlockComponent.release(ctx)
    }

    interface AddBlockActionReceiver {
        fun onAddBlockClicked(block: UiBlock)
        fun onAddObjectClicked(url: String, layout: ObjectType.Layout)
    }
}