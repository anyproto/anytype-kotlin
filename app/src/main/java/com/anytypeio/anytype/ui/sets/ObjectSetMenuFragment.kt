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
                    val navController = findNavController()
                    when {
                        navController.popBackStack(R.id.objectSetScreen, true) -> {
                            // Successfully returned to objectSetScreen and removed it from the stack
                        }
                        navController.popBackStack(R.id.homeScreen, false) -> {
                            // Successfully returned to homeScreen without removing it from the stack
                        }
                        else -> {
                            // homeScreen is not found in the stack, navigate to it
                            navController.navigate(R.id.homeScreen)
                        }
                    }
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