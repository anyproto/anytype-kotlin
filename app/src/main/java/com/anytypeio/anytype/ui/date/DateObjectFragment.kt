package com.anytypeio.anytype.ui.date

import android.os.Build
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_date.presentation.DateObjectViewModel
import com.anytypeio.anytype.feature_date.presentation.DateObjectViewModelFactory
import com.anytypeio.anytype.feature_date.ui.DateObjectScreen
import com.anytypeio.anytype.ui.search.GlobalSearchFragment
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import javax.inject.Inject
import timber.log.Timber

class DateObjectFragment : BaseComposeFragment() {
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
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribe(vm.commands) { command ->
            Timber.d("Received command: $command")
            when (command) {
                DateObjectViewModel.Command.Back -> TODO()
                DateObjectViewModel.Command.ExitToVault -> TODO()
                is DateObjectViewModel.Command.NavigateToBin -> TODO()
                is DateObjectViewModel.Command.NavigateToEditor -> TODO()
                is DateObjectViewModel.Command.NavigateToSetOrCollection -> TODO()
                is DateObjectViewModel.Command.OpenChat -> TODO()
                DateObjectViewModel.Command.OpenGlobalSearch -> TODO()
                is DateObjectViewModel.Command.SendToast.Error -> TODO()
                is DateObjectViewModel.Command.SendToast.ObjectArchived -> TODO()
                is DateObjectViewModel.Command.SendToast.RelationRemoved -> TODO()
                is DateObjectViewModel.Command.SendToast.TypeRemoved -> TODO()
                is DateObjectViewModel.Command.SendToast.UnexpectedLayout -> TODO()
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
            }
        }
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
                    uiHeaderActions = {},
                    uiBottomMenuActions = {},
                    uiTopToolbarActions = vm::onTopToolbarActions,
                    uiVerticalListActions = {},
                    uiHorizontalListActions = vm::onHorizontalItemClicked,
                    onUpdateLimitSearch = vm::updateLimit,
                    onCalendarDateSelected = vm::onCalendarDateSelected
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
        if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
            // Do nothing.
        } else {
            super.onApplyWindowRootInsets(view)
        }
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