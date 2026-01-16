package com.anytypeio.anytype.ui.objects.types.pickers

import androidx.core.os.bundleOf
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.withParentSafe
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModel
import com.anytypeio.anytype.ui.objects.BaseObjectTypeChangeFragment

/**
 * Fragment for changing object type in the editor.
 * Excludes: Lists (Set, Collection), Participants, Templates.
 */
class EditorObjectTypeUpdateFragment : BaseObjectTypeChangeFragment() {

    private val fromFeatured: Boolean
        get() = argOrNull<Boolean>(ARG_FROM_FEATURED) == true

    override fun onItemClicked(item: ObjectWrapper.Type) {
        withParentSafe<ObjectTypeUpdateListener> {
            onUpdateObjectType(objType = item, fromFeatured = fromFeatured)
        }
    }

    override fun resolveTitle(): String = getString(R.string.change_type)

    override fun injectDependencies() {
        val params = ObjectTypeChangeViewModel.VmParams(
            spaceId = SpaceId(space),
            screen = ObjectTypeChangeViewModel.Screen.EDITOR_OBJECT_TYPE_UPDATE,
            excludeTypes = excludeTypes
        )
        componentManager().objectTypeChangeComponent.get(params).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectTypeChangeComponent.release()
    }

    companion object {
        private const val ARG_FROM_FEATURED = "arg.editor-object-type-update.from-featured"

        fun newInstance(
            space: Id,
            fromFeatured: Boolean = false
        ) = EditorObjectTypeUpdateFragment().apply {
            arguments = bundleOf(
                ARG_SPACE to space,
                ARG_FROM_FEATURED to fromFeatured
            )
        }
    }
}
