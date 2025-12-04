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
import com.anytypeio.anytype.core_ui.features.dataview.ViewerGridHeaderAdapter
import com.anytypeio.anytype.core_ui.features.editor.BlockAdapter
import com.anytypeio.anytype.core_ui.features.editor.DragAndDropAdapterDelegate
import com.anytypeio.anytype.core_ui.features.history.VersionHistoryPreviewScreen
import com.anytypeio.anytype.core_ui.features.history.VersionHistoryScreen
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.safeNavigate
import com.anytypeio.anytype.core_utils.ext.setupBottomSheetBehavior
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.history.VersionHistoryVMFactory
import com.anytypeio.anytype.presentation.history.VersionHistoryViewModel
import com.anytypeio.anytype.presentation.history.VersionHistoryViewModel.Command
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationContext
import com.anytypeio.anytype.ui.relations.RelationDateValueFragment
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment
import com.anytypeio.anytype.ui.relations.value.ObjectValueFragment
import com.anytypeio.anytype.ui.relations.value.TagOrStatusValueFragment
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import java.util.LinkedList
import javax.inject.Inject
import timber.log.Timber

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
            override fun onClipboardAction(action: ClipboardInterceptor.Action) {}
            override fun onBookmarkPasted(url: Url) {}
        },
        onBackPressedCallback = { false },
        onDragListener = { _, _ -> false },
        lifecycle = lifecycle,
        dragAndDropSelector = DragAndDropAdapterDelegate(),
        onClickListener = {
            vm.proceedWithClick(it)
        }
    )
    private val viewerGridHeaderAdapter by lazy { ViewerGridHeaderAdapter() }

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
        NavHost(
            navController = navController,
            startDestination = NAVIGATION_MAIN
        ) {
            composable(NAVIGATION_MAIN) {
                VersionHistoryScreen(
                    state = vm.viewState.collectAsStateWithLifecycle(),
                    listState = vm.listState.collectAsStateWithLifecycle(),
                    latestVisibleVersionId = vm.latestVisibleVersionId.collectAsStateWithLifecycle(),
                    onGroupItemClicked = vm::onGroupItemClicked,
                    onLastItemScrolled = vm::startPaging
                )
            }
            bottomSheet(NAVIGATION_VERSION_PREVIEW) {
                VersionHistoryPreviewScreen(
                    state = vm.previewViewState.collectAsStateWithLifecycle().value,
                    editorAdapter = editorAdapter,
                    onDismiss = vm::proceedWithHidePreview,
                    onRestore = vm::proceedWithRestore,
                    gridAdapter = viewerGridHeaderAdapter
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetBehavior(DEFAULT_PADDING_TOP)
        subscribe(vm.navigation){ navigation ->
            when (navigation) {
                is Command.VersionPreview -> navigateToVersionPreview()
                Command.Main -> navComposeController.popBackStack()
                Command.ExitToObject -> exitToObjectMenu()
                is Command.RelationMultiSelect -> navigateToRelationMultiSelect(navigation)
                is Command.RelationDate -> navigateToRelationDate(navigation)
                is Command.RelationObject -> navigateToRelationObject(navigation)
                is Command.RelationText -> navigateToRelationText(navigation)
            }
        }
    }

    private fun navigateToVersionPreview() {
        navComposeController.navigate(NAVIGATION_VERSION_PREVIEW)
    }

    private fun exitToObjectMenu() {
        // Avoid IllegalArgumentException when the object menu isn't in the back stack
        val controller = findNavController()
        try {
            controller.popBackStack(R.id.objectMenuScreen, true)
        } catch (e: IllegalArgumentException) {
            Timber.w(e, "objectMenuScreen not in back stack, doing regular popBackStack")
            controller.popBackStack()
        }
    }

    private fun navigateToRelationMultiSelect(navigation: Command.RelationMultiSelect) {
        val relationContext = if (navigation.isSet) RelationContext.OBJECT_SET else RelationContext.OBJECT
        val bundle = TagOrStatusValueFragment.args(
            ctx = ctx,
            space = spaceId,
            obj = ctx,
            relation = navigation.relationKey.key,
            isLocked = true,
            context = relationContext
        )
        findNavController().safeNavigate(
            R.id.versionHistoryScreen,
            R.id.nav_relations,
            bundle
        )
    }

    private fun navigateToRelationDate(navigation: Command.RelationDate) {
        val relationContext = if (navigation.isSet) RelationDateValueFragment.FLOW_SET_OR_COLLECTION else RelationDateValueFragment.FLOW_DEFAULT
        val fr = RelationDateValueFragment.new(
            ctx = ctx,
            space = spaceId,
            relationKey = navigation.relationKey.key,
            objectId = ctx,
            flow = relationContext,
            isLocked = true
        )
        fr.showChildFragment()
    }

    private fun navigateToRelationObject(navigation: Command.RelationObject) {
        val relationContext = if (navigation.isSet) RelationContext.OBJECT_SET else RelationContext.OBJECT
        findNavController().safeNavigate(
            R.id.versionHistoryScreen,
            R.id.objectValueScreen,
            ObjectValueFragment.args(
                ctx = ctx,
                space = spaceId,
                obj = ctx,
                relation = navigation.relationKey.key,
                isLocked = true,
                relationContext = relationContext
            )
        )
    }

    private fun navigateToRelationText(navigation: Command.RelationText) {
        val relationContext =
            if (navigation.isSet) RelationTextValueFragment.FLOW_SET_OR_COLLECTION else RelationTextValueFragment.FLOW_DEFAULT
        val fr = RelationTextValueFragment.new(
            ctx = ctx,
            space = spaceId,
            relationKey = navigation.relationKey.key,
            objectId = ctx,
            flow = relationContext,
            isLocked = true
        )
        fr.showChildFragment()
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

        const val DEFAULT_PADDING_TOP = 10

        const val CTX_ARG = "anytype.ui.history.ctx_arg"
        const val SPACE_ID_ARG = "anytype.ui.history.space_id_arg"

        const val NAVIGATION_MAIN = "main"
        const val NAVIGATION_VERSION_PREVIEW = "preview"

        fun args(ctx: Id, spaceId: Id) = bundleOf(
            CTX_ARG to ctx,
            SPACE_ID_ARG to spaceId
        )
    }
}