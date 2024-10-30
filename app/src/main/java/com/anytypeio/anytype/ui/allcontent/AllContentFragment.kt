package com.anytypeio.anytype.ui.allcontent

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModel
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModelFactory
import com.anytypeio.anytype.feature_allcontent.ui.AllContentNavigation.ALL_CONTENT_MAIN
import com.anytypeio.anytype.feature_allcontent.ui.AllContentWrapperScreen
import com.anytypeio.anytype.presentation.library.LibraryViewModel
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.objects.creation.ObjectTypeSelectionFragment
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectTypeSelectionListener
import com.anytypeio.anytype.ui.relations.REQUEST_KEY_MODIFY_RELATION
import com.anytypeio.anytype.ui.relations.REQUEST_KEY_UNINSTALL_RELATION
import com.anytypeio.anytype.ui.relations.REQUEST_UNINSTALL_RELATION_ARG_ID
import com.anytypeio.anytype.ui.relations.REQUEST_UNINSTALL_RELATION_ARG_NAME
import com.anytypeio.anytype.ui.search.GlobalSearchFragment
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.types.edit.REQUEST_KEY_MODIFY_TYPE
import com.anytypeio.anytype.ui.types.edit.REQUEST_KEY_UNINSTALL_TYPE
import com.anytypeio.anytype.ui.types.edit.REQUEST_UNINSTALL_TYPE_ARG_ICON
import com.anytypeio.anytype.ui.types.edit.REQUEST_UNINSTALL_TYPE_ARG_ID
import com.anytypeio.anytype.ui.types.edit.REQUEST_UNINSTALL_TYPE_ARG_NAME
import javax.inject.Inject
import timber.log.Timber

class AllContentFragment : BaseComposeFragment(), ObjectTypeSelectionListener {

    @Inject
    lateinit var factory: AllContentViewModelFactory

    private val vm by viewModels<AllContentViewModel> { factory }

