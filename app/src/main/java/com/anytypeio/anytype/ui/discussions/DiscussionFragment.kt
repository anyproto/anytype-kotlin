package com.anytypeio.anytype.ui.discussions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.intents.SystemAction.OpenUrl
import com.anytypeio.anytype.core_utils.intents.proceedWithAction
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModelFactory
import com.anytypeio.anytype.feature_discussions.ui.DiscussionScreenWrapper
import com.anytypeio.anytype.ui.chats.ChatFragment
import com.anytypeio.anytype.ui.chats.SelectChatReactionFragment
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.media.MediaActivity
import com.anytypeio.anytype.ui.primitives.ObjectTypeFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.ui.profile.ParticipantFragment
import com.anytypeio.anytype.ui.settings.typography
import timber.log.Timber
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
        LaunchedEffect(Unit) {
            vm.commands.collect { command ->
                when (command) {
                    is DiscussionViewModel.DiscussionCommand.SelectReaction -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.selectChatReactionScreen,
                                SelectChatReactionFragment.args(
                                    space = SpaceId(space),
                                    chat = ctx,
                                    msg = command.msg
                                )
                            )
                        }
                    }
                    is DiscussionViewModel.DiscussionCommand.ViewMemberCard -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.participantScreen,
                                ParticipantFragment.args(
                                    space = command.space.id,
                                    objectId = command.member
                                )
                            )
                        }.onFailure {
                            Timber.e(it, "Error while opening space member card")
                        }
                    }
                    is DiscussionViewModel.DiscussionCommand.Browse -> {
                        runCatching {
                            proceedWithAction(OpenUrl(command.url))
                        }.onFailure {
                            Timber.e(it, "Error while opening URL from discussion")
                        }
                    }
                    is DiscussionViewModel.DiscussionCommand.MediaPreview -> {
                        runCatching {
                            MediaActivity.start(
                                context = requireContext(),
                                mediaType = MediaActivity.TYPE_IMAGE,
                                objects = listOf(command.obj),
                                index = 0,
                                space = space
                            )
                        }.onFailure {
                            Timber.e(it, "Error while launching media preview from discussion")
                        }
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            vm.navigation.collect { nav ->
                when (nav) {
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
                            Timber.w("Error while opening editor from discussion")
                        }
                    }
                    is OpenObjectNavigation.OpenDataView -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.dataViewNavigation,
                                ObjectSetFragment.args(
                                    ctx = nav.target,
                                    space = nav.space
                                )
                            )
                        }.onFailure {
                            Timber.w("Error while opening set from discussion")
                        }
                    }
                    is OpenObjectNavigation.OpenType -> {
                        runCatching {
                            findNavController().navigate(
                                resId = R.id.objectTypeNavigation,
                                args = ObjectTypeFragment.args(
                                    objectId = nav.target,
                                    space = nav.space
                                )
                            )
                        }.onFailure {
                            Timber.w("Error while opening type from discussion")
                        }
                    }
                    is OpenObjectNavigation.OpenChat -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.chatScreen,
                                ChatFragment.args(
                                    space = nav.space,
                                    ctx = nav.target
                                )
                            )
                        }.onFailure {
                            Timber.w("Error while opening chat from discussion")
                        }
                    }
                    is OpenObjectNavigation.OpenParticipant -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.participantScreen,
                                ParticipantFragment.args(
                                    space = nav.space,
                                    objectId = nav.target
                                )
                            )
                        }.onFailure {
                            Timber.w("Error while opening participant from discussion")
                        }
                    }
                    is OpenObjectNavigation.OpenBookmarkUrl -> {
                        runCatching {
                            proceedWithAction(OpenUrl(nav.url))
                        }.onFailure {
                            Timber.w("Error while opening bookmark URL from discussion")
                        }
                    }
                    is OpenObjectNavigation.OpenDateObject -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.objectNavigation,
                                EditorFragment.args(
                                    ctx = nav.target,
                                    space = nav.space
                                )
                            )
                        }.onFailure {
                            Timber.w("Error while opening date object from discussion")
                        }
                    }
                    is OpenObjectNavigation.UnexpectedLayoutError -> {
                        Timber.w("Unexpected layout error: ${nav.layout}")
                    }
                    OpenObjectNavigation.NonValidObject -> {
                        Timber.w("Attempted to open non-valid object from discussion")
                    }
                }
            }
        }
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
