package com.anytypeio.anytype.ui.payments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.screens.MainPaymentsScreen
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.viewmodel.PaymentsViewModel
import com.anytypeio.anytype.viewmodel.PaymentsViewModelFactory
import javax.inject.Inject

class PaymentsFragment: BaseBottomSheetComposeFragment()  {

    @Inject
    lateinit var factory: PaymentsViewModelFactory

    private val vm by viewModels<PaymentsViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    MainPaymentsScreen(vm.viewState.collectAsStateWithLifecycle().value)
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().paymentsComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().paymentsComponent.release()
    }
}