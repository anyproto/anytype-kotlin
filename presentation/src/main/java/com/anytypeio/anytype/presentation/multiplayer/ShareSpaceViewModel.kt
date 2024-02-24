package com.anytypeio.anytype.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.primitives.SpaceId
import javax.inject.Inject

class ShareSpaceViewModel(
    private val params: Params
) {

    class Factory @Inject constructor(
        private val params: Params
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShareSpaceViewModel(
            params = params
        ) as T
    }

    data class Params(
        val space: SpaceId
    )
}