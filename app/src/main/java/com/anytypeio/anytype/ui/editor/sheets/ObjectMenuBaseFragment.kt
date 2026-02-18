package com.anytypeio.anytype.ui.editor.sheets

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.BuildConfig
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_ui.features.objects.ObjectActionAdapter
import com.anytypeio.anytype.core_ui.layout.SpacingItemDecoration
import com.anytypeio.anytype.core_ui.reactive.click
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.shareFile
import com.anytypeio.anytype.core_utils.ext.throttleFirst
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.core_utils.ui.proceed
import com.anytypeio.anytype.core_utils.ui.showActionableSnackBar
import com.anytypeio.anytype.databinding.FragmentObjectMenuBinding
import com.anytypeio.anytype.feature_object_type.ui.conflict.ConflictScreen
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.menu.ObjectMenuOptionsProvider
import com.anytypeio.anytype.presentation.objects.menu.ObjectMenuViewModelBase
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.editor.cover.SelectCoverObjectFragment
import com.anytypeio.anytype.ui.editor.cover.SelectCoverObjectSetFragment
import com.anytypeio.anytype.ui.editor.modals.IconPickerFragmentBase
import com.anytypeio.anytype.ui.history.VersionHistoryFragment
import com.anytypeio.anytype.ui.linking.BacklinkAction
import com.anytypeio.anytype.ui.linking.BacklinkOrAddToObjectFragment
import com.anytypeio.anytype.ui.moving.OnMoveToAction
import com.anytypeio.anytype.ui.primitives.ObjectFieldsFragment
import com.anytypeio.anytype.ui.publishtoweb.PublishToWebFragment
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

