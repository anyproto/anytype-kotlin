package com.anytypeio.anytype.ui.primitives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_object_type.ui.UiIconsPickerState
import com.anytypeio.anytype.feature_object_type.ui.create.SetTypeTitlesAndIconScreen
import com.anytypeio.anytype.feature_object_type.ui.icons.ChangeIconScreen
import com.anytypeio.anytype.feature_object_type.viewmodel.CreateObjectTypeVMFactory
import com.anytypeio.anytype.feature_object_type.viewmodel.CreateObjectTypeViewModel
import com.anytypeio.anytype.feature_object_type.viewmodel.CreateTypeCommand
import com.anytypeio.anytype.feature_object_type.viewmodel.CreateTypeVmParams
import javax.inject.Inject
import kotlin.getValue
import timber.log.Timber

class CreateTypeFragment: BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: CreateObjectTypeVMFactory
    private val vm by viewModels<CreateObjectTypeViewModel> { factory }

    private val space get() = argString(ARG_SPACE_ID)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme {
            SetTypeTitlesAndIconScreen(
                uiState = vm.uiState.collectAsStateWithLifecycle().value,
                onDismiss = vm::onDismiss,
                onIconClicked = vm::onIconClicked,
                onButtonClicked = vm::onButtonClicked
            )
            IconsPickerScreen()
            LaunchedEffect(Unit) {
                vm.commands.collect{ command ->
                    when (command) {
                        CreateTypeCommand.Dismiss -> {
                            findNavController().popBackStack()
                        }

                        is CreateTypeCommand.NavigateToObjectType -> {
                            runCatching {
                                findNavController().navigate(
                                    resId = R.id.objectTypeNavigation,
                                    args = ObjectTypeFragment.args(
                                        objectId = command.id,
                                        space = command.space
                                    ),
                                    navOptions = navOptions {
                                        popUpTo(R.id.createObjectTypeScreen) {
                                            inclusive = true
                                        }
                                    }
                                )
                            }.onFailure {
                                Timber.e(it, "Failed to open object type object from create type Screen")
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun IconsPickerScreen() {
        val uiState = vm.uiIconsPickerScreen.collectAsStateWithLifecycle().value
        if (uiState is UiIconsPickerState.Visible) {
            ChangeIconScreen(
                modifier = Modifier.fillMaxSize().systemBarsPadding(),
                onDismissRequest = vm::onDismissIconPicker,
                onIconClicked = { name, color ->
                    vm.onNewIconPicked(
                        iconName = name,
                        color = color
                    )
                },
                onRemoveIconClicked = vm::onRemoveIcon
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun injectDependencies() {
        val vmParams = CreateTypeVmParams(spaceId = space)
        componentManager().createObjectTypeComponent.get(vmParams).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createObjectTypeComponent.release()
    }

    companion object {
        const val ARG_SPACE_ID = "arg.create_type.space_id"

        fun args(spaceId: Id) = bundleOf(
            ARG_SPACE_ID to spaceId
        )
    }
}