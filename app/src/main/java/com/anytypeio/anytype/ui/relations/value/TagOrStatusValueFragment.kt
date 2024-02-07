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
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.relations.TagOrStatusValueScreen
import com.anytypeio.anytype.core_utils.ext.argBoolean
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.value.tagstatus.Command
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationContext
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagOrStatusValueViewModel
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagOrStatusValueViewModelFactory
import com.anytypeio.anytype.ui.settings.typography
import com.google.android.material.bottomsheet.BottomSheetDialog
import javax.inject.Inject

class TagOrStatusValueFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: TagOrStatusValueViewModelFactory
    private val vm by viewModels<TagOrStatusValueViewModel> { factory }

    private val ctx get() = argString(CTX_KEY)
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
                TagOrStatusValueScreen(
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
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        super.onStart()
        vm.onStart()
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    private fun observeCommands(command: Command) {
        when (command) {
            is Command.OpenOptionScreen -> {
                val arg = CreateOrEditOptionFragment.args(
                    ctx = command.ctx,
                    objectId = command.objectId,
                    relationKey = command.relationKey,
                    optionId = command.optionId,
                    color = command.color,
                    text = command.text,
                    relationContext = relationContext
                )
                findNavController().navigate(R.id.optionScreen, arg)
            }

            is Command.DeleteOption -> {
                val dialog = DeleteOptionWarningFragment.new(command.optionId)
                dialog.onDeletionAccepted = { optionId ->
                    vm.proceedWithDeleteOptions(optionId)
                    dialog.dismiss()
                }
                dialog.onDeletionCancelled = {
                    dialog.dismiss()
                }
                dialog.show(childFragmentManager, null)
            }

            Command.Dismiss -> dismiss()
            Command.Expand -> expand()
        }
    }

    override fun injectDependencies() {
        val params = TagOrStatusValueViewModel.ViewModelParams(
            ctx = ctx,
            objectId = objectId,
            relationKey = relationKey,
            isLocked = isLocked,
            relationContext = relationContext
        )
        inject(params)
    }

    private fun inject(params: TagOrStatusValueViewModel.ViewModelParams) {
        when (relationContext) {
            RelationContext.OBJECT -> componentManager()
                .tagStatusObjectComponent.get(params)
                .inject(this)
            RelationContext.OBJECT_SET -> componentManager()
                .tagStatusSetComponent.get(params)
                .inject(this)
            RelationContext.DATA_VIEW -> componentManager()
                .tagStatusDataViewComponent.get(params)
                .inject(this)
        }
    }

    override fun releaseDependencies() {
        when (relationContext) {
            RelationContext.OBJECT -> componentManager().tagStatusObjectComponent.release()
            RelationContext.OBJECT_SET -> componentManager().tagStatusSetComponent.release()
            RelationContext.DATA_VIEW -> componentManager().tagStatusDataViewComponent.release()
        }
    }

    private fun setupCollapsedHeight() {
        val height = resources.displayMetrics.heightPixels / 2
        (dialog as? BottomSheetDialog)?.behavior?.peekHeight = height
    }

    companion object {
        const val CTX_KEY = "arg.tag-status.ctx"
        const val RELATION_KEY = "arg.tag-status.relation.key"
        const val OBJECT_ID_KEY = "arg.tag-status.object"
        const val IS_LOCKED_KEY = "arg.tag-status.is-locked"
        const val RELATION_CONTEXT_KEY = "arg.tag-status.relation-context"
    }
}