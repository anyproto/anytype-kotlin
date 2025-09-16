package com.anytypeio.anytype.presentation.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MediaViewModel(
    private val urlBuilder: UrlBuilder
) : BaseViewModel() {

    private val _viewState = MutableStateFlow<MediaViewState>(MediaViewState.Loading)
    val viewState = _viewState.asStateFlow()

    fun processImage(urls: List<String>, index: Int = 0) {
        viewModelScope.launch {
            if (urls.isEmpty()) {
                _viewState.value = MediaViewState.Error("No image URLs provided")
                return@launch
            }

            _viewState.value = MediaViewState.ImageContent(
                urls = urls,
                currentIndex = index
        )
    }
}

sealed class MediaViewState {
    data object Loading : MediaViewState()
    data class Error(val message: String) : MediaViewState()
    data class ImageContent(
        val urls: List<String>,
        val currentIndex: Int
    ) : MediaViewState()
    data class VideoContent(
        val url: String
    ) : MediaViewState()
    data class AudioContent(
        val url: String,
        val name: String
    ) : MediaViewState()
}

    fun processVideo(url: String) {
        viewModelScope.launch {
            if (url.isBlank()) {
                _viewState.value = MediaViewState.Error("No video URL provided")
                return@launch
            }

            _viewState.value = MediaViewState.VideoContent(
                url = url
            )
        }
    }

    fun processAudio(url: String, name: String = "") {
        viewModelScope.launch {
            if (url.isBlank()) {
                _viewState.value = MediaViewState.Error("No audio URL provided")
                return@launch
            }

            _viewState.value = MediaViewState.AudioContent(
                url = url,
                name = name
            )
        }
    }

    class Factory @Inject constructor(
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MediaViewModel(
                urlBuilder = urlBuilder
            ) as T
        }
    }
}