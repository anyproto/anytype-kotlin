package com.anytypeio.anytype.ui.primitives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.setupBottomSheetBehavior
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_object_type.fields.ui.FieldsMainModalScreen
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeCommand
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeVmParams
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeVMFactory
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeViewModel
import javax.inject.Inject
import kotlin.getValue
import timber.log.Timber

class ObjectTypeFieldsFragment : BaseBottomSheetComposeFragment()  {

    @Inject
    lateinit var factory: ObjectTypeVMFactory

    private val vm by viewModels<ObjectTypeViewModel> { factory }

    private val space get() = argString(ARG_SPACE)
    private val typeId get() = argString(ARG_OBJECT_ID)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme {
            FieldsMainModalScreen(
                uiFieldsListState = vm.uiTypePropertiesListState.collectAsStateWithLifecycle().value,
                uiTitleState = vm.uiTitleState.collectAsStateWithLifecycle().value,
                uiIconState = vm.uiIconState.collectAsStateWithLifecycle().value,
                uiEditPropertyState = vm.uiEditPropertyScreen.collectAsStateWithLifecycle().value,
                uiFieldLocalInfoState = vm.uiFieldLocalInfoState.collectAsStateWithLifecycle().value,
                fieldEvent = vm::onFieldEvent
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetBehavior(DEFAULT_PADDING_TOP)
        subscribe(vm.commands) { command ->
            Timber.d("Received command: $command")
            when (command) {
                is ObjectTypeCommand.OpenAddNewPropertyScreen -> {
                    runCatching {
                        findNavController().navigate(
                            R.id.editTypePropertiesScreen,
                            EditTypePropertiesFragment.args(
                                objectId = command.typeId,
                                space = command.space
                            )
                        )
                    }.onFailure {
                        Timber.e(it, "Error while opening edit object type properties screen")
                    }
                }
                is ObjectTypeCommand.ShowToast -> {
                    this.toast(command.msg)
                }
                else -> {
                    // do nothing
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart()
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    override fun injectDependencies() {
        val params = ObjectTypeVmParams(
            spaceId = SpaceId(space),
            objectId = typeId,
            showHiddenFields = true
        )
        componentManager().objectTypeComponent.get(params).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectTypeComponent.release()
    }

    companion object {
        const val ARG_SPACE = "arg.object.type.space"
        const val ARG_OBJECT_ID = "arg.object.type.object_id"

        fun args(space: Id, objectId: Id) = bundleOf(
            ARG_SPACE to space,
            ARG_OBJECT_ID to objectId
        )
    }
}