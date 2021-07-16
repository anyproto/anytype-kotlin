package com.anytypeio.anytype.ui.sets

import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.`object`.ObjectSetMenuViewModel
import com.anytypeio.anytype.ui.page.sheets.ObjectMenuBaseFragment
import javax.inject.Inject

class ObjectSetMenuFragment : ObjectMenuBaseFragment() {

    @Inject
    lateinit var factory: ObjectSetMenuViewModel.Factory
    override val vm by viewModels<ObjectSetMenuViewModel> { factory }

    override fun onCoverClicked() {
        toast(COMING_SOON_MSG)
    }

    override fun onRelationsClicked() {
        toast(COMING_SOON_MSG)
    }

    override fun onIconClicked() {
        toast(COMING_SOON_MSG)
    }

    override fun injectDependencies() {
        componentManager().objectSetMenuComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetMenuComponent.release(ctx)
    }
}