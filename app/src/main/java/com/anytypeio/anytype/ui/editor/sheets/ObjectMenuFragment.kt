package com.anytypeio.anytype.ui.editor.sheets

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.di.feature.DefaultComponentParam
import com.anytypeio.anytype.presentation.objects.menu.ObjectMenuViewModel
import javax.inject.Inject

class ObjectMenuFragment : ObjectMenuBaseFragment() {

    @Inject
    lateinit var factory: ObjectMenuViewModel.Factory
    override val vm by viewModels<ObjectMenuViewModel> { factory }

    override fun onStart() {
        super.onStart()
        with(lifecycleScope) {
            subscribe(vm.isObjectArchived) { isArchived ->
                if (isArchived) {
                    withParent<DocumentMenuActionReceiver> { onMoveToBinSuccess() }
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager()
            .objectMenuComponent
            .get(
                params = DefaultComponentParam(
                    ctx = ctx,
                    space = SpaceId(space)
                )
            )
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectMenuComponent.release()
    }

    companion object {

        fun new(
            ctx: Id,
            space: Id,
            isArchived: Boolean,
            isFavorite: Boolean,
            isLocked: Boolean,
            fromName: String?,
            isTemplate: Boolean
        ) = ObjectMenuFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                SPACE_KEY to space,
                IS_ARCHIVED_KEY to isArchived,
                IS_FAVORITE_KEY to isFavorite,
                IS_LOCKED_KEY to isLocked,
                FROM_NAME to fromName,
                IS_TEMPLATE_KEY to isTemplate
            )
        }
    }
}