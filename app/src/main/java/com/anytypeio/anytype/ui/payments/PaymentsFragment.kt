package com.anytypeio.anytype.ui.payments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.screens.MainPaymentsScreen
import com.anytypeio.anytype.screens.ModalTier
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.viewmodel.PaymentsViewModel
import com.anytypeio.anytype.viewmodel.PaymentsViewModelFactory
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import javax.inject.Inject

class PaymentsFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: PaymentsViewModelFactory

    private val vm by viewModels<PaymentsViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeDialogView(context = requireContext(), dialog = requireDialog()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    MainScreen()
                    //MainPaymentsScreen(vm.viewState.collectAsStateWithLifecycle().value)
                }
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterialNavigationApi::class)
    fun MainScreen() {
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        val navController = rememberNavController(bottomSheetNavigator)
        ModalBottomSheetLayout(bottomSheetNavigator = bottomSheetNavigator) {
            NavHost(navController = navController, startDestination = "first") {
                composable(route = "first") {
                    MainPaymentsScreen(vm.viewState.collectAsStateWithLifecycle().value) { tier ->
                        vm.showTier.value = tier
                        navController.navigate("second")
                    }
                }
                bottomSheet(route = "second") {
                    ModalTier(
                        tier = vm.showTier.collectAsStateWithLifecycle().value,
                        onDismiss = { navController.popBackStack() })
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().paymentsComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().paymentsComponent.release()
    }
}