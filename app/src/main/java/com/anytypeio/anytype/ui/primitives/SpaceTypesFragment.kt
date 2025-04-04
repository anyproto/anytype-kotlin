package com.anytypeio.anytype.ui.primitives

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.types.SpaceTypesViewModel
import com.anytypeio.anytype.presentation.types.SpaceTypesVmFactory
import javax.inject.Inject
import kotlin.getValue
import kotlinx.coroutines.flow.collect
import ui.space.SpaceTypesListScreen

class SpaceTypesFragment : BaseComposeFragment() {

    @Inject
    lateinit var factory: SpaceTypesVmFactory

    private val vm by viewModels<SpaceTypesViewModel> { factory }

    private val space get() = argString(ARG_SPACE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        SpaceTypesListScreen(
            uiState = vm.uiItemsState.collectAsStateWithLifecycle().value,
            onBackPressed = { },
        )
        LaunchedEffect(Unit) {
            vm.commands.collect()
        }
    }

    override fun injectDependencies() {
        val params = SpaceTypesViewModel.VmParams(
            spaceId = SpaceId(space)
        )
        componentManager().spaceTypesComponent.get(params).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().spaceTypesComponent.release()
    }

    override fun onApplyWindowRootInsets(view: View) {
        if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
            // Do nothing.
        } else {
            super.onApplyWindowRootInsets(view)
        }
    }

    companion object {
        const val ARG_SPACE = "arg.space.types.space"
        fun args(space: Id): Bundle = bundleOf(ARG_SPACE to space)
    }
}