package com.anytypeio.anytype.ui.objects.types.pickers

import androidx.core.os.bundleOf
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.hideSoftInput
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ui.objects.BaseObjectTypeChangeFragment

/**
 * Selecting a default object type for application
 */
class AppDefaultObjectTypeFragment : BaseObjectTypeChangeFragment() {

    override fun startWithParams() {
        vm.onStart(
            isWithCollection = false,
            isWithBookmark = false,
            isSetSource = false
        )
    }

    override fun onItemClicked(id: Id, name: String) {
        withParent<OnObjectTypeAction> {
            onProceedWithUpdateType(
                id = id,
                name = name
            )
        }
        hideSoftInput()
        dismiss()
    }

    override fun setTitle() {
        binding.tvTitle.text = getString(R.string.change_type)
    }

    override fun injectDependencies() {
        componentManager().objectTypeChangeComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectTypeChangeComponent.release()
    }

    companion object {
        fun newInstance(excludeTypes: List<Id>) = AppDefaultObjectTypeFragment().apply {
            arguments = bundleOf(ARG_EXCLUDE_TYPES to excludeTypes)
        }
    }

    interface OnObjectTypeAction {
        fun onProceedWithUpdateType(id: Id, name: String)
    }
}