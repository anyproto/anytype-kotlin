package com.anytypeio.anytype.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationNavigation
import com.anytypeio.anytype.gallery_experience.screens.GalleryInstallationScreen
import com.anytypeio.anytype.gallery_experience.screens.GalleryInstallationSpacesScreen
import com.anytypeio.anytype.gallery_experience.viewmodel.GalleryInstallationViewModel
import com.anytypeio.anytype.gallery_experience.viewmodel.GalleryInstallationViewModelFactory
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import javax.inject.Inject

class GalleryInstallationFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: GalleryInstallationViewModelFactory
    private val vm by viewModels<GalleryInstallationViewModel> { factory }
    private lateinit var navController: NavHostController

    @OptIn(ExperimentalMaterialNavigationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeDialogView(context = requireContext(), dialog = requireDialog()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    val bottomSheetNavigator = rememberBottomSheetNavigator()
                    navController = rememberNavController(bottomSheetNavigator)
                    SetupNavigation(bottomSheetNavigator, navController)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        jobs += subscribe(vm.command) { command ->
            when (command) {
                GalleryInstallationNavigation.Main -> navController.navigate(
                    GalleryInstallationNavigation.Main.route
                )

                GalleryInstallationNavigation.Dismiss -> navController.popBackStack()
                else -> {}
            }
        }
    }

    @OptIn(ExperimentalMaterialNavigationApi::class)
    @Composable
    private fun SetupNavigation(
        bottomSheetNavigator: BottomSheetNavigator,
        navController: NavHostController
    ) {
        ModalBottomSheetLayout(bottomSheetNavigator = bottomSheetNavigator) {
            NavigationGraph(navController = navController)
        }
    }

    @OptIn(ExperimentalMaterialNavigationApi::class)
    @Composable
    private fun NavigationGraph(navController: NavHostController) {
        NavHost(
            navController = navController,
            startDestination = GalleryInstallationNavigation.Main.route
        ) {
            composable(GalleryInstallationNavigation.Main.route) {
                InitMainScreen()
            }
            bottomSheet(GalleryInstallationNavigation.Spaces.route) {
                InitSpacesScreen()
            }
        }
    }

    @Composable
    private fun InitMainScreen() {
        skipCollapsed()
        expand()
        GalleryInstallationScreen(
            state = vm.mainState.collectAsStateWithLifecycle().value
        )
    }

    @Composable
    private fun InitSpacesScreen() {
        GalleryInstallationSpacesScreen()
    }

    override fun injectDependencies() {
        componentManager().galleryInstallationsComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().galleryInstallationsComponent.release()
    }
}