package com.anytypeio.anytype.ui.objects.types.pickers

import androidx.core.os.bundleOf
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_utils.ext.hideSoftInput
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ui.objects.BaseObjectTypeChangeFragment

/**
 * Selecting an object type by SmartBlockType.Page for the object
 */
class ObjectSelectTypeFragment : BaseObjectTypeChangeFragment() {

    override fun startWithParams() {
        vm.onStart(
            isWithCollection = false,
            isWithBookmark = false,
            excludeTypes = excludeTypes,
            selectedTypes = emptyList(),
            isSetSource = false
        )
    }

    override fun onItemClicked(id: Id, key: Key, name: String) {
        withParent<OnObjectSelectTypeAction> {
            onProceedWithUpdateType(id = id, key = key)
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
        fun newInstance(excludeTypes: List<Id>) = ObjectSelectTypeFragment().apply {
            arguments = bundleOf(ARG_EXCLUDE_TYPES to excludeTypes)
        }
    }
}