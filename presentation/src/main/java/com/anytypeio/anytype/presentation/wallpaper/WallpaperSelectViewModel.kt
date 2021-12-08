package com.anytypeio.anytype.presentation.wallpaper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.wallpaper.SetWallpaper
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class WallpaperSelectViewModel(
    private val setWallpaper: SetWallpaper
) : BaseViewModel() {

    val isDismissed = MutableStateFlow(false)
    val state = MutableStateFlow<List<WallpaperSelectView>>(emptyList())

    init {
        state.value = mutableListOf<WallpaperSelectView>().apply {
            add(WallpaperSelectView.Section.SolidColor)
            addAll(
                WallpaperColor.values().map {
                    WallpaperSelectView.Wallpaper(
                        item = WallpaperView.SolidColor(it.code)
                    )
                }
            )
            add(WallpaperSelectView.Section.Gradient)
            addAll(
                CoverGradient.default.map { code ->
                    WallpaperSelectView.Wallpaper(
                        item = WallpaperView.Gradient(code)
                    )
                }
            )
        }
    }

    fun onWallpaperSelected(wallpaper: WallpaperView) {
        viewModelScope.launch {
            val params = when(wallpaper) {
                is WallpaperView.Gradient -> SetWallpaper.Params.Gradient(wallpaper.code)
                is WallpaperView.SolidColor -> SetWallpaper.Params.SolidColor(wallpaper.code)
            }
            setWallpaper(params).collect { status ->
                when(status) {
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
        private val setWallpaper: SetWallpaper
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return WallpaperSelectViewModel(
                setWallpaper = setWallpaper
            ) as T
        }
    }

}