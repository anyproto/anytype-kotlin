package com.anytypeio.anytype.presentation.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DeleteObjects
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.SharedFlow
import timber.log.Timber

class MediaViewModel(
    private val urlBuilder: UrlBuilder,
    private val setObjectListIsArchived: SetObjectListIsArchived
) : BaseViewModel() {

    private val _commands = MutableSharedFlow<Command>()
    val commands: SharedFlow<Command> = _commands

    private val _viewState = MutableStateFlow<MediaViewState>(MediaViewState.Loading)
    val viewState = _viewState.asStateFlow()

    fun processImage(objects: List<String>, index: Int = 0) {
        viewModelScope.launch {
            if (objects.isEmpty()) {
                _viewState.value = MediaViewState.Error("No image URLs provided")
                return@launch
            }

            _viewState.value = MediaViewState.ImageContent(
                images = objects.map {
                    MediaViewState.ImageContent.Image(
                        obj = it,
                        url = urlBuilder.large(it)
                    )
                },
                currentIndex = index
            )
        }
    }

    fun processVideo(obj: Id) {
        viewModelScope.launch {
            if (obj.isBlank()) {
                _viewState.value = MediaViewState.Error("No video URL provided")
                return@launch
            }

            _viewState.value = MediaViewState.VideoContent(
                url = urlBuilder.original(obj)
            )
        }
    }

    fun processAudio(obj: Id, name: String = "") {
        viewModelScope.launch {
            val hash = urlBuilder.original(obj)
            if (hash.isBlank()) {
                _viewState.value = MediaViewState.Error("No audio URL provided")
                return@launch
            }

            _viewState.value = MediaViewState.AudioContent(
                url = hash,
                name = name
            )
        }
    }

    fun onDeleteObject(id: Id) {
        viewModelScope.launch {
            setObjectListIsArchived.async(
                params = SetObjectListIsArchived.Params(
                    targets = listOf(id),
                    isArchived = true
                )
            ).onFailure {
                Timber.e(it, "Error while archiving media object")
            }.onSuccess {
                _commands.emit(Command.Dismiss)
            }
        }
    }

    sealed class MediaViewState {
        data object Loading : MediaViewState()
        data class Error(val message: String) : MediaViewState()
        data class ImageContent(
            val images: List<Image>,
            val currentIndex: Int
        ) : MediaViewState() {
            data class Image(
                val obj: Id,
                val url: String
            )
        }

        data class VideoContent(
            val url: String
        ) : MediaViewState()

        data class AudioContent(
            val url: String,
            val name: String
        ) : MediaViewState()
    }

    sealed class Command {
        data object Dismiss : Command()
    }

    class Factory @Inject constructor(
        private val urlBuilder: UrlBuilder,
        private val setObjectListIsArchived: SetObjectListIsArchived
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MediaViewModel(
                urlBuilder = urlBuilder,
                setObjectListIsArchived = setObjectListIsArchived
            ) as T
        }
    }
}