package com.anytypeio.anytype.ui.editor.modals

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.editor.picker.ObjectIconPickerViewModelFactory
import com.anytypeio.anytype.presentation.picker.IconPickerViewModel
import javax.inject.Inject

open class ObjectIconPickerFragment : IconPickerFragmentBase<Id>() {

    @Inject
    lateinit var factory: ObjectIconPickerViewModelFactory
    override val vm by viewModels<IconPickerViewModel<Id>> { factory }

    override val target: Id
        get() = context

    override fun injectDependencies() {
        componentManager().objectIconPickerComponent.get(context).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectIconPickerComponent.release(context)
    }

    companion object {
        fun new(ctx: Id, space: Id) = ObjectIconPickerFragment().apply {
            arguments = bundleOf(
                ARG_CONTEXT_ID_KEY to ctx,
                ARG_SPACE_ID_KEY to space
            )
        }
    }
}