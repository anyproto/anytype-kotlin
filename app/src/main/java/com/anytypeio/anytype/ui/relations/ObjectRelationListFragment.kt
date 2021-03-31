package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.relations.DocumentRelationAdapter
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.ObjectRelationListViewModel
import com.anytypeio.anytype.presentation.relations.ObjectRelationListViewModel.Command
import com.anytypeio.anytype.presentation.relations.ObjectRelationListViewModelFactory
import com.anytypeio.anytype.ui.database.modals.ObjectObjectRelationValueFragment
import com.anytypeio.anytype.ui.page.OnFragmentInteractionListener
import kotlinx.android.synthetic.main.fragment_object_relation_list.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

open class ObjectRelationListFragment : BaseBottomSheetFragment(),
    ObjectRelationTextValueFragment.EditObjectRelationTextValueReceiver,
    ObjectRelationDateValueFragment.EditObjectRelationDateValueReceiver {

    private val vm by viewModels<ObjectRelationListViewModel> { factory }

    @Inject
    lateinit var factory: ObjectRelationListViewModelFactory

    private val ctx: String get() = argString(ARG_CTX)
    private val target: String? get() = argStringOrNull(ARG_TARGET)
    private val mode: Int get() = argInt(ARG_MODE)

    private val docRelationAdapter by lazy {
        DocumentRelationAdapter(emptyList()) {
            vm.onRelationClicked(
                ctx = ctx,
                target = target,
                view = it
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_object_relation_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler.apply {
            adapter = docRelationAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_relations))
                }
            )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(lifecycleScope) {
            if (mode == MODE_ADD) {
                searchBar.visible()
                val queries = searchRelationInput.textChanges()
                    .onStart { emit(searchRelationInput.text.toString()) }
                val views = vm.views.combine(queries) { views, query ->
                    if (views.isEmpty()) {
                        views
                    } else {
                        views.filter { it.name.contains(query, true) }
                    }
                }
                subscribe(views) { docRelationAdapter.update(it) }
            } else {
                searchBar.gone()
                subscribe(vm.views) { docRelationAdapter.update(it) }
            }
            subscribe(vm.commands) { command -> execute(command) }
        }
    }

    private fun execute(command: Command) {
        when (command) {
            is Command.EditTextRelationValue -> {
                val fr = ObjectRelationTextValueFragment.new(
                    ctx = ctx,
                    relationId = command.relation,
                    objectId = command.target
                )
                fr.show(childFragmentManager, null)
            }
            is Command.EditDateRelationValue -> {
                val fr = ObjectRelationDateValueFragment.new(
                    ctx = ctx,
                    relationId = command.relation,
                    objectId = command.target
                )
                fr.show(childFragmentManager, null)
            }
            is Command.EditRelationValue -> {
                val fr = ObjectObjectRelationValueFragment.new(
                    ctx = ctx,
                    relation = command.relation,
                    target = command.target
                )
                fr.show(childFragmentManager, null)
            }
            is Command.SetRelationKey -> {
                withParent<OnFragmentInteractionListener> {
                    onSetRelationKeyClicked(
                        blockId = command.blockId,
                        key = command.key
                    )
                }
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (mode == MODE_LIST) {
            vm.onStartListMode(ctx)
        } else {
            vm.onStartAddMode(ctx)
        }
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    override fun onRelationTextValueChanged(ctx: Id, text: String, objectId: Id, relationId: Id) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            value = text,
            objectId = objectId,
            relationId = relationId
        )
    }

    override fun onRelationTextNumberValueChanged(ctx: Id, number: Number, objectId: Id, relationId: Id) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            value = number,
            objectId = objectId,
            relationId = relationId
        )
    }

    override fun onRelationDateValueChanged(
        ctx: Id,
        timeInSeconds: Number?,
        objectId: Id,
        relationId: Id
    ) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            objectId = objectId,
            relationId = relationId,
            value = timeInSeconds
        )
    }

    override fun injectDependencies() {
        componentManager().documentRelationComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().documentRelationComponent.release(ctx)
    }

    companion object {
        fun new(ctx: String, target: String?, mode: Int) = ObjectRelationListFragment().apply {
            arguments = bundleOf(
                ARG_CTX to ctx,
                ARG_TARGET to target,
                ARG_MODE to mode
            )
        }

        const val ARG_CTX = "arg.document-relation.ctx"
        const val ARG_MODE = "arg.document-relation.mode"
        const val ARG_TARGET = "arg.document-relation.target"
        const val MODE_ADD = 1
        const val MODE_LIST = 2
    }
}