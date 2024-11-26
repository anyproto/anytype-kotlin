package com.anytypeio.anytype.ui.date

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.views.BaseAlertDialog
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_date.models.UiErrorState
import com.anytypeio.anytype.feature_date.presentation.DateObjectViewModel
import com.anytypeio.anytype.feature_date.presentation.DateObjectViewModelFactory
import com.anytypeio.anytype.feature_date.ui.DateObjectScreen
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.objects.creation.ObjectTypeSelectionFragment
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectTypeSelectionListener
import com.anytypeio.anytype.ui.search.GlobalSearchFragment
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import javax.inject.Inject
import timber.log.Timber

class DateObjectFragment : BaseComposeFragment(), ObjectTypeSelectionListener {
    @Inject
    lateinit var factory: DateObjectViewModelFactory

    private val vm by viewModels<DateObjectViewModel> { factory }
    private lateinit var navComposeController: NavHostController

    private val space get() = argString(ARG_SPACE)
    private val objectId get() = argString(ARG_OBJECT_ID)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme {
            DateLayoutScreenWrapper()
            ErrorScreen()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribe(vm.commands) { command ->
            Timber.d("Received command: $command")
            when (command) {
                DateObjectViewModel.Command.Back -> {
                    runCatching {
                        findNavController().popBackStack()
                    }.onFailure { e ->
                        Timber.e(e, "Error while exiting back from all content")
                    }
                }
                DateObjectViewModel.Command.ExitToVault -> {
                    runCatching {
                        findNavController().navigate(R.id.actionOpenVault)
                    }.onFailure { e ->
                        Timber.e(e, "Error while exiting to vault from all content")
                    }
                }
                is DateObjectViewModel.Command.NavigateToEditor -> {
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
                is DateObjectViewModel.Command.NavigateToSetOrCollection -> {
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
                is DateObjectViewModel.Command.OpenChat -> {
                    runCatching {
                        navigation().openChat(
                            target = command.target,
                            space = command.space
                        )
                    }.onFailure {
                        Timber.e(it, "Failed to open a chat from all content")
                    }
                }
                DateObjectViewModel.Command.OpenGlobalSearch -> {
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
                is DateObjectViewModel.Command.SendToast.Error -> TODO()
                is DateObjectViewModel.Command.SendToast.ObjectArchived -> TODO()
                is DateObjectViewModel.Command.NavigateToDateObject -> {
                    runCatching {
                        findNavController().navigate(
                            resId = R.id.dateObjectScreen,
                            args = args(
                                objectId = command.objectId,
                                space = command.space.id
                            ),
                            navOptions = navOptions {
                                launchSingleTop = true
                            }
                        )
                    }.onFailure {
                        Timber.e(it, "Failed to navigate to date object")
                    }
                }

                DateObjectViewModel.Command.ExitToSpaceWidgets -> {
                    runCatching {
                        findNavController().navigate(R.id.actionExitToSpaceWidgets)
                    }.onFailure {
                        Timber.e(it, "Error while opening space switcher from all-content screen")
                    }
                }
                is DateObjectViewModel.Command.SendToast.UnexpectedLayout -> {
                    toast("Unexpected layout")
                }
                DateObjectViewModel.Command.TypeSelectionScreen -> {
                    val dialog = ObjectTypeSelectionFragment.new(space = space)
                    dialog.show(childFragmentManager, null)
                }
            }
        }
    }

    override fun onSelectObjectType(objType: ObjectWrapper.Type) {
        vm.onCreateObjectOfTypeClicked(objType = objType)
    }

    override fun onStart() {
        super.onStart()
        vm.onStart()
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    @OptIn(ExperimentalMaterialNavigationApi::class)
    @Composable
    fun DateLayoutScreenWrapper() {
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        navComposeController = rememberNavController(bottomSheetNavigator)
        NavHost(
            navController = navComposeController,
            startDestination = DATE_MAIN
        ) {
            composable(route = DATE_MAIN) {
                DateObjectScreen(
                    uiTopToolbarState = vm.uiTopToolbarState.collectAsStateWithLifecycle().value,
                    uiHeaderState = vm.uiHeaderState.collectAsStateWithLifecycle().value,
                    uiHorizontalListState = vm.uiHorizontalListState.collectAsStateWithLifecycle().value,
                    uiVerticalListState = vm.uiVerticalListState.collectAsStateWithLifecycle().value,
                    uiDateObjectBottomMenu = vm.uiBottomMenu.collectAsStateWithLifecycle().value,
                    uiSheetState = vm.uiSheetState.collectAsStateWithLifecycle().value,
                    uiContentState = vm.uiContentState.collectAsStateWithLifecycle().value,
                    canPaginate = vm.canPaginate.collectAsStateWithLifecycle().value,
                    uiHeaderActions = vm::onHeaderActions,
                    uiBottomMenuActions = vm::onBottomMenuAction,
                    uiTopToolbarActions = vm::onTopToolbarActions,
                    uiVerticalListActions = vm::onItemClicked,
                    uiHorizontalListActions = vm::onHorizontalItemClicked,
                    onUpdateLimitSearch = vm::updateLimit,
                    onCalendarDateSelected = vm::onCalendarDateSelected,
                    uiCalendarState = vm.uiCalendarState.collectAsStateWithLifecycle().value,
                    onTodayClicked = vm::onTodayClicked,
                    onTomorrowClicked = vm::onTomorrowClicked,
                    onDismissCalendar = vm::onDismissCalendar,
                    showCalendar = vm.showCalendar.collectAsStateWithLifecycle().value,
                    uiSyncStatusState = vm.syncStatusWidget.collectAsStateWithLifecycle().value,
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ErrorScreen() {
        val errorStateScreen = vm.errorState.collectAsStateWithLifecycle()
        when (val state = errorStateScreen.value) {
            UiErrorState.Hidden -> {
                // Do nothing
            }
            is UiErrorState.Show -> {
                val message = when (val r = state.reason) {
                    is UiErrorState.Reason.YearOutOfRange ->
                        stringResource(
                            id = R.string.date_layout_alert_date_out_of_range,
                            r.min,
                            r.max
                        )
                }
                BaseAlertDialog(
                    dialogText = message,
                    buttonText = stringResource(id = R.string.membership_error_button_text_dismiss),
                    onButtonClick = vm::hideError,
                    onDismissRequest = vm::hideError
                )
            }
        }
    }

    override fun injectDependencies() {
        val params = DateObjectViewModel.VmParams(
            spaceId = SpaceId(space),
            objectId = objectId
        )
        componentManager().dateObjectComponent.get(params).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().dateObjectComponent.release()
    }

    override fun onApplyWindowRootInsets(view: View) {
        // Do nothing. TODO add ime padding.
    }

    companion object DateLayoutNavigation {
        private const val DATE_MAIN = "date_main"
        const val ARG_SPACE = "arg.date.object.space"
        const val ARG_OBJECT_ID = "arg.date.object.object_id"

        fun args(space: Id, objectId: Id) = bundleOf(
            ARG_SPACE to space,
            ARG_OBJECT_ID to objectId
        )
    }
}