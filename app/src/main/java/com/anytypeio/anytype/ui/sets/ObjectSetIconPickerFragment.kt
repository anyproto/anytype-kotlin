package com.anytypeio.anytype.ui.sets

import androidx.fragment.app.viewModels
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.page.picker.ObjectSetIconPickerViewModel
import com.anytypeio.anytype.presentation.page.picker.ObjectSetIconPickerViewModelFactory
import com.anytypeio.anytype.ui.page.modals.ObjectIconPickerBaseFragment
import javax.inject.Inject

class ObjectSetIconPickerFragment : ObjectIconPickerBaseFragment() {

    @Inject
    lateinit var factory: ObjectSetIconPickerViewModelFactory
    override val vm by viewModels<ObjectSetIconPickerViewModel> { factory }

    override fun injectDependencies() {
        componentManager().objectSetIconPickerComponent.get(context).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetIconPickerComponent.release(context)
    }
}