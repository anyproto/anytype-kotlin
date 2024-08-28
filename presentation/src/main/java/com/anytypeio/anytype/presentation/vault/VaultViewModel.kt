package com.anytypeio.anytype.presentation.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.wallpaper.GetSpaceWallpapers
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class VaultViewModel(
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val getSpaceWallpapers: GetSpaceWallpapers,
    private val spaceManager: SpaceManager,
    private val saveCurrentSpace: SaveCurrentSpace,
) : BaseViewModel() {

    val spaces = MutableStateFlow<List<VaultSpaceView>>(emptyList())
    val commands = MutableSharedFlow<Command>(replay = 0)

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
        Timber.i("onSpaceClicked")
        viewModelScope.launch {
            val targetSpace = view.space.targetSpaceId
            if (targetSpace != null) {
                spaceManager.set(targetSpace).fold(
                    onFailure = {
                        Timber.e(it, "Could not select space")
                    },
                    onSuccess = {
                        proceedWithSavingCurrentSpace(targetSpace)
                    }
                )
            } else {
                Timber.e("Missing target space")
            }
        }
    }

    fun onSettingsClicked() {
        viewModelScope.launch {
            val entrySpaceView = spaces.value.find { space ->
                space.space.spaceAccessType == SpaceAccessType.DEFAULT
            }
            if (entrySpaceView != null && entrySpaceView.space.targetSpaceId != null) {
                commands.emit(
                    Command.OpenProfileSettings(
                        space = SpaceId(requireNotNull(entrySpaceView.space.targetSpaceId))
                    )
                )
            } else {
                Timber.w("Entry space not found")
            }
        }
    }

    fun onCreateSpaceClicked() {
        viewModelScope.launch {
            commands.emit(Command.CreateNewSpace)
        }
    }

    private suspend fun proceedWithSavingCurrentSpace(targetSpace: String) {
        saveCurrentSpace.async(
            SaveCurrentSpace.Params(SpaceId(targetSpace))
        ).fold(
            onFailure = {
                Timber.e(it, "Error while saving current space on vault screen")
            },
            onSuccess = {
                commands.emit(Command.EnterSpaceHomeScreen)
            }
        )
    }

    class Factory @Inject constructor(
        private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
        private val getSpaceWallpapers: GetSpaceWallpapers,
        private val urlBuilder: UrlBuilder,
        private val spaceManager: SpaceManager,
        private val saveCurrentSpace: SaveCurrentSpace,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = VaultViewModel(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            getSpaceWallpapers = getSpaceWallpapers,
            urlBuilder = urlBuilder,
            spaceManager = spaceManager,
            saveCurrentSpace = saveCurrentSpace
        ) as T
    }

    data class VaultSpaceView(
        val space: ObjectWrapper.SpaceView,
        val icon: SpaceIconView,
        val wallpaper: Wallpaper = Wallpaper.Default
    )

    sealed class Command {
        data object EnterSpaceHomeScreen: Command()
        data object CreateNewSpace: Command()
        data class OpenProfileSettings(val space: SpaceId): Command()
    }
}