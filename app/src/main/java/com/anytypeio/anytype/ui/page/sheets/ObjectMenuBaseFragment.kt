package com.anytypeio.anytype.ui.page.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.`object`.ObjectActionAdapter
import com.anytypeio.anytype.core_ui.layout.SpacingItemDecoration
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.`object`.ObjectAction
import com.anytypeio.anytype.presentation.`object`.ObjectMenuViewModel
import com.anytypeio.anytype.presentation.`object`.ObjectMenuViewModelBase
import com.anytypeio.anytype.ui.page.cover.DocCoverSliderFragment
import com.anytypeio.anytype.ui.page.modals.ObjectIconPickerBaseFragment
import com.anytypeio.anytype.ui.relations.RelationListFragment
import kotlinx.android.synthetic.main.fragment_object_menu.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

abstract class ObjectMenuBaseFragment : BaseBottomSheetFragment() {

    protected val ctx get() = arg<String>(CTX_KEY)
    private val isProfile get() = arg<Boolean>(IS_PROFILE_KEY)
    private val isArchived get() = arg<Boolean>(IS_ARCHIVED)

    abstract val vm : ObjectMenuViewModelBase

    private val actionAdapter by lazy {
        ObjectActionAdapter { action ->
            if (action == ObjectAction.SEARCH_ON_PAGE) {
                // Temp. workaround. Action should be dispatched via vm dispatcher.
                withParent<DocumentMenuActionReceiver> {
                    onSearchOnPageClicked()
                    dismiss()
                }
            } else {
                vm.onActionClick(ctx, action)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_object_menu, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        optionHistory
            .clicks()
            .onEach { vm.onHistoryClicked() }
            .launchIn(lifecycleScope)

        optionLayout
            .clicks()
            .onEach { vm.onLayoutClicked() }
            .launchIn(lifecycleScope)

        optionIcon
            .clicks()
            .onEach { vm.onIconClicked() }
            .launchIn(lifecycleScope)

        optionRelations
            .clicks()
            .onEach { vm.onRelationsClicked() }
            .launchIn(lifecycleScope)

        optionCover
            .clicks()
            .onEach { vm.onCoverClicked() }
            .launchIn(lifecycleScope)

        rvActions.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = actionAdapter
            addItemDecoration(
                SpacingItemDecoration(
                    spacingStart = resources.getDimension(R.dimen.dp_20).toInt(),
                    firstItemSpacingStart = resources.getDimension(R.dimen.dp_16).toInt()
                )
            )
        }
    }

    override fun onStart() {
        with(lifecycleScope) {
            subscribe(vm.actions) { actionAdapter.submitList(it) }
            subscribe(vm.toasts) { toast(it) }
            subscribe(vm.isDismissed) { isDismissed -> if (isDismissed) dismiss() }
            subscribe(vm.commands) { command -> execute(command) }
        }
        super.onStart()
        vm.onStart(
            ctx = ctx,
            isArchived = isArchived,
            isProfile = isProfile
        )
    }

    private fun execute(command: ObjectMenuViewModelBase.Command) {
        when (command) {
            ObjectMenuViewModelBase.Command.OpenObjectCover -> {
                findNavController().navigate(
                    R.id.objectCoverScreen,
                    bundleOf(DocCoverSliderFragment.CTX_KEY to ctx)
                )
            }
            ObjectMenuViewModelBase.Command.OpenObjectIcons -> {
                findNavController().navigate(
                    R.id.objectIconPickerScreen,
                    bundleOf(
                        ObjectIconPickerBaseFragment.ARG_CONTEXT_ID_KEY to ctx,
                        ObjectIconPickerBaseFragment.ARG_TARGET_ID_KEY to ctx,
                    )
                )
            }
            ObjectMenuViewModelBase.Command.OpenObjectLayout -> {
                toast(COMING_SOON_MSG)
            }
            ObjectMenuViewModelBase.Command.OpenObjectRelations -> {
                findNavController().navigate(
                    R.id.objectRelationListScreen,
                    bundleOf(
                        RelationListFragment.ARG_CTX to ctx,
                        RelationListFragment.ARG_TARGET to null,
                        RelationListFragment.ARG_MODE to RelationListFragment.MODE_LIST
                    )
                )
            }
            ObjectMenuViewModelBase.Command.OpenSetCover -> {
                toast(COMING_SOON_MSG)
            }
            ObjectMenuViewModelBase.Command.OpenSetIcons -> {
                findNavController().navigate(
                    R.id.objectSetIconPickerScreen,
                    bundleOf(
                        ObjectIconPickerBaseFragment.ARG_CONTEXT_ID_KEY to ctx,
                        ObjectIconPickerBaseFragment.ARG_TARGET_ID_KEY to ctx,
                    )
                )
            }
            ObjectMenuViewModelBase.Command.OpenSetLayout -> {
                toast(COMING_SOON_MSG)
            }
            ObjectMenuViewModelBase.Command.OpenSetRelations -> {
                toast(COMING_SOON_MSG)
            }
            ObjectMenuViewModelBase.Command.HideSetsLogic -> {
                optionLayout.gone()
                divider3.gone()
                optionRelations.gone()
                divider4.gone()
            }
        }
    }

    companion object {
        const val CTX_KEY = "arg.doc-menu-bottom-sheet.ctx"
        const val IS_ARCHIVED = "arg.doc-menu-bottom-sheet.is-archived"
        const val IS_PROFILE_KEY = "arg.doc-menu-bottom-sheet.is-profile"
        const val COMING_SOON_MSG = "Coming soon..."
    }

    interface DocumentMenuActionReceiver {
        fun onArchiveClicked()
        fun onRestoreFromArchiveClicked()
        fun onSearchOnPageClicked()
        fun onDocRelationsClicked()
        fun onAddCoverClicked()
        fun onSetIconClicked()
        fun onLayoutClicked()
        fun onDownloadClicked()
    }
}

class ObjectMenuFragment : ObjectMenuBaseFragment() {

    @Inject
    lateinit var factory: ObjectMenuViewModel.Factory
    override val vm by viewModels<ObjectMenuViewModel> { factory }

    override fun injectDependencies() {
        componentManager().objectMenuComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectMenuComponent.release(ctx)
    }

    companion object {
        fun new(
            ctx: Id,
            isProfile: Boolean = false,
            isArchived: Boolean
        ) = ObjectMenuFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                IS_ARCHIVED to isArchived,
                IS_PROFILE_KEY to isProfile
            )
        }
    }
}