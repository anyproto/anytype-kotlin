package com.anytypeio.anytype.ui.editor.modals

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.icon.TextBlockTarget
import com.anytypeio.anytype.presentation.picker.IconPickerViewModel
import com.anytypeio.anytype.presentation.editor.picker.TextBlockIconPickerViewModelFactory
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
        componentManager().textBlockIconPickerComponent.get(context).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().textBlockIconPickerComponent.release(context)
    }

    companion object {
        const val ARG_BLOCK_ID_KEY = "arg.picker.block.id"
        fun new(
            context: Id,
            blockId: Id
        ) = TextBlockIconPickerFragment().apply {
            arguments = bundleOf(
                ARG_CONTEXT_ID_KEY to context,
                ARG_BLOCK_ID_KEY to blockId
            )
        }
    }
}