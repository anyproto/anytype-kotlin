package com.anytypeio.anytype.ui.objects.appearance.choose

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.di.feature.DefaultComponentParam
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
        componentManager()
            .objectAppearanceIconComponent
            .get(
                DefaultComponentParam(
                    ctx = ctx,
                    space = SpaceId(space)
                )
            )
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectAppearanceIconComponent.release()
    }

    companion object {
        fun new(ctx: Id, space: Id, block: Id) = ObjectAppearanceChooseIconFragment().apply {
            arguments = bundleOf(
                CONTEXT_ID_KEY to ctx,
                SPACE_KEY to space,
                BLOCK_ID_KEY to block
            )
        }
    }
}