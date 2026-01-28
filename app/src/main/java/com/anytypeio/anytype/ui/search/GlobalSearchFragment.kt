package com.anytypeio.anytype.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.extensions.isKeyboardVisible
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.setupBottomSheetBehavior
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.intents.ActivityCustomTabsHelper
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.search.GlobalSearchViewModel
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.chats.ChatFragment
import com.anytypeio.anytype.ui.date.DateObjectFragment
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.profile.ParticipantFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class GlobalSearchFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: GlobalSearchViewModel.Factory

    private val vm by viewModels<GlobalSearchViewModel> { factory }

    private val space get() = argString(ARG_SPACE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = typography
            ) {
                val scope = rememberCoroutineScope()
                GlobalSearchScreen(
                    state = vm.state.collectAsStateWithLifecycle().value,
                    onQueryChanged = vm::onQueryChanged,
                    onObjectClicked = {
                        if (isKeyboardVisible()) {
                            scope.launch {
                                delay(KEYBOARD_HIDE_DELAY)
                                vm.onObjectClicked(it)
                            }
                        } else {
                            vm.onObjectClicked(it)
                        }
                    },
                    onShowRelatedClicked = vm::onShowRelatedClicked,
                    onClearRelatedClicked = vm::onClearRelatedObjectClicked,
                )
            }
            LaunchedEffect(Unit) {
                vm.navigation.collect { nav ->
                    when (nav) {
                        is OpenObjectNavigation.OpenEditor -> {
                            findNavController().navigate(
                                R.id.objectNavigation,
                                EditorFragment.args(
                                    ctx = nav.target,
                                    space = nav.space
                                )
                            )
                        }
                        is OpenObjectNavigation.OpenDataView -> {
                            findNavController().navigate(
                                R.id.dataViewNavigation,
                                ObjectSetFragment.args(
                                    ctx = nav.target,
                                    space = nav.space
                                )
                            )
                        }
                        is OpenObjectNavigation.OpenParticipant -> {
                            runCatching {
                                findNavController().navigate(
                                    R.id.participantScreen,
                                    ParticipantFragment.args(
                                        objectId = nav.target,
                                        space = nav.space
                                    )
                                )
                            }.onFailure {
                                Timber.w("Error while opening participant screen")
                            }
                        }
                        is OpenObjectNavigation.OpenChat -> {
                            findNavController().navigate(
                                R.id.chatScreen,
                                ChatFragment.args(
                                    ctx = nav.target,
                                    space = nav.space,
                                    popUpToVault = false
                                )
                            )
                        }
                        OpenObjectNavigation.NonValidObject -> {
                            toast(getString(R.string.error_non_valid_object))
                        }
                        is OpenObjectNavigation.OpenDateObject -> {
                            runCatching {
                                findNavController().navigate(
                                    R.id.dateObjectScreen,
                                    DateObjectFragment.args(
                                        objectId = nav.target,
                                        space = nav.space
                                    )
                                )
                            }.onFailure {
                                Timber.e(it, "Failed to navigate to date object screen")
                            }
                        }
                        is OpenObjectNavigation.UnexpectedLayoutError -> {
                            toast(getString(R.string.error_unexpected_layout))
                        }
                        is OpenObjectNavigation.OpenType -> {
                            runCatching {
                                navigation().openObjectType(
                                    objectId = nav.target,
                                    space = nav.space
                                )
                            }.onFailure {
                                Timber.e(it, "Error while opening object type from ")
                            }
                        }
                        is OpenObjectNavigation.OpenBookmarkUrl -> {
                            try {
                                ActivityCustomTabsHelper.openUrl(
                                    activity = requireActivity(),
                                    url = nav.url
                                )
                            } catch (e: Throwable) {
                                Timber.e(e, "Error opening bookmark URL: ${nav.url}")
                                toast("Failed to open URL")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetBehavior(DEFAULT_PADDING_TOP)
    }

    override fun injectDependencies() {
        val params = GlobalSearchViewModel.VmParams(
            space = SpaceId(space)
        )
        componentManager().globalSearchComponent.get(params).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().globalSearchComponent.release()
    }

    companion object {
        const val KEYBOARD_HIDE_DELAY = 300L

        const val ARG_SPACE = "arg.global.search.space"
        fun args(space: Id): Bundle = bundleOf(ARG_SPACE to space)
    }
}