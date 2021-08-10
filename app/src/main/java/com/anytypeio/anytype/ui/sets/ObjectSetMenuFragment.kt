package com.anytypeio.anytype.ui.sets

import androidx.fragment.app.viewModels
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.ObjectSetMenuViewModel
import com.anytypeio.anytype.ui.editor.sheets.ObjectMenuBaseFragment
import javax.inject.Inject

class ObjectSetMenuFragment : ObjectMenuBaseFragment() {

    @Inject
    lateinit var factory: ObjectSetMenuViewModel.Factory
    override val vm by viewModels<ObjectSetMenuViewModel> { factory }

    override fun injectDependencies() {
        componentManager().objectSetMenuComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetMenuComponent.release(ctx)
    }
}