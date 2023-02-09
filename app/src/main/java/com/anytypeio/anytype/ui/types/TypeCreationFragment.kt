package com.anytypeio.anytype.ui.types


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.types.TypeCreationViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class TypeCreationFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: TypeCreationViewModel.Factory

    private val vm by viewModels<TypeCreationViewModel> { factory }

    private val preparedName get() = arg<Id>(ARG_TYPE_NAME)

    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(typography = typography) {
                TypeCreationScreen(vm, preparedName)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribe(vm.navigation) {
            when (it) {
                is TypeCreationViewModel.Navigation.Back -> {
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().typeCreationComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().typeCreationComponent.release()
    }

    companion object {
        fun args(typeName: String) = bundleOf(ARG_TYPE_NAME to typeName)
    }

}

private const val ARG_TYPE_NAME = "arg.type_name"

