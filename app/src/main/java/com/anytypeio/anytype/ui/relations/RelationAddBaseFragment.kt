package com.anytypeio.anytype.ui.relations

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.features.relations.RelationAddAdapter
import com.anytypeio.anytype.core_ui.features.relations.RelationAddHeaderAdapter
import com.anytypeio.anytype.core_ui.reactive.focusChanges
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.statusBarHeight
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetTextInputFragment
import com.anytypeio.anytype.databinding.FragmentRelationAddBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.RelationAddToDataViewViewModel
import com.anytypeio.anytype.presentation.relations.RelationAddToObjectViewModel
import com.anytypeio.anytype.presentation.relations.RelationAddViewModelBase
import com.anytypeio.anytype.presentation.relations.RelationAddViewModelBase.Command
import com.anytypeio.anytype.ui.editor.OnFragmentInteractionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject


abstract class RelationAddBaseFragment :
    BaseBottomSheetTextInputFragment<FragmentRelationAddBinding>() {

    abstract val vm: RelationAddViewModelBase

    override val textInput: EditText get() = binding.searchBar.root.findViewById(R.id.filterInputField)

    abstract val ctx: String

    private lateinit var searchRelationInput: EditText
    lateinit var clearSearchText: View

    protected val createFromScratchAdapter = RelationAddHeaderAdapter {
        onCreateFromScratchClicked()
    }

    private val relationAdapter = RelationAddAdapter { relation ->
        vm.onRelationSelected(
            ctx = ctx,
            relation = relation
        )
    }

    private val concatAdapter = ConcatAdapter(createFromScratchAdapter, relationAdapter)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchRelationInput = binding.searchBar.root.findViewById(R.id.filterInputField)
        searchRelationInput.apply {
            hint = getString(R.string.find_a_relation)
        }
        clearSearchText = binding.searchBar.root.findViewById(R.id.clearSearchText)
        clearSearchText.setOnClickListener {
            searchRelationInput.setText("")
            clearSearchText.invisible()
        }
        setupFullHeight()
        binding.relationAddRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = concatAdapter
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_relations_with_padding))
                }
            )
        }
        with(lifecycleScope) {
            subscribe(searchRelationInput.focusChanges()) { hasFocus -> if (hasFocus) expand(view) }
            subscribe(searchRelationInput.textChanges()) {
                createFromScratchAdapter.query = it.toString()
                vm.onQueryChanged(it.toString())
            }
            subscribe(vm.results) { relationAdapter.submitList(it) }
            subscribe(vm.isDismissed) { isDismissed -> if (isDismissed) dismiss() }
            subscribe(vm.toasts) { toast(it) }
            subscribe(searchRelationInput.textChanges()) {
                if (it.isEmpty()) clearSearchText.invisible() else clearSearchText.visible()
            }
            subscribe(vm.command) { command ->
                when (command) {
                    is Command.DispatchSelectedRelation -> {
                        onRelationSelected(
                            ctx = command.ctx,
                            relation = command.relation,
                            format = command.format
                        )
                    }
                }
            }
        }
    }

    private fun setupFullHeight() {
        val lp = (binding.root.layoutParams as FrameLayout.LayoutParams)
        lp.height =
            Resources.getSystem().displayMetrics.heightPixels - requireActivity().statusBarHeight
        binding.root.layoutParams = lp
    }

    private fun expand(root: View) {
        BottomSheetBehavior.from(root.parent as View).state = BottomSheetBehavior.STATE_EXPANDED
    }

    abstract fun onRelationSelected(ctx: Id, relation: Key, format: RelationFormat)
    abstract fun onCreateFromScratchClicked()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRelationAddBinding = FragmentRelationAddBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val CTX_KEY = "arg.relation-add.ctx"
    }
}

class RelationAddToObjectFragment : RelationAddBaseFragment() {

    override val ctx get() = arg<Id>(CTX_KEY)
    private val isSetOrCollection get() = arg<Boolean>(IS_SET_OR_COLLECTION_KEY)

    @Inject
    lateinit var factory: RelationAddToObjectViewModel.Factory
    override val vm: RelationAddToObjectViewModel by viewModels { factory }

    override fun onRelationSelected(ctx: Id, relation: Key, format: RelationFormat) {
        vm.onRelationSelected(
            ctx = ctx,
            relation = relation,
            format = format,
            screenType = EventsDictionary.Type.menu
        )
    }

