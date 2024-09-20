package com.anytypeio.anytype.feature_allcontent.presentation

import androidx.compose.runtime.Stable

@Stable
sealed class AllContentUiState {
    data object Idle : AllContentUiState()
    data object Loading : AllContentUiState()
    data class Error(val message: String) : AllContentUiState()
}