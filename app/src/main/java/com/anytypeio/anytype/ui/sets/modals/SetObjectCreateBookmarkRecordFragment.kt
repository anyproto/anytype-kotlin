package com.anytypeio.anytype.ui.sets.modals

import android.text.InputType.TYPE_TEXT_VARIATION_URI
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.databinding.FragmentSetObjectCreateBookmarkRecordBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.ObjectSetCreateBookmarkRecordViewModel
import javax.inject.Inject

class SetObjectCreateBookmarkRecordFragment : SetObjectCreateRecordFragmentBase<FragmentSetObjectCreateBookmarkRecordBinding>() {


    override val textInputType: Int = TYPE_TEXT_VARIATION_URI
    override val textInputField: EditText
        get() = binding.textInputField
    override val button: TextView
        get() = binding.button

    private val ctx: String get() = argString(CONTEXT_KEY)

    @Inject
    lateinit var factory: ObjectSetCreateBookmarkRecordViewModel.Factory
    override val vm: ObjectSetCreateBookmarkRecordViewModel by viewModels { factory }


    override fun injectDependencies() {
        componentManager().objectSetCreateBookmarkRecordComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetCreateBookmarkRecordComponent.release(ctx)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSetObjectCreateBookmarkRecordBinding = FragmentSetObjectCreateBookmarkRecordBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val CONTEXT_KEY = "arg.object-set-record.context"
    }
}