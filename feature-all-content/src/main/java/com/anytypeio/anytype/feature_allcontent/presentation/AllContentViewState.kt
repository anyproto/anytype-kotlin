package com.anytypeio.anytype.feature_allcontent.presentation

import androidx.compose.runtime.Stable
import com.anytypeio.anytype.core_models.ObjectWrapper

@Stable
sealed class AllContentUiState {
    data object Idle : AllContentUiState()
    data object Loading : AllContentUiState()
    data class Error(val message: String) : AllContentUiState()
    data class Content(val items: List<ObjectWrapper>) : AllContentUiState()
}