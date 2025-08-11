package com.anytypeio.anytype.ui.publishtoweb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.intents.ActivityCustomTabsHelper
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.publishtoweb.PublishToWebViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import kotlin.math.exp
import timber.log.Timber

class PublishToWebFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: PublishToWebViewModel.Factory

    private val vm by viewModels<PublishToWebViewModel> { factory }

    private val ctx get() = arg<Id>(CTX_KEY)
    private val space get() = arg<Id>(SPACE_KEY)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    PublishToWebScreen(
                        viewState = vm.viewState.collectAsStateWithLifecycle().value,
                        onPublishClicked = vm::onPublishClicked,
                        onUnpublishClicked = vm::onUnpublishClicked,
                        onUpdateClicked = vm::onUpdateClicked,
                        onPreviewClicked = vm::onPreviewClicked
                    )
                    LaunchedEffect(Unit) {
                        vm.commands.collect { command ->
                            Timber.d("DROID-3786 New command: $command")
                            when (command) {
                                is PublishToWebViewModel.Command.Browse -> {
                                    ActivityCustomTabsHelper.openUrl(
                                        activity = requireActivity(),
                                        url = command.url
                                    )
                                }
                                else -> {
                                    // Do nothing
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skipCollapsed()
        expand()
    }

    override fun injectDependencies() {
        componentManager()
            .publishToWebComponent
            .get(
                params = PublishToWebViewModel.Params(
                    ctx = ctx,
                    space = SpaceId(space)
                )
            )
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().publishToWebComponent.release()
    }

    companion object {
        fun args(
            ctx: String,
            space: String
        ) = bundleOf(
            CTX_KEY to ctx,
            SPACE_KEY to space
        )

        private const val CTX_KEY = "arg.publish-to-web.ctx"
        private const val SPACE_KEY = "arg.publish-to-web.space"
    }
}