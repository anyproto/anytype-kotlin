package com.anytypeio.anytype.ui.discussions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModelFactory
import com.anytypeio.anytype.feature_discussions.ui.DiscussionScreenWrapper
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class DiscussionFragment : Fragment() {

    @Inject
    lateinit var factory: DiscussionViewModelFactory

    private val vm by viewModels<DiscussionViewModel> { factory }

    val ctx get() = arg<Id>(CTX_KEY)
    private val space get() = arg<Id>(SPACE_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        releaseDependencies()
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = content {
        MaterialTheme(typography = typography) {
            DiscussionScreenWrapper(
                vm = vm,
                onBackClicked = {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            )
        }
    }

    private fun injectDependencies() {
        componentManager()
            .discussionComponent
            .get(
                key = ctx,
                param = DiscussionViewModel.Params(
                    ctx = ctx,
                    space = SpaceId(space)
                )
            )
            .inject(this)
    }

    private fun releaseDependencies() {
        componentManager().discussionComponent.release(ctx)
    }

    companion object {
        private const val CTX_KEY = "arg.discussion.ctx"
        private const val SPACE_KEY = "arg.discussion.space"

        fun args(
            ctx: Id,
            space: Id
        ) = bundleOf(
            CTX_KEY to ctx,
            SPACE_KEY to space
        )
    }
}
