package com.anytypeio.anytype.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.ExperimentalFeaturesViewModel
import javax.inject.Inject
import kotlin.getValue

class ExperimentalFeaturesFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: ExperimentalFeaturesViewModel.Factory

    private val vm by viewModels<ExperimentalFeaturesViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = content {
        ExperimentalFeaturesScreen(
            isCompactModeEnabled = vm.isCompactModeEnabled.collectAsStateWithLifecycle().value,
            onCompactModeToggled = vm::onCompactModeToggled
        )
    }

    override fun injectDependencies() {
        componentManager().experimentalFeaturesComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().experimentalFeaturesComponent.release()
    }
}
