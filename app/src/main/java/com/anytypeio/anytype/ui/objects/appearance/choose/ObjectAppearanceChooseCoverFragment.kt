package com.anytypeio.anytype.ui.objects.appearance.choose

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseCoverViewModel
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView
import javax.inject.Inject

class ObjectAppearanceChooseCoverFragment :
    ObjectAppearanceChooseFragmentBase<ObjectAppearanceChooseSettingsView.Cover, ObjectAppearanceChooseCoverViewModel>() {

    @Inject
    lateinit var factory: ObjectAppearanceChooseCoverViewModel.Factory

    override val vm: ObjectAppearanceChooseCoverViewModel by viewModels { factory }

    override val title: Int = R.string.cover

    override fun injectDependencies() {
        componentManager().objectAppearanceCoverComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectAppearanceCoverComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, block: Id) = ObjectAppearanceChooseCoverFragment().apply {
            arguments = bundleOf(CONTEXT_ID_KEY to ctx, BLOCK_ID_KEY to block)
        }
    }
}