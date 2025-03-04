package com.anytypeio.anytype.ui.primitives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.setupBottomSheetBehavior
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_object_type.properties.add.ui.AddFieldScreen
import com.anytypeio.anytype.feature_object_type.properties.add.AddPropertyViewModel
import com.anytypeio.anytype.feature_object_type.properties.add.AddPropertyViewModel.AddPropertyCommand
import com.anytypeio.anytype.feature_object_type.properties.add.AddPropertyVmFactory
import com.anytypeio.anytype.feature_object_type.properties.add.AddPropertyVmParams
import javax.inject.Inject

class AddPropertyFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var viewModelFactory: AddPropertyVmFactory
    private val vm by viewModels<AddPropertyViewModel> { viewModelFactory }
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
                event = vm::onEvent
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

    private fun execute(command: AddPropertyCommand) {
        when (command) {
            is AddPropertyCommand.SetProperty -> {
                withParent<OnAddPropertyListener> {
                    onAddProperty(newPropertyId = command.id)
                }
            }
        }
    }

    override fun injectDependencies() {
        val params = AddPropertyVmParams(
            objectTypeId = typeId,
            spaceId = SpaceId(space)

        )
        componentManager().addPropertyComponent.get(params).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().addPropertyComponent.release()
    }

    companion object {

        fun args(objectId: Id, space: Id) = bundleOf(
            ARG_OBJECT_ID to objectId,
            ARG_SPACE to space
        )

        const val ARG_OBJECT_ID = "arg.primitives.add.property.object.id"
        const val ARG_SPACE = "arg.primitives.add.property.space"

        const val DEFAULT_PADDING_TOP = 10
    }
}

interface OnAddPropertyListener {
    fun onAddProperty(newPropertyId: Id)
}