package com.anytypeio.anytype.ui.editor.sheets

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.di.common.componentManager
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
        componentManager().objectMenuComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectMenuComponent.release(ctx)
    }

    companion object {

        fun new(
            ctx: Id,
            isArchived: Boolean,
            isProfile: Boolean,
            isFavorite: Boolean,
            isLocked: Boolean
        ) = ObjectMenuFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                IS_ARCHIVED_KEY to isArchived,
                IS_PROFILE_KEY to isProfile,
                IS_FAVORITE_KEY to isFavorite,
                IS_LOCKED_KEY to isLocked
            )
        }
    }
}