package com.anytypeio.anytype.ui.primitives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.views.BaseAlertDialog
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.setupBottomSheetBehavior
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_properties.EditTypePropertiesViewModelFactory
import com.anytypeio.anytype.feature_properties.EditTypePropertiesViewModel
import com.anytypeio.anytype.feature_properties.EditTypePropertiesViewModel.EditTypePropertiesCommand
import com.anytypeio.anytype.feature_properties.add.EditTypePropertiesVmParams
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesErrorState
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesEvent
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesEvent.OnPropertyFormatSelected
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesEvent.OnPropertyFormatsListDismiss
import com.anytypeio.anytype.feature_properties.add.ui.AddFieldScreen
import com.anytypeio.anytype.feature_properties.edit.UiPropertyFormatsListState
import com.anytypeio.anytype.feature_properties.edit.ui.PropertyFormatsListScreen
import javax.inject.Inject

class EditTypePropertiesFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var viewModelFactory: EditTypePropertiesViewModelFactory
    private val vm by viewModels<EditTypePropertiesViewModel> { viewModelFactory }
    private val space get() = argString(ARG_SPACE)
    private val typeId get() = argString(ARG_OBJECT_ID)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme {
            AddFieldScreen(
                state = vm.uiState.collectAsStateWithLifecycle().value,
                uiStateEditProperty = vm.uiPropertyEditState.collectAsStateWithLifecycle().value,
                event = vm::onEvent
            )
            PropertyFormatsScreen()
            ErrorScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ErrorScreen() {
        val errorStateScreen = vm.errorState.collectAsStateWithLifecycle().value
        if (errorStateScreen is UiEditTypePropertiesErrorState.Show) {
            when (val r = errorStateScreen.reason) {
                is UiEditTypePropertiesErrorState.Reason.ErrorAddingProperty -> {
                    BaseAlertDialog(
                        dialogText = stringResource(id = R.string.add_property_error_add),
                        buttonText = stringResource(id = R.string.membership_error_button_text_dismiss),
                        onButtonClick = vm::hideError,
                        onDismissRequest = vm::hideError
                    )
                }

                is UiEditTypePropertiesErrorState.Reason.ErrorCreatingProperty -> {
                    BaseAlertDialog(
                        dialogText = stringResource(id = R.string.add_property_error_create_new),
                        buttonText = stringResource(id = R.string.membership_error_button_text_dismiss),
                        onButtonClick = vm::hideError,
                        onDismissRequest = vm::hideError
                    )
                }

                is UiEditTypePropertiesErrorState.Reason.ErrorUpdatingProperty -> {
                    BaseAlertDialog(
                        dialogText = stringResource(id = R.string.add_property_error_update),
                        buttonText = stringResource(id = R.string.membership_error_button_text_dismiss),
                        onButtonClick = vm::hideError,
                        onDismissRequest = vm::hideError
                    )
                }

                is UiEditTypePropertiesErrorState.Reason.Other -> {
                    BaseAlertDialog(
                        dialogText = r.msg,
                        buttonText = stringResource(id = R.string.membership_error_button_text_dismiss),
                        onButtonClick = vm::hideError,
                        onDismissRequest = vm::hideError
                    )
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
                onDismissRequest = { vm.onEvent(OnPropertyFormatsListDismiss) },
                onFormatClick = { vm.onEvent(OnPropertyFormatSelected(it)) }
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetBehavior(DEFAULT_PADDING_TOP)
    }

    override fun onStart() {
        super.onStart()
        jobs += lifecycleScope.subscribe(vm.commands) { command -> execute(command) }
    }

    private fun execute(command: EditTypePropertiesCommand) {
        when (command) {
            is EditTypePropertiesCommand.Exit -> {
                findNavController().popBackStack()
            }
        }
    }

    override fun injectDependencies() {
        val params = EditTypePropertiesVmParams(
            objectTypeId = typeId,
            spaceId = SpaceId(space)

        )
        componentManager().editTypePropertiesComponent.get(params).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().editTypePropertiesComponent.release()
    }

    companion object {

        fun args(objectId: Id, space: Id) = bundleOf(
            ARG_OBJECT_ID to objectId,
            ARG_SPACE to space
        )

        const val ARG_OBJECT_ID = "arg.primitives.edit.type.property.object.id"
        const val ARG_SPACE = "arg.primitives.edit.type.property.space"

        const val DEFAULT_PADDING_TOP = 10
    }
}