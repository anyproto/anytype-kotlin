package com.anytypeio.anytype.ui.sets

import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.editor.picker.IconPickerViewModel
import com.anytypeio.anytype.presentation.editor.picker.ObjectSetIconPickerViewModelFactory
import com.anytypeio.anytype.ui.editor.modals.IconPickerFragmentBase
import javax.inject.Inject

class ObjectSetIconPickerFragment : IconPickerFragmentBase<Id>() {

    @Inject
    lateinit var factory: ObjectSetIconPickerViewModelFactory
    override val vm by viewModels<IconPickerViewModel<Id>> { factory }

    override val target: Id
        get() = context

    override fun injectDependencies() {
        componentManager().objectSetIconPickerComponent.get(context).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetIconPickerComponent.release(context)
    }
}