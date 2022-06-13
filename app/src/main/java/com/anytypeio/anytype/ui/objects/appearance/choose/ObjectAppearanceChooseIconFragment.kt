package com.anytypeio.anytype.ui.objects.appearance.choose

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseIconViewModel
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView
import javax.inject.Inject

class ObjectAppearanceChooseIconFragment :
    ObjectAppearanceChooseFragmentBase<ObjectAppearanceChooseSettingsView.Icon, ObjectAppearanceChooseIconViewModel>() {


    @Inject
    lateinit var factory: ObjectAppearanceChooseIconViewModel.Factory
    override val vm by viewModels<ObjectAppearanceChooseIconViewModel> { factory }
    override val title: Int = R.string.icon

    override fun injectDependencies() {
        componentManager().objectAppearanceIconComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectAppearanceIconComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, block: Id) = ObjectAppearanceChooseIconFragment().apply {
            arguments = bundleOf(CONTEXT_ID_KEY to ctx, BLOCK_ID_KEY to block)
        }
    }
}