package com.anytypeio.anytype.ui.editor.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.objects.ObjectActionAdapter
import com.anytypeio.anytype.core_ui.layout.SpacingItemDecoration
import com.anytypeio.anytype.core_ui.reactive.click
import com.anytypeio.anytype.core_ui.reactive.proceed
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.shareFile
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.core_utils.ui.showActionableSnackBar
import com.anytypeio.anytype.databinding.FragmentObjectMenuBinding
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.menu.ObjectMenuOptionsProvider
import com.anytypeio.anytype.presentation.objects.menu.ObjectMenuViewModelBase
import com.anytypeio.anytype.ui.editor.cover.SelectCoverObjectFragment
import com.anytypeio.anytype.ui.editor.cover.SelectCoverObjectSetFragment
import com.anytypeio.anytype.ui.editor.layout.ObjectLayoutFragment
import com.anytypeio.anytype.ui.editor.modals.IconPickerFragmentBase
import com.anytypeio.anytype.ui.moving.MoveToFragment
import com.anytypeio.anytype.ui.moving.OnMoveToAction
import com.anytypeio.anytype.ui.relations.RelationListFragment

abstract class ObjectMenuBaseFragment :
    BaseBottomSheetFragment<FragmentObjectMenuBinding>(),
    OnMoveToAction {

    protected val ctx get() = arg<Id>(CTX_KEY)
    private val isProfile get() = arg<Boolean>(IS_PROFILE_KEY)
    private val isArchived get() = arg<Boolean>(IS_ARCHIVED_KEY)
    private val isFavorite get() = arg<Boolean>(IS_FAVORITE_KEY)
    private val isLocked get() = arg<Boolean>(IS_LOCKED_KEY)
    private val fromName get() = arg<String?>(FROM_NAME)

    abstract val vm: ObjectMenuViewModelBase

    private val actionAdapter by lazy {
        ObjectActionAdapter { action ->
            vm.onActionClicked(ctx, action)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        click(binding.objectDiagnostics) { vm.onDiagnosticsClicked(ctx) }
        click(binding.optionHistory) { vm.onHistoryClicked() }
        click(binding.optionLayout) { vm.onLayoutClicked(ctx) }
        click(binding.optionIcon) { vm.onIconClicked(ctx) }
        click(binding.optionRelations) { vm.onRelationsClicked() }
        click(binding.optionCover) { vm.onCoverClicked(ctx) }

        binding.rvActions.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = actionAdapter
            addItemDecoration(
                SpacingItemDecoration(
                    firstItemSpacingStart = resources.getDimension(R.dimen.dp_8).toInt(),
                    lastItemSpacingEnd = resources.getDimension(R.dimen.dp_8).toInt()
                )
            )
        }
    }

    override fun onStart() {
        proceed(vm.actions) { actionAdapter.submitList(it) }
        proceed(vm.toasts) { toast(it) }
        proceed(vm.isDismissed) { isDismissed -> if (isDismissed) dismiss() }
        proceed(vm.commands) { command -> execute(command) }
        proceed(vm.options) { options -> renderOptions(options) }

        super.onStart()
        vm.onStart(
            ctx = ctx,
            isArchived = isArchived,
            isFavorite = isFavorite,
            isProfile = isProfile,
            isLocked = isLocked
        )
    }

    // TODO refactor to recycler view
    private fun renderOptions(options: ObjectMenuOptionsProvider.Options) {
        val iconVisibility = options.hasIcon.toVisibility()
        val coverVisibility = options.hasCover.toVisibility()
        val layoutVisibility = options.hasLayout.toVisibility()
        val relationsVisibility = options.hasRelations.toVisibility()
        val historyVisibility = options.hasHistory.toVisibility()
        val objectDiagnosticsVisibility = options.hasDiagnosticsVisibility.toVisibility()

        binding.optionIcon.visibility = iconVisibility
        binding.optionCover.visibility = coverVisibility
        binding.optionLayout.visibility = layoutVisibility
        binding.optionRelations.visibility = relationsVisibility
        binding.optionHistory.visibility = historyVisibility
        binding.iconDivider.visibility = iconVisibility
        binding.coverDivider.visibility = coverVisibility
        binding.layoutDivider.visibility = layoutVisibility
        binding.relationsDivider.visibility = relationsVisibility
        binding.historyDivider.visibility = historyVisibility
        binding.objectDiagnostics.visibility = objectDiagnosticsVisibility
        binding.objectDiagnosticsDivider.visibility = objectDiagnosticsVisibility
    }

    private fun execute(command: ObjectMenuViewModelBase.Command) {
        when (command) {
            ObjectMenuViewModelBase.Command.OpenObjectCover -> openObjectCover()
            ObjectMenuViewModelBase.Command.OpenObjectIcons -> openObjectIcons()
            ObjectMenuViewModelBase.Command.OpenObjectLayout -> openObjectLayout()
            ObjectMenuViewModelBase.Command.OpenObjectRelations -> openObjectRelations()
            ObjectMenuViewModelBase.Command.OpenSetCover -> openSetCover()
            ObjectMenuViewModelBase.Command.OpenSetIcons -> openSetIcons()
            ObjectMenuViewModelBase.Command.OpenSetLayout -> toast(COMING_SOON_MSG)
            ObjectMenuViewModelBase.Command.OpenSetRelations -> toast(COMING_SOON_MSG)
            ObjectMenuViewModelBase.Command.OpenLinkToChooser -> openLinkChooser(command)
            is ObjectMenuViewModelBase.Command.OpenSnackbar -> openSnackbar(command)
            is ObjectMenuViewModelBase.Command.ShareDebugTree -> shareFile(command.uri)
        }
    }

    private fun openObjectCover() {
        findNavController().navigate(
            R.id.objectCoverScreen,
            bundleOf(SelectCoverObjectFragment.CTX_KEY to ctx)
        )
    }

    private fun openObjectIcons() {
        findNavController().navigate(
            R.id.objectIconPickerScreen,
            bundleOf(
                IconPickerFragmentBase.ARG_CONTEXT_ID_KEY to ctx,
            )
        )
    }

    private fun openObjectLayout() {
        val fr = ObjectLayoutFragment.new(ctx)
        fr.show(childFragmentManager, null)
    }

    private fun openObjectRelations() {
        findNavController().navigate(
            R.id.objectRelationListScreen,
            bundleOf(
                RelationListFragment.ARG_CTX to ctx,
                RelationListFragment.ARG_TARGET to null,
                RelationListFragment.ARG_LOCKED to isLocked,
                RelationListFragment.ARG_MODE to RelationListFragment.MODE_LIST
            )
        )
    }

    private fun openSetCover() {
        findNavController().navigate(
            R.id.objectSetCoverScreen,
            bundleOf(SelectCoverObjectSetFragment.CTX_KEY to ctx)
        )
    }


    private fun openSetIcons() {
        findNavController().navigate(
            R.id.objectSetIconPickerScreen,
            bundleOf(
                IconPickerFragmentBase.ARG_CONTEXT_ID_KEY to ctx,
            )
        )
    }


    private fun openLinkChooser(command: ObjectMenuViewModelBase.Command) {
        val fr = MoveToFragment.new(
            ctx = ctx,
            blocks = emptyList(),
            restorePosition = null,
            restoreBlock = null,
            title = getString(R.string.link_to)
        )
        fr.show(childFragmentManager, null)
    }

    private fun openSnackbar(command: ObjectMenuViewModelBase.Command.OpenSnackbar) {
        binding.root.postDelayed({
            dialog?.window
                ?.decorView
                ?.showActionableSnackBar(
                    command.currentObjectName,
                    command.targetObjectName,
                    command.icon,
                    R.string.snack_link_to,
                    binding.anchor
                ) {
                    vm.proceedWithOpeningPage(command.id)
                }
        }, 300L)
    }


    override fun onMoveTo(
        target: Id,
        blocks: List<Id>,
        text: String,
        icon: ObjectIcon,
        isSet: Boolean
    ) {
        vm.onLinkedMyselfTo(myself = ctx, addTo = target, fromName)
    }

    override fun onMoveToClose(blocks: List<Id>, restorePosition: Int?, restoreBlock: Id?) {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentObjectMenuBinding = FragmentObjectMenuBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val CTX_KEY = "arg.doc-menu-bottom-sheet.ctx"
        const val IS_ARCHIVED_KEY = "arg.doc-menu-bottom-sheet.is-archived"
        const val IS_PROFILE_KEY = "arg.doc-menu-bottom-sheet.is-profile"
        const val IS_FAVORITE_KEY = "arg.doc-menu-bottom-sheet.is-favorite"
        const val IS_LOCKED_KEY = "arg.doc-menu-bottom-sheet.is-locked"
        const val FROM_NAME = "arg.doc-menu-bottom-sheet.from-name"
        const val COMING_SOON_MSG = "Coming soon..."
    }

    interface DocumentMenuActionReceiver {
        fun onMoveToBinSuccess()
        fun onSearchOnPageClicked()
        fun onDocRelationsClicked()
        fun onAddCoverClicked()
        fun onSetIconClicked()
        fun onLayoutClicked()
        fun onUndoRedoClicked()
    }
}

private fun Boolean.toVisibility() = if (this) View.VISIBLE else View.GONE