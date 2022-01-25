package com.anytypeio.anytype.ui.editor

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.CreateObjectViewModel
import javax.inject.Inject

class CreateObjectFragment : BaseFragment(R.layout.fragment_create_object) {

    @Inject
    lateinit var factory: CreateObjectViewModel.Factory
    private val vm by viewModels<CreateObjectViewModel> { factory }

    private val mType get() = arg<String>(TYPE_KEY)

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.createObjectStatus) { state ->
            when (state) {
                is CreateObjectViewModel.State.Error -> {
                    activity?.toast(state.msg)
                }
                is CreateObjectViewModel.State.Success -> {
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.createObjectFragment, true)
                        .build()
                    findNavController().navigate(
                        R.id.objectNavigation,
                        bundleOf(EditorFragment.ID_KEY to state.id),
                        navOptions
                    )
                }
            }
        }
        super.onStart()
        vm.onStart(mType)
    }

    override fun injectDependencies() {
        componentManager().createObjectComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createObjectComponent.release()
    }

    companion object {
        const val TYPE_KEY = "arg.ui.editor.create.type"
    }
}