    override fun onCreateFromScratchClicked() {
        val fr = RelationCreateFromScratchForObjectFragment.new(
            ctx = ctx,
            query = createFromScratchAdapter.query,
            isSetOrCollection = isSetOrCollection
        )
        fr.showChildFragment()
    }

    override fun injectDependencies() {
        if (isSetOrCollection) {
            componentManager().relationAddToObjectSetComponent.get(ctx).inject(this)
        } else {
            componentManager().relationAddToObjectComponent.get(ctx).inject(this)
        }
    }

    override fun releaseDependencies() {
        if (isSetOrCollection) {
            componentManager().relationAddToObjectSetComponent.release(ctx)
        } else {
            componentManager().relationAddToObjectComponent.release(ctx)
        }
    }

    companion object {

        private const val IS_SET_OR_COLLECTION_KEY = "arg.relation-add-to-object.is-set-or-collection"

        fun new(
            ctx: Id,
            isSetOrCollection: Boolean = true
        ) = RelationAddToObjectFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                IS_SET_OR_COLLECTION_KEY to isSetOrCollection
            )
        }
    }
}

class RelationAddToDataViewFragment : RelationAddBaseFragment() {

    private val dv get() = arg<Id>(DV_KEY)
    override val ctx get() = arg<Id>(CTX_KEY)

    @Inject
    lateinit var factory: RelationAddToDataViewViewModel.Factory
    override val vm: RelationAddToDataViewViewModel by viewModels { factory }

    override fun onRelationSelected(ctx: Id, relation: Key, format: RelationFormat) {
        vm.onRelationSelected(
            ctx = ctx,
            relation = relation,
            format = format,
            dv = dv,
            screenType = EventsDictionary.Type.dataView
        )
    }

    override fun onCreateFromScratchClicked() {
        val fr = RelationCreateFromScratchForDataViewFragment.new(
            ctx = ctx,
            dv = dv,
            query = createFromScratchAdapter.query
        )
        fr.showChildFragment()
    }

    override fun injectDependencies() {
        componentManager().relationAddToDataViewComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().relationAddToDataViewComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, dv: Id, viewer: Id): RelationAddToDataViewFragment =
            RelationAddToDataViewFragment().apply {
                arguments = bundleOf(
                    CTX_KEY to ctx,
                    DV_KEY to dv,
                    VIEWER_KEY to viewer
                )
            }

        private const val DV_KEY = "arg.relation-add-to-data-view.dv"
        private const val VIEWER_KEY = "arg.relation-add-to-data-view.viewer"
    }
}

class RelationAddToObjectBlockFragment : RelationAddBaseFragment(),
    OnCreateFromScratchRelationListener {

    override val ctx get() = arg<Id>(CTX_KEY)
    private val target get() = arg<Id>(TARGET_KEY)

    @Inject
    lateinit var factory: RelationAddToObjectViewModel.Factory
    override val vm: RelationAddToObjectViewModel by viewModels { factory }

    override fun onStart() {
        with(lifecycleScope) {
            jobs += subscribe(vm.commands) { execute(it) }
        }
        super.onStart()
    }

    override fun onRelationSelected(ctx: Id, relation: Key, format: RelationFormat) {
        vm.onRelationSelected(
            ctx = ctx,
            relation = relation,
            format = format,
            screenType = EventsDictionary.Type.block
        )
    }

    private fun execute(command: RelationAddToObjectViewModel.Command) {
        when (command) {
            is RelationAddToObjectViewModel.Command.OnRelationAdd -> {
                withParent<OnFragmentInteractionListener> {
                    onAddRelationToTarget(
                        target = target,
                        relationKey = command.relation
                    )
                }
                dismiss()
            }
        }
    }

    override fun onCreateFromScratchClicked() {
        val fr = RelationCreateFromScratchForObjectBlockFragment.newInstance(
            ctx = ctx,
            target = target,
            query = createFromScratchAdapter.query
        )
        fr.showChildFragment()
    }

    override fun onCreateRelation(target: Id, relation: Key) {
        withParent<OnFragmentInteractionListener> {
            onAddRelationToTarget(
                target = target,
                relationKey = relation
            )
        }
        dismiss()
    }

    override fun injectDependencies() {
        componentManager().relationAddToObjectComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().relationAddToObjectComponent.release(ctx)
    }

    companion object {
        const val TARGET_KEY = "arg.relation-add-to-object-block.target"

        fun newInstance(
            ctx: Id,
            target: Id
        ): RelationAddToObjectBlockFragment = RelationAddToObjectBlockFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx, TARGET_KEY to target)
        }
    }
}