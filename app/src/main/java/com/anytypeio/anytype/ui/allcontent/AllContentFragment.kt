package com.anytypeio.anytype.ui.allcontent

import android.os.Build
import com.anytypeio.anytype.core_utils.intents.ActivityCustomTabsHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
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
import com.anytypeio.anytype.presentation.navigation.NavPanelState
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.home.WidgetsScreenFragment
import com.anytypeio.anytype.ui.multiplayer.ShareSpaceFragment
import com.anytypeio.anytype.ui.objects.creation.ObjectTypeSelectionFragment
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectTypeSelectionListener
import com.anytypeio.anytype.ui.profile.ParticipantFragment
import com.anytypeio.anytype.ui.search.GlobalSearchFragment
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import timber.log.Timber

class AllContentFragment : BaseComposeFragment(), ObjectTypeSelectionListener {

    @Inject
    lateinit var factory: AllContentViewModelFactory

    private val vm by viewModels<AllContentViewModel> { factory }

    private val space get() = argString(ARG_SPACE)

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
                is AllContentViewModel.Command.ExitToSpaceHome -> {
                    navigation().exitToSpaceHome()
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
                is AllContentViewModel.Command.NavigateToObjectType -> {
                    runCatching {
                        navigation().openObjectType(
                            objectId = command.id,
                            space = command.space
                        )
                    }.onFailure {
                        Timber.e(it, "Failed to open object type object from all content")
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
                        navigation().openObjectType(
                            objectId = command.item.id,
                            space = space
                        )
                    }.onFailure {
                        toast("Failed to open type editing screen")
                        Timber.e(it, "Failed to open type editing screen from all content")
                    }
                }

                is AllContentViewModel.Command.OpenTypeCreation -> {
                    runCatching {
                        navigation().openCreateObjectTypeScreen(spaceId = command.space)
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
                        toast("Failed to open property creation screen")
                        Timber.e(it, "Failed to open property creation screen from all content")
                    }
                }

                is AllContentViewModel.Command.OpenRelationEditing -> {
                    //todo: implement new screen logic
                }
                is AllContentViewModel.Command.NavigateToDateObject -> {
                    runCatching {
                        navigation().openDateObject(
                            objectId = command.objectId,
                            space = command.space
                        )
                    }.onFailure { e ->
                        Timber.e(e, "Error while opening date object from All Objects screen")
                    }
                }
                is AllContentViewModel.Command.OpenShareScreen -> {
                    runCatching {
                        findNavController().navigate(
                            R.id.shareSpaceScreen,
                            args = ShareSpaceFragment.args(command.space)
                        )
                    }.onFailure { e ->
                        Timber.e(e, "Error while opening date object from All Objects screen")
                    }
                }

                is AllContentViewModel.Command.NavigateToParticipant -> {
                    runCatching {
                        findNavController().navigate(
                            R.id.participantScreen,
                            ParticipantFragment.args(
                                objectId = command.objectId,
                                space = command.space
                            )
                        )
                    }.onFailure {
                        Timber.w("Error while opening participant screen")
                    }
                }
                is AllContentViewModel.Command.OpenUrl -> {
                    try {
                        ActivityCustomTabsHelper.openUrl(
                            activity = requireActivity(),
                            url = command.url
                        )
                    } catch (e: Throwable) {
                        Timber.e(e, "Error opening bookmark URL: ${command.url}")
                        toast("Failed to open URL")
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
                    onOpenAsObject = vm::onOpenAsObject,
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
                        // Currently not used.
                        runCatching {
                            findNavController().navigate(
                                R.id.actionExitToSpaceWidgets,
                                WidgetsScreenFragment.args(space = space)
                            )
                        }.onFailure {
                            Timber.e(it, "Error while opening space switcher from all-content screen")
                        }
                    },
                    onRelationClicked = vm::onRelationClicked,
                    undoMoveToBin = vm::proceedWithUndoMoveToBin,
                    onDismissSnackbar = vm::proceedWithDismissSnackbar,
                    uiBottomMenu = vm.navPanelState.collectAsStateWithLifecycle(NavPanelState.Init).value,
                    onShareButtonClicked = vm::onMemberButtonClicked,
                    onHomeButtonClicked = vm::onHomeClicked
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
        if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
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