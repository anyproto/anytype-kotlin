package com.anytypeio.anytype.feature_discussions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject

class DiscussionViewModelFactory @Inject constructor(
    private val params: BaseViewModel.DefaultParams
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = DiscussionViewModel(
        params = params
    ) as T
}