    private val space get() = argString(ARG_SPACE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(REQUEST_KEY_UNINSTALL_TYPE) { _, bundle ->
            val id = requireNotNull(bundle.getString(REQUEST_UNINSTALL_TYPE_ARG_ID))
            val name = requireNotNull(bundle.getString(REQUEST_UNINSTALL_TYPE_ARG_NAME))
            vm.uninstallObject(id, LibraryViewModel.LibraryItem.TYPE, name)
        }
        setFragmentResultListener(REQUEST_KEY_MODIFY_TYPE) { _, bundle ->
            val id = requireNotNull(bundle.getString(REQUEST_UNINSTALL_TYPE_ARG_ID))
            val name = requireNotNull(bundle.getString(REQUEST_UNINSTALL_TYPE_ARG_NAME))
            val icon = requireNotNull(bundle.getString(REQUEST_UNINSTALL_TYPE_ARG_ICON))
            vm.updateObject(id, name, icon)
        }
        setFragmentResultListener(REQUEST_KEY_UNINSTALL_RELATION) { _, bundle ->
            val id = requireNotNull(bundle.getString(REQUEST_UNINSTALL_RELATION_ARG_ID))
            val name = requireNotNull(bundle.getString(REQUEST_UNINSTALL_RELATION_ARG_NAME))
            vm.uninstallObject(id, LibraryViewModel.LibraryItem.RELATION, name)
        }
        setFragmentResultListener(REQUEST_KEY_MODIFY_RELATION) { _, bundle ->
            val id = requireNotNull(bundle.getString(REQUEST_UNINSTALL_RELATION_ARG_ID))
            val name = requireNotNull(bundle.getString(REQUEST_UNINSTALL_RELATION_ARG_NAME))
            vm.updateObject(
                id = id,
                name = name,
                icon = null
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme(
            typography = typography
        ) {
            AllContentScreenWrapper()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribe(vm.commands) { command ->
            when (command) {
                is AllContentViewModel.Command.ExitToVault -> {
                    runCatching {
                        findNavController().navigate(R.id.actionOpenVault)
                    }.onFailure { e ->
                        Timber.e(e, "Error while exiting to vault from all content")
                    }
                }

                is AllContentViewModel.Command.Back -> {
                    runCatching {
                        findNavController().popBackStack()
                    }.onFailure { e ->
                        Timber.e(e, "Error while exiting back from all content")
                    }
                }

                is AllContentViewModel.Command.OpenGlobalSearch -> {
                    runCatching {
                        findNavController().navigate(
                            resId = R.id.globalSearchScreen,
                            args = GlobalSearchFragment.args(
                                space = space
                            )
                        )
                    }.onFailure { e ->
                        Timber.e(e, "Error while opening global search screen from all content")
                    }
                }

                is AllContentViewModel.Command.NavigateToEditor -> {
                    runCatching {
                        navigation().openDocument(
                            target = command.id,
                            space = command.space
                        )
                    }.onFailure {
                        toast("Failed to open document")
                        Timber.e(it, "Failed to open document from all content")
                    }
                }
                is AllContentViewModel.Command.OpenChat -> {
                    runCatching {
                        navigation().openChat(
                            target = command.target,
                            space = command.space
                        )
                    }.onFailure {
                        Timber.e(it, "Failed to open a chat from all content")
                    }
                }

                is AllContentViewModel.Command.NavigateToSetOrCollection -> {
                    runCatching {
                        navigation().openObjectSet(
                            target = command.id,
                            space = command.space,
                        )
                    }.onFailure {
                        toast("Failed to open object set")
                        Timber.e(it, "Failed to open object set from all content")
                    }
                }

                is AllContentViewModel.Command.SendToast.UnexpectedLayout -> {
                    val message =
                        "${getString(R.string.all_content_error_unexpected_layout)}: ${command.layout}"
                    toast(message)
                }

                is AllContentViewModel.Command.SendToast.RelationRemoved -> {
                    val message =
                        "${getString(R.string.all_content_toast_relation_removed)}: ${command.name}"
                    toast(message)
                }

                is AllContentViewModel.Command.SendToast.TypeRemoved -> {
                    val message =
                        "${getString(R.string.all_content_toast_type_removed)}: ${command.name}"
                    toast(message)
                }

                is AllContentViewModel.Command.SendToast.Error -> {
                    toast(command.message)
                }

                is AllContentViewModel.Command.SendToast.ObjectArchived -> {
                    val message =
                        "${getString(R.string.all_content_toast_archived)}: ${command.name}"
                    toast(message)
                }

                is AllContentViewModel.Command.NavigateToBin -> {
                    runCatching {
                        navigation().launchCollections(
                            subscription = Subscription.Bin,
                            space = command.space
                        )
                    }.onFailure {
                        toast("Failed to open bin")
                        Timber.e(it, "Failed to open bin from all content")
                    }
                }

                is AllContentViewModel.Command.OpenTypeEditing -> {
                    runCatching {
                        navigation().openTypeEditingScreen(
                            id = command.item.id,
                            name = command.item.name,
                            icon = (command.item.icon as? ObjectIcon.Basic.Emoji)?.unicode ?: "",
                            readOnly = command.item.readOnly
                        )
                    }.onFailure {
                        toast("Failed to open type editing screen")
                        Timber.e(it, "Failed to open type editing screen from all content")
                    }
                }

                is AllContentViewModel.Command.OpenTypeCreation -> {
                    runCatching {
                        navigation().openTypeCreationScreen(
                            name = ""
                        )
                    }.onFailure {
                        toast("Failed to open type creation screen")
                        Timber.e(it, "Failed to open type creation screen from all content")
                    }
                }

                is AllContentViewModel.Command.OpenRelationCreation -> {
                    runCatching {
                        navigation().openRelationCreationScreen(
                            id = "",
                            name = "",
                            space = command.space
                        )
                    }.onFailure {
                        toast("Failed to open relation creation screen")
                        Timber.e(it, "Failed to open relation creation screen from all content")
                    }
                }

                is AllContentViewModel.Command.OpenRelationEditing -> {
                    runCatching {
                        navigation().openRelationEditingScreen(
                            typeName = command.typeName,
                            id = command.id,
                            iconUnicode = command.iconUnicode,
                            readOnly = command.readOnly
                        )
                    }.onFailure {
                        toast("Failed to open relation editing screen")
                        Timber.e(it, "Failed to open relation editing screen from all content")
                    }
                }
            }
        }
    }

    @Composable
    fun AllContentScreenWrapper() {
        NavHost(
            navController = rememberNavController(),
            startDestination = ALL_CONTENT_MAIN
        ) {
            composable(route = ALL_CONTENT_MAIN) {
                AllContentWrapperScreen(
                    uiItemsState = vm.uiItemsState.collectAsStateWithLifecycle().value,
                    onTabClick = vm::onTabClicked,
                    onQueryChanged = vm::onFilterChanged,
                    uiTabsState = vm.uiTabsState.collectAsStateWithLifecycle().value,
                    uiTitleState = vm.uiTitleState.collectAsStateWithLifecycle().value,
                    uiMenuState = vm.uiMenuState.collectAsStateWithLifecycle().value,
                    uiSnackbarState = vm.uiSnackbarState.collectAsStateWithLifecycle().value,
                    onSortClick = vm::onSortClicked,
                    onModeClick = vm::onAllContentModeClicked,
                    onItemClicked = vm::onItemClicked,
                    onBinClick = vm::onViewBinClicked,
                    canPaginate = vm.canPaginate.collectAsStateWithLifecycle().value,
                    onUpdateLimitSearch = vm::updateLimit,
                    uiContentState = vm.uiContentState.collectAsStateWithLifecycle().value,
                    onTypeClicked = vm::onTypeClicked,
                    onGlobalSearchClicked = vm::onGlobalSearchClicked,
                    onAddDocClicked = vm::onAddDockClicked,
                    onCreateObjectLongClicked = {
                        val dialog = ObjectTypeSelectionFragment.new(space = space)
                        dialog.show(childFragmentManager, null)
                    },
                    onBackClicked = vm::onBackClicked,
                    moveToBin = vm::proceedWithMoveToBin,
                    onBackLongClicked = {
                        runCatching {
                            findNavController().navigate(R.id.actionOpenSpaceSwitcher)
                        }.onFailure {
                            Timber.e(it, "Error while opening space switcher from all-content screen")
                        }
                    },
                    onRelationClicked = vm::onRelationClicked,
                    undoMoveToBin = vm::proceedWithUndoMoveToBin,
                    onDismissSnackbar = vm::proceedWithDismissSnackbar,
                    uiBottomMenu = vm.uiBottomMenu.collectAsStateWithLifecycle().value
                )
            }
        }
    }

    override fun onSelectObjectType(objType: ObjectWrapper.Type) {
        vm.onCreateObjectOfTypeClicked(objType = objType)
    }

    override fun onStart() {
        vm.onStart()
        super.onStart()
    }

    override fun onStop() {
        vm.onStop()
        super.onStop()
    }

    override fun injectDependencies() {
        val vmParams = AllContentViewModel.VmParams(spaceId = SpaceId(space))
        componentManager().allContentComponent.get(vmParams).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().allContentComponent.release()
    }

    override fun onApplyWindowRootInsets(view: View) {
        if (BuildConfig.USE_EDGE_TO_EDGE && Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
            // Do nothing.
        } else {
            super.onApplyWindowRootInsets(view)
        }
    }

    companion object {
        const val KEYBOARD_HIDE_DELAY = 300L

        const val ARG_SPACE = "arg.all.content.space"
        fun args(space: Id): Bundle = bundleOf(ARG_SPACE to space)
    }
}