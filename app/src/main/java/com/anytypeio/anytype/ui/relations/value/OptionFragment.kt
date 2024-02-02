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
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_ui.relations.OptionWidget
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.argStringOrNull
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.option.OptionViewModel
import com.anytypeio.anytype.presentation.relations.option.OptionViewModelFactory
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationContext
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class OptionFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: OptionViewModelFactory
    private val vm by viewModels<OptionViewModel> { factory }

    private val ctx get() = argString(CTX_KEY)
    private val objectId get() = argString(OBJECT_ID_KEY)
    private val relationKey get() = argString(RELATION_KEY)
    private val optionId get() = argStringOrNull(OPTION_ID_KEY)
    private val color get() = argStringOrNull(COLOR_KEY)
    private val text get() = argStringOrNull(TEXT_KEY)
    private val relationContext get() = requireArguments().getSerializable(RELATION_CONTEXT_KEY) as RelationContext

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(
                    typography = typography,
                    shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(10.dp)),
                    colors = MaterialTheme.colors.copy(
                        surface = colorResource(id = R.color.context_menu_background)
                    )
                ) {
                    OptionWidget(
                        state = vm.viewState.collectAsStateWithLifecycle().value,
                        onButtonClicked = { vm.onButtonClick() },
                        onTextChanged = { vm.updateName(it) },
                        onColorChanged = { vm.updateColor(it) }
                    )
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skipCollapsed()
        expand()
    }

    override fun onStart() {
        super.onStart()
        jobs += lifecycleScope.subscribe(vm.command) { observeCommand(it) }
    }

    private fun observeCommand(command: OptionViewModel.Command) {
        when (command) {
            is OptionViewModel.Command.Dismiss -> dismiss()
        }
    }

    override fun injectDependencies() {
        val params = OptionViewModel.Params(
            ctx = ctx,
            relationKey = relationKey,
            optionId = optionId,
            color = color,
            name = text,
            objectId = objectId
        )
        inject(params)
    }

    override fun releaseDependencies() {
        componentManager().optionObjectComponent.release()
    }

    private fun inject(params: OptionViewModel.Params) {
        when (relationContext) {
            RelationContext.OBJECT -> {
                componentManager()
                    .optionObjectComponent.get(params)
                    .inject(this)
            }

            RelationContext.OBJECT_SET -> TODO()
            RelationContext.DATA_VIEW -> TODO()
        }
    }

    companion object {
        const val CTX_KEY = "arg.option.ctx"
        const val OBJECT_ID_KEY = "arg.option.object_id"
        const val RELATION_KEY = "arg.option.relation_key"
        const val OPTION_ID_KEY = "arg.option.option_id"
        const val COLOR_KEY = "arg.option.color"
        const val TEXT_KEY = "arg.option.text"
        const val RELATION_CONTEXT_KEY = "arg.option.relation-context"

        fun args(
            ctx: String,
            objectId: String,
            relationKey: Key,
            optionId: String?,
            color: String?,
            text: String?,
            relationContext: RelationContext
        ) = bundleOf(
            CTX_KEY to ctx,
            OBJECT_ID_KEY to objectId,
            RELATION_KEY to relationKey,
            OPTION_ID_KEY to optionId,
            COLOR_KEY to color,
            TEXT_KEY to text,
            RELATION_CONTEXT_KEY to relationContext
        )
    }
}