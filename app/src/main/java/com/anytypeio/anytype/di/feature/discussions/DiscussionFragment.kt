package com.anytypeio.anytype.di.feature.discussions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModelFactory
import com.anytypeio.anytype.feature_discussions.ui.DiscussionScreenWrapper
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class DiscussionFragment : BaseComposeFragment() {

    @Inject
    lateinit var factory: DiscussionViewModelFactory

    private val vm by viewModels<DiscussionViewModel> { factory }

    private val ctx get() = arg<Id>(CTX_KEY)
    private val space get() = arg<Id>(SPACE_KEY)

    // Rendering

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    DiscussionScreenWrapper(vm = vm)
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
                param = BaseViewModel.DefaultParams(
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