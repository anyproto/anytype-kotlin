package com.anytypeio.anytype.presentation.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.NO_VALUE
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import com.anytypeio.anytype.domain.objects.CreatePrefilledNote
import com.anytypeio.anytype.domain.spaces.GetSpaceViews
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

class AddToAnytypeViewModel(
    private val createBookmarkObject: CreateBookmarkObject,
    private val createPrefilledNote: CreatePrefilledNote,
    private val spaceManager: SpaceManager,
    private val getSpaceViews: GetSpaceViews,
    private val urlBuilder: UrlBuilder
) : BaseViewModel() {

    private val selectedSpaceId = MutableStateFlow(NO_VALUE)

    private val spaces: Flow<List<ObjectWrapper.SpaceView>> = getSpaceViews.asFlow(Unit)

    val navigation = MutableSharedFlow<OpenObjectNavigation>()
    val spaceViews = MutableStateFlow<List<SpaceView>>(emptyList())
    val commands = MutableSharedFlow<Command>()

    init {
        viewModelScope.launch {
            selectedSpaceId.value = spaceManager.get()
        }
        viewModelScope.launch {
            combine(
                spaces,
                selectedSpaceId
            ) { spaces, selected ->
                spaces.map { space ->
                    SpaceView(
                        obj = space,
                        isSelected = space.targetSpaceId == selected,
                        icon = space.spaceIcon(
                            builder = urlBuilder,
                            spaceGradientProvider = SpaceGradientProvider.Default
                        )
                    )
                }
            }.collect { views ->
                spaceViews.value = views
            }
        }
    }

    fun onCreateBookmark(url: String) {
        viewModelScope.launch {
            val targetSpace = selectedSpaceId.value
            val targetSpaceView = spaceViews.value.firstOrNull { view ->
                view.obj.targetSpaceId == targetSpace
            }
            createBookmarkObject(
                CreateBookmarkObject.Params(
                    space = targetSpace,
                    url = url
                )
            ).process(
                success = { obj ->
                    if (targetSpace == spaceManager.get()) {
                        navigation.emit(OpenObjectNavigation.OpenEditor(obj))
                    } else {
                        with(commands) {
                            if (targetSpaceView != null) {
                                emit(Command.ObjectAddToSpaceToast(targetSpaceView.obj.name))
                            }
                            emit(Command.Dismiss)
                        }
                    }
                },
                failure = {
                    Timber.d(it, "Error while creating bookmark")
                    sendToast("Error while creating bookmark: ${it.msg()}")
                }
            )
        }
    }

    fun onCreateNote(text: String) {
        viewModelScope.launch {
            val targetSpace = selectedSpaceId.value
            val targetSpaceView = spaceViews.value.firstOrNull { view ->
                view.obj.targetSpaceId == targetSpace
            }
            createPrefilledNote.async(
                CreatePrefilledNote.Params(
                    text = text,
                    space = targetSpace
                )
            ).fold(
                onSuccess = { result ->
                    if (targetSpace == spaceManager.get()) {
                        navigation.emit(OpenObjectNavigation.OpenEditor(result))
                    } else {
                        with(commands) {
                            if (targetSpaceView != null) {
                                emit(Command.ObjectAddToSpaceToast(targetSpaceView.obj.name))
                            }
                            emit(Command.Dismiss)
                        }
                    }
                },
                onFailure = {
                    Timber.d(it, "Error while creating note")
                    sendToast("Error while creating note: ${it.msg()}")
                }
            )
        }
    }

    fun onSelectSpaceClicked(view: SpaceView) {
        Timber.d("onSelectSpaceClicked: ${view.obj.targetSpaceId}")
        viewModelScope.launch {
            selectedSpaceId.value = view.obj.targetSpaceId!!
        }
    }

    class Factory @Inject constructor(
        private val createBookmarkObject: CreateBookmarkObject,
        private val createPrefilledNote: CreatePrefilledNote,
        private val spaceManager: SpaceManager,
        private val getSpaceViews: GetSpaceViews,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddToAnytypeViewModel(
                createBookmarkObject = createBookmarkObject,
                spaceManager = spaceManager,
                createPrefilledNote = createPrefilledNote,
                getSpaceViews = getSpaceViews,
                urlBuilder = urlBuilder
            ) as T
        }
    }

    data class SpaceView(
        val obj: ObjectWrapper.SpaceView,
        val isSelected: Boolean,
        val icon: SpaceIconView
    )

    sealed class Command {
        object Dismiss : Command()
        data class ObjectAddToSpaceToast(
            val spaceName: String?
        ) : Command()
    }
}