package com.anytypeio.anytype.feature_allcontent.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import com.anytypeio.anytype.core_models.primitives.SpaceId

class AllContentViewModel(private val vmParams: VmParams) : ViewModel() {

    data class VmParams(
        val spaceId: SpaceId
    )
}

@Stable
sealed class AllContentUiState {
    data object Idle : AllContentUiState()
    data object Loading : AllContentUiState()
    data class Error(val message: String) : AllContentUiState()
}