package com.anytypeio.anytype.ui.editor.sheets

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.core_utils.ext.subscribe
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
                if (isArchived) parentFragment?.findNavController()?.popBackStack()
            }
        }
    }

    override fun injectDependencies() {
        componentManager().objectMenuComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectMenuComponent.release(ctx)
    }
}