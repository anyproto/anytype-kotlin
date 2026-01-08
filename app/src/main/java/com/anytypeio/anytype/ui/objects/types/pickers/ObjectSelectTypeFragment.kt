package com.anytypeio.anytype.ui.objects.types.pickers

import androidx.core.os.bundleOf
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.withParentSafe
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModel
import com.anytypeio.anytype.ui.objects.BaseObjectTypeChangeFragment

/**
 * Selecting an object type by SmartBlockType.Page for the object
 */
class ObjectSelectTypeFragment : BaseObjectTypeChangeFragment() {

    override fun onItemClicked(item: ObjectWrapper.Type) {
        withParentSafe<ObjectTypeSelectionListener> {
            onSelectObjectType(objType = item)
        }
    }

    override fun resolveTitle(): String = getString(R.string.change_type)

    override fun injectDependencies() {
        val params = ObjectTypeChangeViewModel.VmParams(
            spaceId = SpaceId(space),
            screen = ObjectTypeChangeViewModel.Screen.OBJECT_TYPE_CHANGE,
            excludeTypes = excludeTypes
        )
        componentManager().objectTypeChangeComponent.get(params).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectTypeChangeComponent.release()
    }

    companion object {
        fun newInstance(space: Id, excludeTypes: List<Id>) = ObjectSelectTypeFragment().apply {
            arguments = bundleOf(
                ARG_SPACE to space,
                ARG_EXCLUDE_TYPES to excludeTypes
            )
        }
    }
}
