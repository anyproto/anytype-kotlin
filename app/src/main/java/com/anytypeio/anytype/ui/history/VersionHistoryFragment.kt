package com.anytypeio.anytype.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.features.editor.BlockAdapter
import com.anytypeio.anytype.core_ui.features.editor.DragAndDropAdapterDelegate
import com.anytypeio.anytype.core_ui.features.history.VersionHistoryPreviewScreen
import com.anytypeio.anytype.core_ui.features.history.VersionHistoryScreen
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.setupBottomSheetBehavior
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.history.VersionGroupNavigation
import com.anytypeio.anytype.presentation.history.VersionHistoryVMFactory
import com.anytypeio.anytype.presentation.history.VersionHistoryViewModel
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import java.util.LinkedList
import javax.inject.Inject

class VersionHistoryFragment : BaseBottomSheetComposeFragment() {

    private val ctx get() = argString(CTX_ARG)
    private val spaceId get() = argString(SPACE_ID_ARG)

    @Inject
    lateinit var factory: VersionHistoryVMFactory
    private val vm by viewModels<VersionHistoryViewModel> { factory }
    private lateinit var navComposeController: NavHostController

    private val editorAdapter = BlockAdapter(
        restore = LinkedList(),
        initialBlock = mutableListOf(),
        clipboardInterceptor = object : ClipboardInterceptor {
            override fun onClipboardAction(action: ClipboardInterceptor.Action) {
                TODO("Not yet implemented")
            }

            override fun onBookmarkPasted(url: Url) {
                TODO("Not yet implemented")
            }
        },
        onBackPressedCallback = { false },
        onDragListener = { _, _ -> false },
        lifecycle = lifecycle,
        dragAndDropSelector = DragAndDropAdapterDelegate(),
    )

    @OptIn(ExperimentalMaterialNavigationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val bottomSheetNavigator = rememberBottomSheetNavigator()
                navComposeController = rememberNavController(bottomSheetNavigator)
                SetupNavigation(bottomSheetNavigator, navComposeController)
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
        NavHost(navController = navController, startDestination = VersionGroupNavigation.Main.route) {
            composable(VersionGroupNavigation.Main.route) {
                VersionHistoryScreen(
                    state = vm.viewState.collectAsStateWithLifecycle().value,
                    onGroupClick = vm::onGroupClicked,
                    onItemClick = vm::onGroupItemClicked
                )
            }
            bottomSheet(VersionGroupNavigation.VersionPreview.route) {
                VersionHistoryPreviewScreen(
                    state = vm.previewViewState.collectAsStateWithLifecycle().value,
                    editorAdapter = editorAdapter,
                    onDismiss = vm::proceedWithHidePreview,
                    onRestore = vm::proceedWithRestore
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetBehavior(74)
        subscribe(vm.navigation){ navigation ->
            when(navigation){
                is VersionGroupNavigation.VersionPreview -> {
                    navComposeController.navigate(VersionGroupNavigation.VersionPreview.route)
                }
                VersionGroupNavigation.Main -> {
                    navComposeController.popBackStack()
                }
                VersionGroupNavigation.Dismiss -> {
                    findNavController().popBackStack()
                }
                VersionGroupNavigation.ExitToObject -> {
                    findNavController().popBackStack(R.id.objectMenuScreen, true)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart()
    }

    override fun injectDependencies() {
        val vmParams = VersionHistoryViewModel.VmParams(
            objectId = ctx,
            spaceId = SpaceId(spaceId)
        )
        componentManager().versionHistoryComponent.get(vmParams,).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().versionHistoryComponent.release()
    }

    companion object {
        const val CTX_ARG = "anytype.ui.history.ctx_arg"
        const val SPACE_ID_ARG = "anytype.ui.history.space_id_arg"

        fun args(ctx: Id, spaceId: Id) = bundleOf(
            CTX_ARG to ctx,
            SPACE_ID_ARG to spaceId
        )
    }
}