abstract class ObjectMenuBaseFragment :
    BaseBottomSheetFragment<FragmentObjectMenuBinding>(),
    OnMoveToAction,
    BacklinkAction {

    protected val ctx get() = arg<Id>(CTX_KEY)
    protected val space get() = arg<Id>(SPACE_KEY)
    private val isArchived get() = arg<Boolean>(IS_ARCHIVED_KEY)
    private val isFavorite get() = arg<Boolean>(IS_FAVORITE_KEY)
    private val isLocked get() = arg<Boolean>(IS_LOCKED_KEY)
    private val isReadOnly get() = arg<Boolean>(IS_READ_ONLY_KEY)
    private val isTemplate get() = argOrNull<Boolean>(IS_TEMPLATE_KEY)
    private val fromName get() = argOrNull<String?>(FROM_NAME)

    abstract val vm: ObjectMenuViewModelBase

    private val actionAdapter by lazy {
        ObjectActionAdapter { action ->
            vm.onActionClicked(
                ctx = ctx,
                space = space,
                action = action
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        binding.objectLayoutConflictScreen.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ConflictScreen(
                    showScreen = vm.showLayoutConflictScreen.collectAsStateWithLifecycle().value,
                    onResetClick = {
                        vm.onResetToDefaultLayout(
                            ctx = ctx,
                            space = space
                        )
                    },
                    onDismiss = { vm.onHideConflictScreen() }
                )
            }
        }
    }

    override fun onStart() {
        click(binding.objectDiagnostics) { vm.onDiagnosticsClicked(ctx = ctx) }
        click(binding.optionHistory) { vm.onHistoryClicked(ctx = ctx, space = space) }
        click(binding.optionDescription) { vm.onDescriptionClicked(ctx = ctx, space = space) }
        click(binding.optionIcon) { vm.onIconClicked(ctx = ctx, space = space) }
        click(binding.optionRelations) { vm.onRelationsClicked() }
        click(binding.optionCover) { vm.onCoverClicked(ctx = ctx, space = space) }
        click(binding.publishToWeb) { vm.onPublishToWebClicked(ctx = ctx, space = space) }
        click(binding.debugGoroutines) { vm.onDiagnosticsGoroutinesClicked(ctx = ctx) }
        click(binding.objectLayoutConflict) { vm.onShowConflictScreen(objectId = ctx, space = space) }
        binding.optionTemplateNamePrefill.setOnCheckedChangeListener {
            vm.onTemplateNamePrefillToggleClicked(ctx = ctx, space = space)
        }

        proceed(vm.actions) { actionAdapter.submitList(it) }
        proceed(vm.toasts) { toast(it) }
        proceed(vm.isDismissed) { isDismissed -> if (isDismissed) dismiss() }
        proceed(vm.commands.throttleFirst()) { command -> execute(command) }
        proceed(vm.options) { options -> renderOptions(options) }

        if (BuildConfig.DEBUG) {
            binding.debugGoroutines.visible()
            binding.debugGoroutinesDivider.visible()
        }

        super.onStart()
        vm.onStart(
            space = SpaceId(space),
            ctx = ctx,
            isArchived = isArchived,
            isFavorite = isFavorite,
            isLocked = isLocked,
            isTemplate = isTemplate == true,
            isReadOnly = isReadOnly
        )
    }

    // TODO refactor to recycler view
    private fun renderOptions(options: ObjectMenuOptionsProvider.Options) {
        val iconVisibility = options.hasIcon.toVisibility()
        val coverVisibility = options.hasCover.toVisibility()
        val relationsVisibility = options.hasRelations.toVisibility()
        val historyVisibility = options.hasHistory.toVisibility()
        val objectDiagnosticsVisibility = options.hasDiagnosticsVisibility.toVisibility()
        val objectLayoutConflictVisibility = options.hasObjectLayoutConflict.toVisibility()

        if (options.hasDescriptionShow) {
            binding.optionDescription.setAction(setAsHide = false)
        } else {
            binding.optionDescription.setAction(setAsHide = true)
        }

        binding.optionIcon.visibility = iconVisibility
        binding.optionCover.visibility = coverVisibility
        binding.optionRelations.visibility = relationsVisibility
        binding.optionHistory.visibility = historyVisibility
        binding.iconDivider.visibility = iconVisibility
        binding.coverDivider.visibility = coverVisibility
        binding.relationsDivider.visibility = relationsVisibility
        binding.historyDivider.visibility = historyVisibility
        binding.objectDiagnostics.visibility = objectDiagnosticsVisibility
        binding.objectDiagnosticsDivider.visibility = objectDiagnosticsVisibility
        binding.objectLayoutConflict.visibility = objectLayoutConflictVisibility

        // Template Name Prefill - only visible for templates with title support
        if (isTemplate == true && options.hasTemplateNamePrefill) {
            binding.optionTemplateNamePrefill.visibility = View.VISIBLE
            binding.templateNamePrefillDivider.visibility = View.VISIBLE
            binding.optionTemplateNamePrefill.setChecked(options.isTemplateNamePrefillEnabled)
        } else {
            binding.optionTemplateNamePrefill.visibility = View.GONE
            binding.templateNamePrefillDivider.visibility = View.GONE
        }
    }

    private fun execute(command: ObjectMenuViewModelBase.Command) {
        when (command) {
            ObjectMenuViewModelBase.Command.OpenObjectCover -> openObjectCover()
            ObjectMenuViewModelBase.Command.OpenObjectIcons -> openObjectIcons()
            ObjectMenuViewModelBase.Command.OpenObjectRelations -> openObjectRelations()
            ObjectMenuViewModelBase.Command.OpenSetCover -> openSetCover()
            ObjectMenuViewModelBase.Command.OpenSetIcons -> openSetIcons()
            ObjectMenuViewModelBase.Command.OpenSetLayout -> toast(COMING_SOON_MSG)
            ObjectMenuViewModelBase.Command.OpenSetRelations -> openDataViewRelations()
            ObjectMenuViewModelBase.Command.OpenLinkToChooser -> openLinkChooser()
            is ObjectMenuViewModelBase.Command.OpenSnackbar -> openSnackbar(command)
            is ObjectMenuViewModelBase.Command.ShareDebugTree -> shareFile(command.uri)
            is ObjectMenuViewModelBase.Command.OpenTemplate -> openTemplate(command)
            is ObjectMenuViewModelBase.Command.ShareDebugGoroutines -> {
                val snackbar = Snackbar.make(
                    dialog?.window?.decorView!!,
                    "Success, Path: ${command.path}",
                    Snackbar.LENGTH_INDEFINITE
                )
                snackbar.setAction("Done") {
                    snackbar.dismiss()
                }
                snackbar.anchorView = binding.anchor
                snackbar.show()
            }

            is ObjectMenuViewModelBase.Command.ShareDeeplinkToObject -> {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, command.link)
                    putExtra(
                        Intent.EXTRA_TITLE,
                        getString(R.string.multiplayer_deeplink_to_your_object)
                    )
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(intent, null))
            }

            is ObjectMenuViewModelBase.Command.OpenHistoryScreen -> {
                runCatching {
                    findNavController().navigate(
                        R.id.versionHistoryScreen,
                        VersionHistoryFragment.args(
                            ctx = ctx,
                            spaceId = space
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Failed to open history screen")
                }
            }
            is ObjectMenuViewModelBase.Command.PublishToWeb -> {
                runCatching {
                    findNavController().navigate(
                        R.id.publishToWebScreen,
                        PublishToWebFragment.args(
                            ctx = ctx,
                            space = space
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Failed to open publish-to-web screen")
                }
            }
        }
    }

    private fun openTemplate(command: ObjectMenuViewModelBase.Command.OpenTemplate) {
        val msg = "${getString(R.string.snackbar_template_add)} ${command.typeName}"
        toast(msg)
        navigation().openModalTemplateEdit(
            template = command.templateId,
            templateTypeId = command.typeId,
            templateTypeKey = command.typeKey,
            space = command.space
        )
    }

    private fun openObjectCover() {
        findNavController().navigate(
            R.id.objectCoverScreen,
            SelectCoverObjectFragment.args(
                ctx = ctx,
                space = space
            )
        )
    }

    private fun openObjectIcons() {
        findNavController().navigate(
            R.id.objectIconPickerScreen,
            bundleOf(
                IconPickerFragmentBase.ARG_CONTEXT_ID_KEY to ctx,
                IconPickerFragmentBase.ARG_SPACE_ID_KEY to space
            )
        )
    }

    private fun openObjectRelations() {
        findNavController().navigate(
            R.id.objectRelationListScreen,
            bundleOf(
                ObjectFieldsFragment.ARG_CTX to ctx,
                ObjectFieldsFragment.ARG_SPACE to space,
                ObjectFieldsFragment.ARG_TARGET to null,
                ObjectFieldsFragment.ARG_LOCKED to isLocked,
                ObjectFieldsFragment.ARG_SET_FLOW to false
            )
        )
    }

    private fun openDataViewRelations() {
        findNavController().navigate(
            R.id.objectRelationListScreen,
            bundleOf(
                ObjectFieldsFragment.ARG_CTX to ctx,
                ObjectFieldsFragment.ARG_SPACE to space,
                ObjectFieldsFragment.ARG_TARGET to null,
                ObjectFieldsFragment.ARG_LOCKED to isLocked,
                ObjectFieldsFragment.ARG_SET_FLOW to true
            )
        )
    }

    private fun openSetCover() {
        findNavController().navigate(
            R.id.objectSetCoverScreen,
            SelectCoverObjectSetFragment.args(
                ctx = ctx,
                space = space
            )
        )
    }


    private fun openSetIcons() {
        findNavController().navigate(
            R.id.objectSetIconPickerScreen,
            bundleOf(
                IconPickerFragmentBase.ARG_CONTEXT_ID_KEY to ctx,
                IconPickerFragmentBase.ARG_SPACE_ID_KEY to space
            )
        )
    }


    private fun openLinkChooser() {
        val fr = BacklinkOrAddToObjectFragment().apply {
            arguments = BacklinkOrAddToObjectFragment.args(
                ctx = ctx,
                space = space
            )
        }
        fr.showChildFragment()
    }

    private fun openSnackbar(command: ObjectMenuViewModelBase.Command.OpenSnackbar) {
        binding.root.postDelayed({
            dialog?.window
                ?.decorView
                ?.showActionableSnackBar(
                    from = command.currentObjectName,
                    to = command.targetObjectName,
                    icon = command.icon,
                    middleString = R.string.snack_link_to,
                    anchor = binding.anchor
                ) {
                    if (command.isCollection) {
                        vm.proceedWithOpeningCollection(
                            target = command.id,
                            space = command.space
                        )
                    } else {
                        vm.proceedWithOpeningPage(
                            target = command.id,
                            space = command.space
                        )
                    }
                }
        }, 300L)
    }


    override fun onMoveTo(
        target: Id,
        space: Id,
        blocks: List<Id>,
        text: String,
        icon: ObjectIcon,
        isDataView: Boolean
    ) {
        vm.onLinkedMyselfTo(
            myself = ctx,
            addTo = target,
            fromName = fromName,
            space = space
        )
    }

    override fun backLink(
        id: Id,
        name: String,
        layout: ObjectType.Layout?,
        icon: ObjectIcon
    ) {
        vm.onBackLinkOrAddToObjectAction(
            ctx = ctx,
            backLinkId = id,
            backLinkName = name,
            backLinkLayout = layout,
            backLinkIcon = icon,
            fromName = fromName.orEmpty(),
            space = space
        )
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
        const val SPACE_KEY = "arg.doc-menu-bottom-sheet.space"
        const val IS_ARCHIVED_KEY = "arg.doc-menu-bottom-sheet.is-archived"
        const val IS_FAVORITE_KEY = "arg.doc-menu-bottom-sheet.is-favorite"
        const val IS_LOCKED_KEY = "arg.doc-menu-bottom-sheet.is-locked"
        const val IS_READ_ONLY_KEY = "arg.doc-menu-bottom-sheet.is-read-only"
        const val FROM_NAME = "arg.doc-menu-bottom-sheet.from-name"
        const val COMING_SOON_MSG = "Coming soon..."
        const val IS_TEMPLATE_KEY = "arg.doc-menu-bottom-sheet.is-template"
    }

    interface DocumentMenuActionReceiver {
        fun onSearchOnPageClicked()
        fun onDocRelationsClicked()
        fun onAddCoverClicked()
        fun onSetIconClicked()
        fun onUndoRedoClicked()
    }
}

private fun Boolean.toVisibility() = if (this) View.VISIBLE else View.GONE