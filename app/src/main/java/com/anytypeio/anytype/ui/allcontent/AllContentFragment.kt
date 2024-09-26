package com.anytypeio.anytype.ui.allcontent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModel
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModelFactory
import com.anytypeio.anytype.feature_allcontent.ui.AllContentMainScreen
import com.anytypeio.anytype.feature_allcontent.ui.AllContentNavigation.ALL_CONTENT_MAIN
import javax.inject.Inject

class AllContentFragment : BaseComposeFragment() {

    @Inject
    lateinit var factory: AllContentViewModelFactory

    private val vm by viewModels<AllContentViewModel> { factory }

    private val space get() = argString(ARG_SPACE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        AllContentScreenWrapper()
    }

    @Composable
    fun AllContentScreenWrapper() {
        NavHost(
            navController = rememberNavController(),
            startDestination = ALL_CONTENT_MAIN
        ) {
            composable(
                route = ALL_CONTENT_MAIN
            ) {
                AllContentMainScreen(
                    uiState = vm.uiState.collectAsStateWithLifecycle().value,
                    onTabClick = vm::onTabClicked
                )
            }
        }
    }


    override fun injectDependencies() {
        val vmParams = AllContentViewModel.VmParams(spaceId = SpaceId(space))
        componentManager().allContentComponent.get(vmParams).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().allContentComponent.release()
    }

    companion object {
        const val KEYBOARD_HIDE_DELAY = 300L

        const val ARG_SPACE = "arg.all.content.space"
        fun args(space: Id): Bundle = bundleOf(ARG_SPACE to space)
    }
}