package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.databinding.FragmentRelationValueBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel
import com.anytypeio.anytype.presentation.sets.RelationValueDVViewModel
import com.anytypeio.anytype.ui.relations.add.AddFileRelationFragment
import com.anytypeio.anytype.ui.relations.add.AddObjectRelationFragment
import com.anytypeio.anytype.ui.relations.add.AddOptionsRelationDVFragment
import javax.inject.Inject

open class RelationValueDVFragment : RelationValueBaseFragment<FragmentRelationValueBinding>(),
    FileActionsFragment.FileActionReceiver,
    AddFileRelationFragment.FileValueAddReceiver {

    @Inject
    lateinit var factory: RelationValueDVViewModel.Factory
    override val vm: RelationValueDVViewModel by viewModels { factory }

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

    override val onStatusClickedCallback: (RelationValueView.Option.Status) -> Unit = {}

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
        with(lifecycleScope) {
            subscribe(btnEditOrDone.clicks()) { vm.onEditOrDoneClicked(isLocked) }
            subscribe(btnAddValue.clicks()) { vm.onAddValueClicked(isLocked) }
        }
    }

    override fun observeViews(values: List<RelationValueView>) {
        relationValueAdapter.update(values)
    }

    override fun onObjectValueChanged(
        ctx: Id,
        objectId: Id,
        relationKey: Key,
        ids: List<Id>
    ) {
        vm.onAddObjectsOrFilesValueToRecord(
            ctx = ctx,
            record = objectId,
            relationKey = relationKey,
            ids = ids
        )
    }

    override fun onFileValueChanged(ctx: Id, objectId: Id, relationKey: Key, ids: List<Id>) {
        vm.onAddObjectsOrFilesValueToRecord(
            ctx = ctx,
            record = objectId,
            relationKey = relationKey,
            ids = ids
        )
    }

    override fun observeCommands(command: RelationValueBaseViewModel.ObjectRelationValueCommand) {
        when (command) {
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowAddObjectScreen -> {
                showAddObjectScreen()
            }
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowAddStatusOrTagScreen -> {
                showAddStatusOrTagScreen()
            }
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowAddFileScreen -> {
                showAddFileScreen()
            }
            RelationValueBaseViewModel.ObjectRelationValueCommand.ShowFileValueActionScreen -> {
                //turn off for now https://app.clickup.com/t/h59z1j
                //FileActionsFragment().showChildFragment()
                openFilePicker()
            }
        }
    }

    private fun showAddObjectScreen() {
        val fr = AddObjectRelationFragment.new(
            ctx = ctx,
            relationKey = relationKey,
            objectId = target,
            types = types,
            flow = AddObjectRelationFragment.FLOW_DATAVIEW
        )
        fr.showChildFragment()
    }

    private fun showAddStatusOrTagScreen() {
        val fr = AddOptionsRelationDVFragment.new(
            ctx = ctx,
            target = target,
            relationKey = relationKey
        )
        fr.showChildFragment()
    }

    private fun showAddFileScreen() {
        val fr = AddFileRelationFragment.new(
            ctx = ctx,
            objectId = target,
            flow = AddFileRelationFragment.FLOW_DATAVIEW,
            relationKey = relationKey
        )
        fr.showChildFragment()
    }

    override fun onFileValueActionAdd() {
        vm.onFileValueActionAddClicked()
    }

    override fun onFileValueActionUploadFromGallery() {
        toast("Not implemented")
        vm.onFileValueActionUploadFromGalleryClicked()
    }

    override fun onFileValueActionUploadFromStorage() {
        toast("Not implemented")
        vm.onFileValueActionUploadFromStorageClicked()
    }

    override fun PickiTonMultipleCompleteListener(
        paths: ArrayList<String>?,
        wasSuccessful: Boolean,
        Reason: String?
    ) {
        toast("Not implemented yet")
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRelationValueBinding = FragmentRelationValueBinding.inflate(
        inflater, container, false
    )

    override fun injectDependencies() {
        componentManager().objectSetObjectRelationValueComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetObjectRelationValueComponent.release(ctx)
    }
}