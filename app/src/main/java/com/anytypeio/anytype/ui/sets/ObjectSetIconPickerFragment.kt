package com.anytypeio.anytype.ui.sets

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.argBoolean
import com.anytypeio.anytype.core_utils.ext.setVisible
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.picker.IconPickerViewModel
import com.anytypeio.anytype.presentation.editor.picker.ObjectSetIconPickerViewModelFactory
import com.anytypeio.anytype.ui.editor.modals.IconPickerFragmentBase
import javax.inject.Inject

class ObjectSetIconPickerFragment : IconPickerFragmentBase<Id>() {

    @Inject
    lateinit var factory: ObjectSetIconPickerViewModelFactory
    override val vm by viewModels<IconPickerViewModel<Id>> { factory }

    override val target: Id
        get() = context

    private val showRemoveButton: Boolean
        get() = argBoolean(ARG_SHOW_REMOVE_BUTTON)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRemoveIcon.setVisible(showRemoveButton)
    }

    override fun injectDependencies() {
        componentManager().objectSetIconPickerComponent.get(context).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetIconPickerComponent.release(context)
    }
}

const val ARG_SHOW_REMOVE_BUTTON = "arg.show_remove_button"