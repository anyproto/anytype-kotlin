package com.anytypeio.anytype.ui.objects.appearance.choose

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChoosePreviewLayoutViewModel
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView
import javax.inject.Inject

class ObjectAppearanceChoosePreviewLayoutFragment :
    ObjectAppearanceChooseFragmentBase<ObjectAppearanceChooseSettingsView.PreviewLayout, ObjectAppearanceChoosePreviewLayoutViewModel>() {

    @Inject
    lateinit var factory: ObjectAppearanceChoosePreviewLayoutViewModel.Factory
    override val vm by viewModels<ObjectAppearanceChoosePreviewLayoutViewModel> { factory }
    override val title: Int = R.string.preview_layout

    override fun injectDependencies() {
        componentManager().objectAppearancePreviewLayoutComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectAppearancePreviewLayoutComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, block: Id) = ObjectAppearanceChoosePreviewLayoutFragment().apply {
            arguments = bundleOf(CONTEXT_ID_KEY to ctx, BLOCK_ID_KEY to block)
        }
    }
}