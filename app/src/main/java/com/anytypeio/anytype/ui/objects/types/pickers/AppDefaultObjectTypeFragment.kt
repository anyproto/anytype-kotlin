package com.anytypeio.anytype.ui.objects.types.pickers

import androidx.core.os.bundleOf
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
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
            isSetSource = false,
            isWithFiles = false
        )
    }

    override fun onItemClicked(item: ObjectWrapper.Type) {
        withParent<ObjectTypeSelectionListener> {
            onSelectObjectType(objType = item)
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
}