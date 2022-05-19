package com.anytypeio.anytype.presentation.wallpaper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.wallpaperSet
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.cover.GetCoverGradientCollection
import com.anytypeio.anytype.domain.wallpaper.SetWallpaper
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class WallpaperSelectViewModel(
    private val setWallpaper: SetWallpaper,
    private val analytics: Analytics,
    private val getGradients: GetCoverGradientCollection,
) : BaseViewModel() {

    val isDismissed = MutableStateFlow(false)
    val state = MutableStateFlow<List<WallpaperSelectView>>(emptyList())

    init {
        state.value = buildList {
            add(WallpaperSelectView.Section.Gradient)
            addAll(
                getGradients.provide().map { code ->
                    WallpaperSelectView.Wallpaper(
                        item = WallpaperView.Gradient(code)
                    )
                }
            )
            add(WallpaperSelectView.Section.SolidColor)
            addAll(
                WallpaperColor.values().map {
                    WallpaperSelectView.Wallpaper(
                        item = WallpaperView.SolidColor(it.code)
                    )
                }
            )
        }

        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.wallpaperScreenShow
        )
    }

    fun onWallpaperSelected(wallpaper: WallpaperView) {
        viewModelScope.launch {
            sendEvent(
                analytics = analytics,
                eventName = wallpaperSet
            )
            val params = when (wallpaper) {
                is WallpaperView.Gradient -> SetWallpaper.Params.Gradient(wallpaper.code)
                is WallpaperView.SolidColor -> SetWallpaper.Params.SolidColor(wallpaper.code)
            }
            setWallpaper(params).collect { status ->
                when (status) {
                    is Interactor.Status.Error -> {

                    }
                    is Interactor.Status.Started -> {

                    }
                    is Interactor.Status.Success -> {
                        isDismissed.value = true
                    }
                }
            }
        }
    }

    class Factory(
        private val setWallpaper: SetWallpaper,
        private val analytics: Analytics,
        private val getGradients: GetCoverGradientCollection,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WallpaperSelectViewModel(
                setWallpaper = setWallpaper,
                analytics = analytics,
                getGradients = getGradients
            ) as T
        }
    }

}