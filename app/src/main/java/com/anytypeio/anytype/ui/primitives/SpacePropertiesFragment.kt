package com.anytypeio.anytype.ui.primitives

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState
import com.anytypeio.anytype.feature_properties.edit.UiPropertyFormatsListState
import com.anytypeio.anytype.feature_properties.edit.ui.PropertyFormatsListScreen
import com.anytypeio.anytype.feature_properties.edit.ui.PropertyScreen
import com.anytypeio.anytype.feature_properties.space.SpacePropertiesEvent
import com.anytypeio.anytype.feature_properties.space.SpacePropertiesViewModel
import com.anytypeio.anytype.feature_properties.space.SpacePropertiesVmFactory
import com.anytypeio.anytype.feature_properties.space.ui.SpacePropertiesListScreen
import javax.inject.Inject
import kotlin.getValue
import timber.log.Timber

class SpacePropertiesFragment : BaseComposeFragment() {

    @Inject
    lateinit var factory: SpacePropertiesVmFactory

    private val vm by viewModels<SpacePropertiesViewModel> { factory }

    private val space get() = argString(ARG_SPACE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        SpacePropertiesListScreen(
            uiState = vm.uiItemsState.collectAsStateWithLifecycle().value,
            onBackPressed = vm::onBackClicked,
            onPropertyClicked = vm::onPropertyClicked,
            onAddIconClicked = vm::onCreateNewPropertyClicked
        )
        SpacePropertyScreen(
            uiState = vm.uiEditPropertyScreen.collectAsStateWithLifecycle().value
        )
        PropertyFormatsScreen()

        LaunchedEffect(Unit) {
            vm.commands.collect { command ->
                when (command) {
                    SpacePropertiesViewModel.Command.Back -> {
                        runCatching {
                            findNavController().popBackStack()
                        }.onFailure { e ->
                            Timber.e(e, "Error while exiting back from all content")
                        }
                    }

                    is SpacePropertiesViewModel.Command.ShowToast -> {
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

    @Composable
    private fun PropertyFormatsScreen() {
        val uiState = vm.uiPropertyFormatsListState.collectAsStateWithLifecycle().value
        if (uiState is UiPropertyFormatsListState.Visible) {
            PropertyFormatsListScreen(
                uiState = uiState,
                onDismissRequest = { vm.proceedWithEditPropertyEvent(SpacePropertiesEvent.OnPropertyFormatsListDismiss) },
                onFormatClick = {
                    vm.proceedWithEditPropertyEvent(
                        SpacePropertiesEvent.OnPropertyFormatSelected(
                            it
                        )
                    )
                },
            )
        }
    }

    @Composable
    private fun SpacePropertyScreen(uiState: UiEditPropertyState) {
        if (uiState is UiEditPropertyState.Visible) {
            PropertyScreen(uiState = uiState,
                modifier = Modifier.fillMaxWidth(),
                onDismissRequest = vm::onDismissPropertyScreen,
                onFormatClick = { vm.proceedWithEditPropertyEvent(SpacePropertiesEvent.OnPropertyFormatClick) },
                onLimitObjectTypesDoneClick = {
                    vm.proceedWithEditPropertyEvent(
                        SpacePropertiesEvent.OnLimitTypesDoneClick(it)
                    )
                },
                onSaveButtonClicked = { vm.proceedWithEditPropertyEvent(SpacePropertiesEvent.OnSaveButtonClicked) },
                onCreateNewButtonClicked = { vm.proceedWithEditPropertyEvent(SpacePropertiesEvent.OnCreateNewButtonClicked) },
                onPropertyNameUpdate = {
                    vm.proceedWithEditPropertyEvent(
                        SpacePropertiesEvent.OnPropertyNameUpdate(name = it)
                    )
                },
                onLimitTypesClick = { vm.proceedWithEditPropertyEvent(SpacePropertiesEvent.OnLimitTypesClick) },
                onDismissLimitTypes = { vm.proceedWithEditPropertyEvent(SpacePropertiesEvent.OnLimitTypesDismiss) })
        }
    }

    override fun injectDependencies() {
        val params = SpacePropertiesViewModel.VmParams(
            spaceId = SpaceId(space)
        )
        componentManager().spacePropertiesComponent.get(params).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().spacePropertiesComponent.release()
    }

    override fun onApplyWindowRootInsets(view: View) {
        if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
            // Do nothing.
        } else {
            super.onApplyWindowRootInsets(view)
        }
    }

    companion object {
        const val ARG_SPACE = "arg.space.properties.space"
        fun args(space: Id): Bundle = bundleOf(ARG_SPACE to space)
    }
}