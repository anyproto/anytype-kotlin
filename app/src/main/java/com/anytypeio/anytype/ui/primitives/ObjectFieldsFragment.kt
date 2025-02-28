package com.anytypeio.anytype.ui.primitives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.features.fields.FieldListScreen
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.argStringOrNull
import com.anytypeio.anytype.core_utils.ext.safeNavigate
import com.anytypeio.anytype.core_utils.ext.setupBottomSheetBehavior
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.di.feature.DefaultComponentParam
import com.anytypeio.anytype.feature_object_type.fields.ui.LocalInfoScreen
import com.anytypeio.anytype.presentation.relations.ObjectRelationListViewModelFactory
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import com.anytypeio.anytype.presentation.relations.RelationListViewModel.Command
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationContext
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.editor.OnFragmentInteractionListener
import com.anytypeio.anytype.ui.relations.RelationDateValueFragment
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment
import com.anytypeio.anytype.ui.relations.value.ObjectValueFragment
import com.anytypeio.anytype.ui.relations.value.TagOrStatusValueFragment
import javax.inject.Inject
import kotlin.getValue
import timber.log.Timber

class ObjectFieldsFragment : BaseBottomSheetComposeFragment(),
    RelationTextValueFragment.TextValueEditReceiver,
    RelationDateValueFragment.DateValueEditReceiver {

    private val vm by viewModels<RelationListViewModel> { factory }

    private val ctx: String get() = argString(ARG_CTX)
    private val space: String get() = argString(ARG_SPACE)
    private val target: String? get() = argStringOrNull(ARG_TARGET)
    private val isSetFlow: Boolean get() = arg(ARG_SET_FLOW)

    @Inject
    lateinit var factory: ObjectRelationListViewModelFactory

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        content {
            MaterialTheme {
                FieldListScreen(
                    state = vm.views.collectAsStateWithLifecycle().value,
                    onRelationClicked = {
                        vm.onRelationClicked(
                            ctx = ctx,
                            target = target,
                            view = it.view
                        )
                    },
                    onLocalInfoIconClicked = {
                        vm.onShowLocalInfo()
                    },
                    onTypeIconClicked = {
                        vm.onTypeIconClicked()
                    },
                    onRemoveFromObjectClicked = vm::onRemoveFromObjectClicked,
                    onAddToTypeClicked = vm::onAddToTypeClicked
                )
                val showLocalFieldExplanationScreen = vm.showLocalInfo.collectAsStateWithLifecycle().value
                if (showLocalFieldExplanationScreen) {
                    val bottomSheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true
                    )
                    LocalInfoScreen(
                        modifier = Modifier.fillMaxWidth(),
                        bottomSheetState = bottomSheetState,
                        onDismiss = { vm.onDismissLocalInfo() }
                    )
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetBehavior(DEFAULT_PADDING_TOP)
    }

    private fun execute(command: Command) {
        when (command) {
            is Command.EditTextRelationValue -> {
                runCatching {
                    val fr = RelationTextValueFragment.new(
                        ctx = ctx,
                        relationKey = command.relationKey,
                        objectId = command.target,
                        isLocked = command.isLocked,
                        flow = if (isSetFlow)
                            RelationTextValueFragment.FLOW_DATAVIEW
                        else
                            RelationTextValueFragment.FLOW_DEFAULT,
                        space = space
                    )
                    fr.showChildFragment()
                }.onFailure {
                    Timber.e(it, "Error while opening relation text value from relation list")
                }
            }

            is Command.EditDateRelationValue -> {
                val fr = RelationDateValueFragment.new(
                    ctx = ctx,
                    space = space,
                    relationKey = command.relationKey,
                    objectId = command.target,
                    flow = if (isSetFlow) {
                        RelationDateValueFragment.FLOW_SET_OR_COLLECTION
                    } else {
                        RelationDateValueFragment.FLOW_DEFAULT
                    },
                    isLocked = command.isLocked
                )
                fr.showChildFragment()
            }

            is Command.EditFileObjectRelationValue -> {
                val relationContext =
                    if (isSetFlow) RelationContext.OBJECT_SET else RelationContext.OBJECT
                findNavController().safeNavigate(
                    R.id.objectRelationListScreen,
                    R.id.objectValueScreen,
                    ObjectValueFragment.args(
                        ctx = command.ctx,
                        space = space,
                        obj = command.target,
                        relation = command.relationKey,
                        isLocked = command.isLocked,
                        relationContext = relationContext
                    )
                )
            }

            is Command.SetRelationKey -> {
                withParent<OnFragmentInteractionListener> {
                    onSetRelationKeyClicked(
                        blockId = command.blockId,
                        key = command.key
                    )
                }
                dismiss()
            }

            is Command.EditTagOrStatusRelationValue -> {
                val relationContext =
                    if (isSetFlow) RelationContext.OBJECT_SET else RelationContext.OBJECT
                val bundle = TagOrStatusValueFragment.args(
                    ctx = command.ctx,
                    space = space,
                    obj = command.target,
                    relation = command.relationKey,
                    isLocked = command.isLocked,
                    context = relationContext
                )
                findNavController().safeNavigate(
                    R.id.objectRelationListScreen,
                    R.id.nav_relations,
                    bundle
                )
            }

            is Command.NavigateToDateObject -> {
                runCatching {
                    navigation().openDateObject(
                        objectId = command.objectId,
                        space = space
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening date object from relation list")
                }
            }

            is Command.NavigateToObjectType -> {
                runCatching {
                    navigation().openCurrentObjectTypeFields(
                        objectId = command.objectTypeId,
                        space = space
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening object type fields from object fields list")
                }
            }
        }
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.commands) { command -> execute(command) }
        jobs += lifecycleScope.subscribe(vm.toasts) { toast(it) }
        super.onStart()
        vm.onStartListMode(ctx)
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    override fun onTextValueChanged(ctx: Id, text: String, objectId: Id, relationKey: Key) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            relationKey = relationKey,
            value = text,
            isValueEmpty = text.isEmpty()
        )
    }

    override fun onNumberValueChanged(ctx: Id, number: Double?, objectId: Id, relationKey: Key) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            relationKey = relationKey,
            value = number,
            isValueEmpty = number == null
        )
    }

    override fun onDateValueChanged(
        ctx: Id,
        timeInSeconds: Number?,
        objectId: Id,
        relationKey: Key
    ) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            relationKey = relationKey,
            value = timeInSeconds,
            isValueEmpty = timeInSeconds == null
        )
    }

    override fun onOpenDateObject(timeInMillis: TimeInMillis) {
        vm.onOpenDateObjectByTimeInMillis(timeInMillis)
    }

    override fun injectDependencies() {
        val param = DefaultComponentParam(
            ctx = ctx,
            space = SpaceId(space)
        )
        if (isSetFlow) {
            componentManager().objectSetRelationListComponent.get(param).inject(this)
        } else {
            componentManager().objectRelationListComponent.get(param).inject(this)
        }
    }

    override fun releaseDependencies() {
        if (isSetFlow) {
            componentManager().objectSetRelationListComponent.release()
        } else {
            componentManager().objectRelationListComponent.release()
        }
    }

    /**
     * This screen should be started from Objects with Editor Layouts
     * or from objects with Set or Collection Layouts
     * @param isSetFlow - true if started from Set or Collection
     */
    companion object {
        fun new(
            ctx: Id,
            space: Id,
            target: String?,
            locked: Boolean = false,
            isSetFlow: Boolean = false,
        ) = ObjectFieldsFragment().apply {
            arguments = bundleOf(
                ARG_CTX to ctx,
                ARG_SPACE to space,
                ARG_TARGET to target,
                ARG_LOCKED to locked,
                ARG_SET_FLOW to isSetFlow
            )
        }

        const val ARG_CTX = "arg.document-relation.ctx"
        const val ARG_SPACE = "arg.document-relation.space"
        const val ARG_TARGET = "arg.document-relation.target"
        const val ARG_LOCKED = "arg.document-relation.locked"
        const val ARG_SET_FLOW = "arg.document-relation.set-flow"

        const val DEFAULT_PADDING_TOP = 10
    }
}