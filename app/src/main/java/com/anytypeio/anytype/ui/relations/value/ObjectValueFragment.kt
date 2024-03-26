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
import com.anytypeio.anytype.core_ui.relations.RelationObjectValueScreen
import com.anytypeio.anytype.core_utils.ext.argBoolean
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueViewModel
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueViewModelFactory
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationContext
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.ui.settings.typography
import com.google.android.material.bottomsheet.BottomSheetDialog
import javax.inject.Inject

class ObjectValueFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: ObjectValueViewModelFactory
    private val vm by viewModels<ObjectValueViewModel> { factory }

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
    }

    private fun observeCommands(command: ObjectValueViewModel.Command) = when (command) {
        ObjectValueViewModel.Command.Dismiss -> dismiss()
        is ObjectValueViewModel.Command.OpenObject -> {
            findNavController().navigate(
                R.id.objectNavigation,
                EditorFragment.args(
                    ctx = command.id,
                    space = command.space
                )
            )
            dismiss()
        }
        is ObjectValueViewModel.Command.OpenSet -> {
            findNavController().navigate(
                R.id.dataViewNavigation,
                ObjectSetFragment.args(
                    ctx = command.id,
                    space = command.space
                )
            )
            dismiss()
        }
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
            relationContext = relationContext
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
        const val CTX_KEY = "arg.relation.object.ctx"
        const val RELATION_KEY = "arg.relation.object.relation.key"
        const val OBJECT_ID_KEY = "arg.relation.object.object"
        const val IS_LOCKED_KEY = "arg.relation.object.is-locked"
        const val RELATION_CONTEXT_KEY = "arg.relation.object.relation-context"
    }
}