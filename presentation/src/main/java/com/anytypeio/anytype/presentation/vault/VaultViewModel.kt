package com.anytypeio.anytype.presentation.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class VaultViewModel(
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    private val urlBuilder: UrlBuilder
) : BaseViewModel() {

    val spaces = MutableStateFlow<List<VaultSpaceView>>(emptyList())

    init {
        Timber.i("VaultViewModel, init")
        viewModelScope.launch {
            spaceViewSubscriptionContainer
                .observe()
                .map { spaces ->
                    spaces.map { space ->
                        VaultSpaceView(
                            space = space,
                            icon = space.spaceIcon(
                                builder = urlBuilder,
                                spaceGradientProvider = SpaceGradientProvider.Default
                            )
                        )
                    }
                }.collect {
                    spaces.value = it
                }
        }
    }

    class Factory @Inject constructor(
        private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = VaultViewModel(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            urlBuilder = urlBuilder
        ) as T
    }


    data class VaultSpaceView(
        val space: ObjectWrapper.SpaceView,
        val icon: SpaceIconView
    )
}