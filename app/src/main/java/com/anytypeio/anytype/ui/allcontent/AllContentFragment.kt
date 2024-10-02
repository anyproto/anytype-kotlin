package com.anytypeio.anytype.ui.allcontent

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.core_models.Id
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
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import timber.log.Timber

class AllContentFragment : BaseComposeFragment() {

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
                is AllContentViewModel.Command.NavigateToEditor -> {
                    runCatching {
                        navigation().openDocument(
                            target = command.id,
                            space = command.space
                        )
                    }.onFailure {
                        toast("Failed to open document")
                        Timber.e(it, "Failed to open document")
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
                        Timber.e(it, "Failed to open object set")
                    }
                }
                is AllContentViewModel.Command.SendToast -> {
                    toast(command.message)
                }
                is AllContentViewModel.Command.NavigateToBin -> {
                    runCatching {
                        navigation().launchCollections(
                            subscription = Subscription.Bin,
                            space = command.space
                        )
                    }.onFailure {
                        toast("Failed to open bin")
                        Timber.e(it, "Failed to open bin")
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
                    uiMenuButtonViewState = vm.uiMenuButtonState.collectAsStateWithLifecycle().value,
                    uiMenuState = vm.uiMenu.collectAsStateWithLifecycle().value,
                    onSortClick = vm::onSortClicked,
                    onModeClick = vm::onAllContentModeClicked,
                    onItemClicked = vm::onItemClicked,
                    onBinClick = vm::onViewBinClicked,
                    canPaginate = vm.canPaginate.collectAsStateWithLifecycle().value,
                    onUpdateLimitSearch = vm::updateLimit,
                    uiContentState = vm.uiContentState.collectAsStateWithLifecycle().value,
                )
            }
        }
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