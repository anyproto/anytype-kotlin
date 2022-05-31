package com.anytypeio.anytype.ui.sets

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.menu.ObjectSetMenuViewModel
import com.anytypeio.anytype.ui.editor.sheets.ObjectMenuBaseFragment
import javax.inject.Inject

class ObjectSetMenuFragment : ObjectMenuBaseFragment() {

    @Inject
    lateinit var factory: ObjectSetMenuViewModel.Factory
    override val vm by viewModels<ObjectSetMenuViewModel> { factory }

    override fun onStart() {
        super.onStart()
        with(lifecycleScope) {
            subscribe(vm.isObjectArchived) { isArchived ->
                if (isArchived) {
                    findNavController().popBackStack(R.id.objectSetScreen, true)
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().objectSetMenuComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetMenuComponent.release(ctx)
    }
}