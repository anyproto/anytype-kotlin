package com.anytypeio.anytype.ui.primitives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.types.SpaceTypesViewModel
import com.anytypeio.anytype.presentation.types.SpaceTypesVmFactory
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import kotlin.getValue
import timber.log.Timber
import com.anytypeio.anytype.feature_object_type.ui.space.SpaceTypesListScreen

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
        MaterialTheme(typography = typography) {
            SpaceTypesListScreen(
                uiState = vm.uiItemsState.collectAsStateWithLifecycle().value,
                onBackPressed = vm::onBackClicked,
                onTypeClicked = vm::onTypeClicked,
                onAddIconClicked = vm::onCreateNewTypeClicked,
                onMoveToBin = vm::onMoveToBin
            )
        }
        LaunchedEffect(Unit) {
            vm.commands.collect { command ->
                when (command) {
                    SpaceTypesViewModel.Command.Back -> {
                        runCatching {
                            findNavController().popBackStack()
                        }.onFailure { e ->
                            Timber.e(e, "Error while exiting back from all content")
                        }
                    }

                    is SpaceTypesViewModel.Command.CreateNewType -> {
                        runCatching {
                            navigation().openCreateObjectTypeScreen(spaceId = command.space)
                        }.onFailure {
                            toast("Failed to open type creation screen")
                            Timber.e(it, "Failed to open type creation screen from all content")
                        }
                    }

                    is SpaceTypesViewModel.Command.OpenType -> {
                        runCatching {
                            navigation().openObjectType(
                                objectId = command.id,
                                space = command.space
                            )
                        }.onFailure {
                            Timber.e(it, "Failed to open object type object from all content")
                        }
                    }

                    is SpaceTypesViewModel.Command.ShowToast -> {
                        runCatching {
                            toast(command.message)
                        }.onFailure {
                            Timber.e(it, "Failed to show toast message")
                        }
                    }
                }
            }
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
        // Do not apply.
    }

    companion object {
        const val ARG_SPACE = "arg.space.types.space"
        fun args(space: Id): Bundle = bundleOf(ARG_SPACE to space)
    }
}