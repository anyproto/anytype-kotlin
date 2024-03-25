package com.anytypeio.anytype.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.NO_VALUE
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_ui.views.BaseAlertDialog
import com.anytypeio.anytype.core_utils.ext.argString
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
import timber.log.Timber

class GalleryInstallationFragment : BaseBottomSheetComposeFragment() {

    private val deepLinkType get() = argString(DEEPLINK_TYPE_KEY)
    private val deepLinkSource get() = argString(DEEPLINK_SOURCE_KEY)

    @Inject
    lateinit var factory: GalleryInstallationViewModelFactory
    private val vm by viewModels<GalleryInstallationViewModel> { factory }
    private lateinit var navController: NavHostController

    @OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterial3Api::class)
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
                    val errorText = remember { mutableStateOf(NO_VALUE) }
                    val isErrorDialogVisible = remember { mutableStateOf(false) }
                    SetupNavigation(bottomSheetNavigator, navController)
                    LaunchedEffect(key1 = Unit) {
                        vm.errorState.collect { error ->
                            if (!error.isNullOrBlank()) {
                                errorText.value = error
                                isErrorDialogVisible.value = true
                            }
                        }
                    }
                    if (isErrorDialogVisible.value) {
                        BaseAlertDialog(
                            dialogText = errorText.value,
                            buttonText = stringResource(id = R.string.alert_qr_camera_ok),
                            onButtonClick = {
                                isErrorDialogVisible.value = false
                                errorText.value = NO_VALUE
                            },
                            onDismissRequest = {
                                isErrorDialogVisible.value = false
                                errorText.value = NO_VALUE
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        jobs += subscribe(vm.command) { command ->
            Timber.d("GalleryInstallationFragment command: $command")
            when (command) {
                GalleryInstallationNavigation.Main -> navController.navigate(
                    GalleryInstallationNavigation.Main.route
                )
                GalleryInstallationNavigation.Spaces -> navController.navigate(
                    GalleryInstallationNavigation.Spaces.route
                )
                GalleryInstallationNavigation.Dismiss -> navController.popBackStack()
                GalleryInstallationNavigation.Exit -> dismiss()
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
            state = vm.mainState.collectAsStateWithLifecycle().value,
            onInstallClicked = vm::onInstallClicked
        )
    }

    @Composable
    private fun InitSpacesScreen() {
        GalleryInstallationSpacesScreen(
            state = vm.spacesViewState.collectAsStateWithLifecycle().value,
            onNewSpaceClick = vm::onNewSpaceClick,
            onSpaceClick = vm::onSpaceClick,
            onDismiss = vm::onDismiss
        )
    }

    override fun injectDependencies() {
        val params = GalleryInstallationViewModel.ViewModelParams(
            deepLinkType = deepLinkType,
            deepLinkSource = deepLinkSource
        )
        componentManager().galleryInstallationsComponent.get(params).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().galleryInstallationsComponent.release()
    }

    companion object {
        const val DEEPLINK_TYPE_KEY = "arg.gallery-installation.deeplink-type-key"
        const val DEEPLINK_SOURCE_KEY = "arg.gallery-installation.deeplink-source-key"
        fun args(
            deepLinkType: String,
            deepLinkSource: String
        ): Bundle = bundleOf(
            DEEPLINK_TYPE_KEY to deepLinkType,
            DEEPLINK_SOURCE_KEY to deepLinkSource
        )
    }
}