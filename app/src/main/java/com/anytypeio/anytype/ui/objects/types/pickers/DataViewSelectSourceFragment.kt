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
 * Selecting a source by SmartBlockType.Page for the non-empty data view
 */
class DataViewSelectSourceFragment : BaseObjectTypeChangeFragment() {

    override fun startWithParams() {
        vm.onStart(
            isWithCollection = true,
            isWithBookmark = true,
            excludeTypes = emptyList(),
            selectedTypes = selectedTypes,
            isSetSource = true,
            isWithFiles = true
        )
    }

    override fun onItemClicked(item: ObjectWrapper.Type) {
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
        fun newInstance(selectedTypes: List<Id>) = DataViewSelectSourceFragment().apply {
            arguments = bundleOf(ARG_SELECTED_TYPES to selectedTypes)
        }
    }
}