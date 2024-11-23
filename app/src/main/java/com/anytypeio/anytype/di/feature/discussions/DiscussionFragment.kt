package com.anytypeio.anytype.di.feature.discussions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ext.daggerViewModel
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModelFactory
import com.anytypeio.anytype.feature_discussions.ui.DiscussionScreenWrapper
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.search.GlobalSearchViewModel
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.search.GlobalSearchScreen
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import timber.log.Timber

class DiscussionFragment : BaseComposeFragment() {

    @Inject
    lateinit var factory: DiscussionViewModelFactory

    private val vm by viewModels<DiscussionViewModel> { factory }

    private val ctx get() = arg<Id>(CTX_KEY)
    private val space get() = arg<Id>(SPACE_KEY)

    // Rendering

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {

                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                    var showBottomSheet by remember { mutableStateOf(false) }

                    DiscussionScreenWrapper(
                        vm = vm,
                        onAttachObjectClicked = {
                            showBottomSheet = true
                        },
                        onBackButtonClicked = {
                            // TODO
                        },
                        onMarkupLinkClicked = {

                        }
                    )

                    if (showBottomSheet) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                showBottomSheet = false
                            },
                            sheetState = sheetState,
                            containerColor = colorResource(id = R.color.background_secondary),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            dragHandle = null
                        ) {
                            val component = componentManager().globalSearchComponent
                            val searchViewModel = daggerViewModel {
                                component.get(
                                    params = GlobalSearchViewModel.VmParams(
                                        space = SpaceId(space)
                                    )
                                ).getViewModel()
                            }
                            GlobalSearchScreen(
                                modifier = Modifier.padding(top = 12.dp),
                                state = searchViewModel.state
                                    .collectAsStateWithLifecycle()
                                    .value
                                ,
                                onQueryChanged = searchViewModel::onQueryChanged,
                                onObjectClicked = {
                                    vm.onAttachObject(it)
                                    showBottomSheet = false
                                },
                                onShowRelatedClicked = {
                                    // Do nothing.
                                },
                                onClearRelatedClicked = {

                                },
                                focusOnStart = false
                            )
                        }
                    } else {
                        componentManager().globalSearchComponent.release()
                    }
                }
                LaunchedEffect(Unit) {
                    vm.navigation.collect { nav ->
                        when(nav) {
                            is OpenObjectNavigation.OpenEditor -> {
                                runCatching {
                                    findNavController().navigate(
                                        R.id.objectNavigation,
                                        EditorFragment.args(
                                            ctx = nav.target,
                                            space = nav.space
                                        )
                                    )
                                }.onFailure {
                                    Timber.w("Error while opening editor from chat.")
                                }
                            }
                            else -> toast("TODO")
                        }
                    }
                }
            }
        }
    }

    // DI

    override fun injectDependencies() {
        componentManager()
            .discussionComponent
            .get(
                key = ctx,
                param = DiscussionViewModel.Params.Default(
                    ctx = ctx,
                    space = SpaceId(space)
                )
            )
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().discussionComponent.release(ctx)
    }

    override fun onApplyWindowRootInsets(view: View) {
        // Do not apply.
    }

    companion object {
        private const val CTX_KEY = "arg.discussion.ctx"
        private const val SPACE_KEY = "arg.discussion.space"
        fun args(
            space: Id,
            ctx: Id
        ) = bundleOf(
            CTX_KEY to ctx,
            SPACE_KEY to space
        )
    }
}