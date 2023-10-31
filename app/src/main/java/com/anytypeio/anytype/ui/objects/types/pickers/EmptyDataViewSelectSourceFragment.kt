package com.anytypeio.anytype.ui.objects.types.pickers

import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.hideSoftInput
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.ObjectTypeView
import com.anytypeio.anytype.ui.objects.BaseObjectTypeChangeFragment

/**
 * Selecting a source by SmartBlockType.Page for the empty data view
 */
class EmptyDataViewSelectSourceFragment : BaseObjectTypeChangeFragment() {

    override fun startWithParams() {
        vm.onStart(
            isWithCollection = false,
            isWithBookmark = true,
            excludeTypes = emptyList(),
            selectedTypes = emptyList(),
            isSetSource = true
        )
    }

    override fun onItemClicked(item: ObjectTypeView) {
        withParent<OnDataViewSelectSourceAction> {
            onProceedWithSelectSource(id = item.id)
        }
        hideSoftInput()
        dismiss()
    }

    override fun setTitle() {
        binding.tvTitle.text = getString(R.string.select_query)
    }

    override fun injectDependencies() {
        componentManager().objectTypeChangeComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectTypeChangeComponent.release()
    }

    companion object {
        fun newInstance() = EmptyDataViewSelectSourceFragment()
    }
}