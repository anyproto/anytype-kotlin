package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.relations.RelationFileValueAdapter
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.FileValueAddCommand
import com.anytypeio.anytype.presentation.relations.FileValueAddView
import com.anytypeio.anytype.presentation.relations.RelationFileValueAddViewModel
import kotlinx.android.synthetic.main.fragment_relation_value_file_add.*
import javax.inject.Inject

class RelationFileValueAddFragment : BaseBottomSheetFragment() {

    @Inject
    lateinit var factory: RelationFileValueAddViewModel.Factory
    val vm: RelationFileValueAddViewModel by viewModels { factory }

    private val ctx get() = argString(CONTEXT_ID)
    private val objectId get() = argString(OBJECT_ID)
    private val relationId get() = argString(RELATION_ID)
    private val flow get() = arg<Int>(FLOW_KEY)

    private val adapter by lazy {
        RelationFileValueAdapter(
            onFileClick = { vm.onFileClicked(it) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_relation_value_file_add, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTransparentBackground()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvFiles.layoutManager = LinearLayoutManager(requireContext())
        rvFiles.adapter = adapter
        btnBottomAction.setOnClickListener { vm.onActionButtonClicked() }
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.viewsFiltered) { observeState(it) }
        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        jobs += lifecycleScope.subscribe(searchRelationInput.textChanges())
        { vm.onFilterTextChanged(it.toString()) }
        super.onStart()
        vm.onStart(objectId = objectId, relationId = relationId)
    }

    private fun observeState(state: FileValueAddView) {
        adapter.update(state.files)
        tvFilesCount.text = state.count
    }

    private fun observeCommands(command: FileValueAddCommand) {
        when (command) {
            is FileValueAddCommand.DispatchResult -> {
                dispatchResultAndDismiss(command.ids)
            }
        }
    }

    private fun dispatchResultAndDismiss(ids: List<Id>) {
        withParent<FileValueAddReceiver> {
            onFileValueChanged(
                ctx = ctx,
                objectId = objectId,
                relationId = relationId,
                ids = ids
            )
        }
        dismiss()
    }

    private fun setTransparentBackground() {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun injectDependencies() {
        if (flow == FLOW_DEFAULT) {
            componentManager().relationFileValueComponent.get(ctx).inject(this)
        } else {
            componentManager().relationFileValueDVComponent.get(ctx).inject(this)
        }
    }

    override fun releaseDependencies() {
        if (flow == FLOW_DEFAULT) {
            componentManager().relationFileValueComponent.release(ctx)
        } else {
            componentManager().relationFileValueDVComponent.release(ctx)
        }
    }

    companion object {

        fun new(
            ctx: Id,
            objectId: Id,
            relationId: Id,
            flow: Int = FLOW_DEFAULT
        ) = RelationFileValueAddFragment().apply {
            arguments = bundleOf(
                CONTEXT_ID to ctx,
                OBJECT_ID to objectId,
                RELATION_ID to relationId,
                FLOW_KEY to flow
            )
        }

        const val CONTEXT_ID = "arg.relation.add.file.context"
        const val RELATION_ID = "arg.relation.add.file.relation.id"
        const val OBJECT_ID = "arg.relation.add.file.object.id"
        const val FLOW_KEY = "arg.relation.add.file.flow"
        const val FLOW_DEFAULT = 0
        const val FLOW_DATAVIEW = 1
    }

    interface FileValueAddReceiver {
        fun onFileValueChanged(
            ctx: Id,
            objectId: Id,
            relationId: Id,
            ids: List<Id>
        )
    }
}