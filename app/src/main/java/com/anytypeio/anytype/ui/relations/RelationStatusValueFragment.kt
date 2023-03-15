package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.proceed
import com.anytypeio.anytype.databinding.FragmentRelationStatusValueBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel
import com.anytypeio.anytype.presentation.sets.RelationValueViewModel
import com.anytypeio.anytype.ui.relations.add.AddOptionsRelationFragment
import javax.inject.Inject

class RelationStatusValueFragment :
    RelationValueBaseFragment<FragmentRelationStatusValueBinding>() {

    @Inject
    lateinit var factory: RelationValueViewModel.Factory
    override val vm: RelationValueViewModel by viewModels { factory }

    override val root: View
        get() = binding.root
    override val recycler: RecyclerView
        get() = binding.recycler
    override val btnAddValue: ImageView
        get() = binding.btnAddValue
    override val btnEditOrDone: TextView
        get() = binding.btnEditOrDone
    override val refresh: ProgressBar
        get() = binding.refresh
    override val tvRelationHeader: TextView
        get() = binding.tvRelationHeader

    private val btnClear
        get() = binding.btnClear
    private val tvEmptyPlaceholder
        get() = binding.tvEmptyPlaceholder

    override val onStatusClickedCallback: (RelationValueView.Option.Status) -> Unit = {
        vm.onAddValueClicked(isLocked)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = relationValueAdapter
        }
        dividerItem = DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
            setDrawable(drawable(R.drawable.divider_relations))
        }
        dividerItemEdit = DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
            setDrawable(drawable(R.drawable.divider_relations_edit))
        }
        proceed(btnAddValue.clicks()) { vm.onAddValueClicked(isLocked) }
        proceed(btnClear.clicks()) {
            vm.onRemoveStatusFromObjectClicked(
                ctx = ctx,
                target = target,
                relationKey = relationKey,
            )
        }
    }

    override fun observeViews(values: List<RelationValueView>) {
        if (values.isNotEmpty() && values.first() is RelationValueView.Empty) {
            recycler.gone()
            btnClear.gone()
            tvEmptyPlaceholder.visible()
            btnAddValue.visible()
        } else {
            recycler.visible()
            btnClear.visible()
            tvEmptyPlaceholder.gone()
            btnAddValue.gone()
        }
        relationValueAdapter.update(values)
    }

    override fun onObjectValueChanged(ctx: Id, objectId: Id, relationKey: Key, ids: List<Id>) {
        vm.onAddObjectsOrFilesValueToObject(
            ctx = ctx,
            target = objectId,
            relationKey = relationKey,
            ids = ids
        )
    }

    override fun PickiTonMultipleCompleteListener(
        paths: ArrayList<String>?,
        wasSuccessful: Boolean,
        Reason: String?
    ) {
        toast("Not implemented yet")
    }

    override fun observeCommands(command: RelationValueBaseViewModel.ObjectRelationValueCommand) {
        when (command) {
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowAddStatusOrTagScreen -> {
                showAddStatusOrTagScreen()
            }
            else -> {}
        }
    }

    private fun showAddStatusOrTagScreen() {
        val fr = AddOptionsRelationFragment.new(
            ctx = ctx,
            objectId = target,
            relationKey = relationKey
        )
        fr.showChildFragment()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRelationStatusValueBinding {
        return FragmentRelationStatusValueBinding.inflate(
            inflater, container, false
        )
    }

    override fun injectDependencies() {
        componentManager().objectObjectRelationValueComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectObjectRelationValueComponent.release(ctx)
    }

    companion object {
        fun new(
            ctx: Id,
            target: Id,
            relationKey: Key,
            targetObjectTypes: List<Id>,
            isLocked: Boolean = false
        ) = RelationStatusValueFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                TARGET_KEY to target,
                RELATION_KEY to relationKey,
                TARGET_TYPES_KEY to targetObjectTypes,
                IS_LOCKED_KEY to isLocked
            )
        }
    }
}