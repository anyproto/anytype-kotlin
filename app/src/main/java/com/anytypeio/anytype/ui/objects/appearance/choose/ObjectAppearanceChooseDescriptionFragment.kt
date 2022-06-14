package com.anytypeio.anytype.ui.objects.appearance.choose

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseDescriptionViewModel
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView
import javax.inject.Inject

class ObjectAppearanceChooseDescriptionFragment :
    ObjectAppearanceChooseFragmentBase<ObjectAppearanceChooseSettingsView.Description, ObjectAppearanceChooseDescriptionViewModel>() {

    @Inject
    lateinit var factory: ObjectAppearanceChooseDescriptionViewModel.Factory
    override val vm by viewModels<ObjectAppearanceChooseDescriptionViewModel> { factory }
    override val title: Int = R.string.description

    override fun injectDependencies() {
        componentManager().objectAppearanceChooseDescriptionComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectAppearanceChooseDescriptionComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, block: Id) = ObjectAppearanceChooseDescriptionFragment().apply {
            arguments = bundleOf(CONTEXT_ID_KEY to ctx, BLOCK_ID_KEY to block)
        }
    }
}