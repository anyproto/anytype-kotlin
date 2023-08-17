package com.anytypeio.anytype.ui.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.onboarding.OnboardingViewModel
import com.anytypeio.anytype.presentation.spaces.SelectSpaceViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import timber.log.Timber

class SelectSpaceFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: SelectSpaceViewModel.Factory

    private val vm by viewModels<SelectSpaceViewModel> { factory }

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
                val spaces = vm.spaces.collectAsState().value
                Box(modifier = Modifier.padding(20.dp)) {
                    if (spaces.isNotEmpty()) {
                        LazyColumn {
                            items(spaces) { item ->
                                Text(text = item.toString())
                            }
                        }
                    } else {
                        Text(text = "Empty!")
                    }
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().selectSpaceComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().selectSpaceComponent.release()
    }
}