package com.anytypeio.anytype.ui.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.databinding.FragmentCreateObjectBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.CreateObjectViewModel
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import javax.inject.Inject

class CreateObjectFragment : BaseFragment<FragmentCreateObjectBinding>(R.layout.fragment_create_object) {

    @Inject
    lateinit var factory: CreateObjectViewModel.Factory
    private val vm by viewModels<CreateObjectViewModel> { factory }

    private val mType get() = arg<Key>(TYPE_KEY)

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
                    val layout = state.layout
                    if (layout == ObjectType.Layout.COLLECTION || layout == ObjectType.Layout.SET) {
                        findNavController().navigate(
                            R.id.dataViewNavigation,
                            bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to state.id),
                            navOptions
                        )
                    } else {
                        findNavController().navigate(
                            R.id.objectNavigation,
                            bundleOf(EditorFragment.ID_KEY to state.id),
                            navOptions
                        )
                    }
                }
            }
        }
        super.onStart()
        vm.onStart(mType)
    }

    override fun onStop() {
        vm.onStop()
        super.onStop()
    }

    override fun injectDependencies() {
        componentManager().createObjectComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createObjectComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateObjectBinding = FragmentCreateObjectBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val TYPE_KEY = "arg.ui.editor.create.type"
    }
}