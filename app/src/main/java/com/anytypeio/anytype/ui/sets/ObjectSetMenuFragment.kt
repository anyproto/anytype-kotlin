package com.anytypeio.anytype.ui.sets

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.`object`.ObjectSetMenuViewModel
import com.anytypeio.anytype.ui.page.modals.ObjectIconPickerBaseFragment
import com.anytypeio.anytype.ui.page.sheets.ObjectMenuBaseFragment
import javax.inject.Inject

class ObjectSetMenuFragment : ObjectMenuBaseFragment() {

    @Inject
    lateinit var factory: ObjectSetMenuViewModel.Factory
    override val vm by viewModels<ObjectSetMenuViewModel> { factory }

    override fun onCoverClicked() {
        toast(COMING_SOON_MSG)
    }

    override fun proceedWithRelationsScreen() {
        toast(COMING_SOON_MSG)
    }

    override fun onIconClicked() {
        findNavController().navigate(
            R.id.objectSetIconPickerScreen,
            bundleOf(
                ObjectIconPickerBaseFragment.ARG_CONTEXT_ID_KEY to ctx,
                ObjectIconPickerBaseFragment.ARG_TARGET_ID_KEY to ctx,
            )
        )
    }

    override fun injectDependencies() {
        componentManager().objectSetMenuComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetMenuComponent.release(ctx)
    }
}