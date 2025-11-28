package com.anytypeio.anytype.presentation.media

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.download.DownloadFile
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.FetchObject
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.SharedFlow
import timber.log.Timber
import kotlinx.coroutines.async

class MediaViewModel(
    private val urlBuilder: UrlBuilder,
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val downloadFile: DownloadFile,
    private val fetchObject: FetchObject
) : BaseViewModel() {

    private val _commands = MutableSharedFlow<Command>()
    val commands: SharedFlow<Command> = _commands

    private val _viewState = MutableStateFlow<MediaViewState>(MediaViewState.Loading)
    val viewState = _viewState.asStateFlow()

    fun processImage(objects: List<String>, index: Int = 0, space: SpaceId) {
        viewModelScope.launch {
            if (objects.isEmpty()) {
                _viewState.value = MediaViewState.Error("No image object IDs provided")
                return@launch
            }

            // Fetch archived status for all images
            val imagesWithArchived = objects.map { id ->
                async {
                    val obj = fetchObject.async(
                        params = FetchObject.Params(
                            space = space,
                            obj = id,
                            keys = listOf(Relations.ID, Relations.IS_ARCHIVED)
                        )
                    ).getOrNull()

                    if (obj == null) {
                        Timber.w("Image object not found: $id")
                    } else {
                        Timber.d("Image object found: $obj")
                    }

                    val isArchived = obj?.let { ObjectWrapper.Basic(it.map).isArchived } ?: true
                    MediaViewState.ImageContent.Image(
                        obj = id,
                        url = urlBuilder.large(id),
                        isArchived = isArchived
                    )
                }
            }.map { it.await() }

            Timber.d("Images with archived status: $imagesWithArchived")

            _viewState.value = MediaViewState.ImageContent(
                images = imagesWithArchived,
                currentIndex = index
            )
        }
    }

    fun processVideo(obj: Id, space: SpaceId) {
        viewModelScope.launch {
            if (obj.isBlank()) {
                _viewState.value = MediaViewState.Error("No video object ID provided")
                return@launch
            }

            val fetchedObj = fetchObject.async(
                params = FetchObject.Params(
                    space = space,
                    obj = obj,
                    keys = listOf(Relations.ID, Relations.IS_ARCHIVED)
                )
            ).getOrNull()
            val isArchived = fetchedObj?.let { ObjectWrapper.Basic(it.map).isArchived } ?: false

            _viewState.value = MediaViewState.VideoContent(
                url = urlBuilder.original(obj),
                isArchived = isArchived
            )
        }
    }

    fun processAudio(obj: Id, name: String = "", space: SpaceId) {
        viewModelScope.launch {
            val hash = urlBuilder.original(obj)
            if (hash.isBlank()) {
                _viewState.value = MediaViewState.Error("No audio object ID provided")
                return@launch
            }

            val fetchedObj = fetchObject.async(
                params = FetchObject.Params(
                    space = space,
                    obj = obj,
                    keys = listOf(Relations.ID, Relations.IS_ARCHIVED)
                )
            ).getOrNull()
            val isArchived = fetchedObj?.let { ObjectWrapper.Basic(it.map).isArchived } ?: false

            _viewState.value = MediaViewState.AudioContent(
                url = hash,
                name = name,
                isArchived = isArchived
            )
        }
    }

    fun onDeleteObject(id: Id) {
        Timber.d("onDeleteObject: $id")
        viewModelScope.launch {
            setObjectListIsArchived.async(
                params = SetObjectListIsArchived.Params(
                    targets = listOf(id),
                    isArchived = true
                )
            ).onFailure { error ->
                Timber.e(error, "Error while archiving media object").also {
                    _commands.emit(
                        Command.ShowToast.Generic("Error: ${error.message}")
                    )
                }
            }.onSuccess {
                _commands.emit(Command.ShowToast.MovedToBin)
                _commands.emit(Command.Dismiss)
            }
        }
    }

    fun onRestoreObjectClicked(id: Id) {
        viewModelScope.launch {
            setObjectListIsArchived.async(
                params = SetObjectListIsArchived.Params(
                    targets = listOf(id),
                    isArchived = false
                )
            ).onFailure { error ->
                Timber.e(error, "Error while restoring media object").also {
                    _commands.emit(
                        Command.ShowToast.Generic("Error: ${error.message}")
                    )
                }
            }.onSuccess {
                _commands.emit(Command.ShowToast.Restored)
                _commands.emit(Command.Dismiss)
            }
        }
    }

    fun onDownloadObject(id: Id, space: SpaceId) {
        Timber.d("onDownload: $id, space: $space")
        viewModelScope.launch {
            val obj = fetchObject.async(
                params = FetchObject.Params(
                    space = space,
                    obj = id,
                    keys = listOf(
                        Relations.ID,
                        Relations.NAME,
                        Relations.FILE_EXT
                    )
                )
            ).getOrNull()

            val name: String

            if (obj != null) {
                val wrapper = ObjectWrapper.File(obj.map)
                name = wrapper.name + "." + wrapper.fileExt
            } else {
                name = ""
            }

            downloadFile.run(
                DownloadFile.Params(
                    url = urlBuilder.original(id),
                    name = name
                )
            ).proceed(
                failure = {
                    Timber.e(it, "Error while downloading media object")
                    _commands.emit(
                        Command.ShowToast.Generic("Error while downloading media object: ${it.message}")
                    )
                },
                success = {
                    _commands.emit(
                        Command.ShowToast.Generic("Download completed successfully.")
                    )
                }
            )
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
                val url: String,
                val isArchived: Boolean = false
            )
            val currentImage: Image? get() = images.getOrNull(currentIndex)
            val isCurrentImageArchived: Boolean get() = currentImage?.isArchived ?: false
        }

        data class VideoContent(
            val url: String,
            val isArchived: Boolean = false
        ) : MediaViewState()

        data class AudioContent(
            val url: String,
            val name: String,
            val isArchived: Boolean = false
        ) : MediaViewState()
    }

    sealed class Command {
        data object Dismiss : Command()
        sealed class ShowToast : Command() {
            data class Generic(val message: String) : Command()
            data class ErrorWhileDownloadingObject(val exception: String) : Command()
            data object MovedToBin : Command()
            data object Restored : Command()
        }
    }

    class Factory @Inject constructor(
        private val urlBuilder: UrlBuilder,
        private val setObjectListIsArchived: SetObjectListIsArchived,
        private val downloadFile: DownloadFile,
        private val fetchObject: FetchObject
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MediaViewModel(
                urlBuilder = urlBuilder,
                setObjectListIsArchived = setObjectListIsArchived,
                downloadFile = downloadFile,
                fetchObject = fetchObject
            ) as T
        }
    }
}