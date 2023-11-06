package com.anytypeio.anytype.ui.objects.creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.CreateObjectOfTypeViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class CreateObjectOfTypeFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: CreateObjectOfTypeViewModel.Factory

    private val vm by viewModels<CreateObjectOfTypeViewModel> { factory }

    lateinit var onTypeSelected: (Key) -> Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = typography
            ) {
                CreateObjectOfTypeScreen(
                    views = vm.views.collectAsStateWithLifecycle().value,
                    onTypeClicked = {
                        onTypeSelected.invoke(it)
                    }
                )
            }
        }
    }

    override fun injectDependencies() {
        componentManager().createObjectOfTypeComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createObjectOfTypeComponent.release()
    }
}