package com.anytypeio.anytype.ui.primitives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_object_type.ui.create.CreateNewTypeScreen
import com.anytypeio.anytype.feature_object_type.viewmodel.CreateObjectTypeVMFactory
import com.anytypeio.anytype.feature_object_type.viewmodel.CreateObjectTypeViewModel
import javax.inject.Inject
import kotlin.getValue

class CreateTypeFragment: BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: CreateObjectTypeVMFactory

    private val vm by viewModels<CreateObjectTypeViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme {
            CreateNewTypeScreen(
                uiState = vm.uiState.collectAsStateWithLifecycle().value,
                onDismiss = {  },
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun injectDependencies() {
        componentManager().createObjectTypeComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createObjectTypeComponent.release()
    }
}