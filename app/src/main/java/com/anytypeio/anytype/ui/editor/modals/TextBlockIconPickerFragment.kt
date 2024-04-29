package com.anytypeio.anytype.ui.editor.modals

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.di.feature.DefaultComponentParam
import com.anytypeio.anytype.domain.icon.TextBlockTarget
import com.anytypeio.anytype.presentation.editor.picker.TextBlockIconPickerViewModelFactory
import com.anytypeio.anytype.presentation.picker.IconPickerViewModel
import javax.inject.Inject

class TextBlockIconPickerFragment : IconPickerFragmentBase<TextBlockTarget>() {

    @Inject
    lateinit var factory: TextBlockIconPickerViewModelFactory
    override val vm by viewModels<IconPickerViewModel<TextBlockTarget>> { factory }

    private val blockId: Id
        get() = arg(ARG_BLOCK_ID_KEY)

    override val target: TextBlockTarget by lazy {
        TextBlockTarget(context, blockId)
    }

    override fun injectDependencies() {
        componentManager()
            .textBlockIconPickerComponent
            .get(
                params = DefaultComponentParam(
                    ctx = context,
                    space = SpaceId(space)
                )
            )
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().textBlockIconPickerComponent.release()
    }

    companion object {
        const val ARG_BLOCK_ID_KEY = "arg.picker.block.id"
        fun new(
            context: Id,
            blockId: Id,
            space: Id
        ) = TextBlockIconPickerFragment().apply {
            arguments = bundleOf(
                ARG_CONTEXT_ID_KEY to context,
                ARG_BLOCK_ID_KEY to blockId,
                ARG_SPACE_ID_KEY to space
            )
        }
    }
}