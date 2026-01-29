package com.anytypeio.anytype.ui.relations.value

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.relations.RelationObjectValueScreen
import com.anytypeio.anytype.core_utils.ext.argBoolean
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.intents.ActivityCustomTabsHelper
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueViewModel
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueViewModelFactory
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationContext
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.chats.ChatFragment
import com.anytypeio.anytype.ui.date.DateObjectFragment
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.profile.ParticipantFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.ui.settings.typography
import com.google.android.material.bottomsheet.BottomSheetDialog
import javax.inject.Inject
import timber.log.Timber

class ObjectValueFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: ObjectValueViewModelFactory
    private val vm by viewModels<ObjectValueViewModel> { factory }

    private val ctx get() = argString(CTX_KEY)
    private val space get() = argString(SPACE_KEY)
    private val relationKey get() = argString(RELATION_KEY)
    private val objectId get() = argString(OBJECT_ID_KEY)
    private val isLocked get() = argBoolean(IS_LOCKED_KEY)
    private val relationContext get() = requireArguments().getSerializable(RELATION_CONTEXT_KEY) as RelationContext

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = typography,
                shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(10.dp)),
                colors = MaterialTheme.colors.copy(
                    surface = colorResource(id = R.color.context_menu_background)
                )
            ) {
                RelationObjectValueScreen(
                    state = vm.viewState.collectAsStateWithLifecycle().value,
                    action = vm::onAction,
                    onQueryChanged = vm::onQueryChanged
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCollapsedHeight()
        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        jobs += lifecycleScope.subscribe(vm.navigation) { nav ->
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
                            space = nav.space
                        )
                    )
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
                OpenObjectNavigation.NonValidObject -> {
                    toast(getString(R.string.error_non_valid_object))
                }
                is OpenObjectNavigation.UnexpectedLayoutError -> {
                    toast(getString(R.string.error_unexpected_layout))
                }
            }
        }
    }

    private fun observeCommands(command: ObjectValueViewModel.Command) = when (command) {
        ObjectValueViewModel.Command.Dismiss -> dismiss()
        ObjectValueViewModel.Command.Expand -> expand()
        is ObjectValueViewModel.Command.DeleteObject -> {
            val dialog = DeleteOptionWarningFragment.new(
                optionId = command.id,
                descriptionString = R.string.object_values_delete_description
            )
            dialog.onDeletionAccepted = { optionId ->
                vm.onDeleteAction(optionId)
                dialog.dismiss()
            }
            dialog.onDeletionCancelled = {
                dialog.dismiss()
            }
            dialog.show(childFragmentManager, null)
        }
    }

    override fun injectDependencies() {
        val params = ObjectValueViewModel.ViewModelParams(
            ctx = ctx,
            objectId = objectId,
            relationKey = relationKey,
            isLocked = isLocked,
            relationContext = relationContext,
            space = SpaceId(space)
        )
        inject(params)
    }

    private fun inject(params: ObjectValueViewModel.ViewModelParams) = when (relationContext) {
        RelationContext.OBJECT -> componentManager()
            .objectValueObjectComponent.get(params)
            .inject(this)
        RelationContext.OBJECT_SET -> componentManager()
            .objectValueSetComponent.get(params)
            .inject(this)
        RelationContext.DATA_VIEW -> componentManager()
            .objectValueDataViewComponent.get(params)
            .inject(this)
    }

    override fun releaseDependencies() = when (relationContext) {
        RelationContext.OBJECT -> componentManager().objectValueObjectComponent.release()
        RelationContext.OBJECT_SET -> componentManager().objectValueSetComponent.release()
        RelationContext.DATA_VIEW -> componentManager().objectValueDataViewComponent.release()
    }

    private fun setupCollapsedHeight() {
        val height = resources.displayMetrics.heightPixels / 2
        (dialog as? BottomSheetDialog)?.behavior?.peekHeight = height
    }

    companion object {

        private const val CTX_KEY = "arg.relation.object.ctx"
        private const val SPACE_KEY = "arg.relation.object.space"
        private const val RELATION_KEY = "arg.relation.object.relation.key"
        private const val OBJECT_ID_KEY = "arg.relation.object.object"
        private const val IS_LOCKED_KEY = "arg.relation.object.is-locked"
        private const val RELATION_CONTEXT_KEY = "arg.relation.object.relation-context"

        fun args(
            ctx: Id,
            space: Id,
            obj: Id,
            relation: Key,
            isLocked: Boolean,
            relationContext: RelationContext
        ) = bundleOf(
            CTX_KEY to ctx,
            SPACE_KEY to space,
            OBJECT_ID_KEY to obj,
            RELATION_KEY to relation,
            IS_LOCKED_KEY to isLocked,
            RELATION_CONTEXT_KEY to relationContext
        )
    }
}