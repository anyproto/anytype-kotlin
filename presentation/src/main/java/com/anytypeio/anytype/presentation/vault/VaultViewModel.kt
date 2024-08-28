package com.anytypeio.anytype.presentation.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.wallpaper.GetSpaceWallpapers
import com.anytypeio.anytype.domain.workspace.SpaceManager
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
    private val urlBuilder: UrlBuilder,
    private val getSpaceWallpapers: GetSpaceWallpapers,
    private val spaceManager: SpaceManager
) : BaseViewModel() {

    val spaces = MutableStateFlow<List<VaultSpaceView>>(emptyList())

    init {
        Timber.i("VaultViewModel, init")
        viewModelScope.launch {
            val wallpapers = getSpaceWallpapers.async(Unit).getOrNull() ?: emptyMap()
            spaceViewSubscriptionContainer
                .observe()
                .map { spaces ->
                    spaces
                        .filter { space ->
                            space.spaceLocalStatus == SpaceStatus.OK
                                    && !space.spaceAccountStatus.isDeletedOrRemoving()
                        }
                        .map { space ->
                            VaultSpaceView(
                                space = space,
                                icon = space.spaceIcon(
                                    builder = urlBuilder,
                                    spaceGradientProvider = SpaceGradientProvider.Default
                                ),
                                wallpaper = wallpapers.getOrDefault(
                                    key = space.targetSpaceId,
                                    defaultValue = Wallpaper.Default
                                )
                            )
                        }
                }.collect {
                    spaces.value = it
                }
        }
    }

    fun onSpaceClicked(view: VaultSpaceView) {
        Timber.i("onSpaceClicked: $view")
        viewModelScope.launch {
            spaceManager.set(
                view.space.targetSpaceId!!
            )
        }
    }

    class Factory @Inject constructor(
        private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
        private val getSpaceWallpapers: GetSpaceWallpapers,
        private val urlBuilder: UrlBuilder,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = VaultViewModel(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            getSpaceWallpapers = getSpaceWallpapers,
            urlBuilder = urlBuilder,
            spaceManager = spaceManager
        ) as T
    }


    data class VaultSpaceView(
        val space: ObjectWrapper.SpaceView,
        val icon: SpaceIconView,
        val wallpaper: Wallpaper = Wallpaper.Default
    )
}