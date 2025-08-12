package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SpaceCreationUseCase
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.spaces.CreateSpace
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateSpaceViewModel(
    private val createSpace: CreateSpace,
    private val spaceManager: SpaceManager,
    private val analytics: Analytics,
    private val uploadFile: UploadFile,
    private val setSpaceDetails: SetSpaceDetails
) : BaseViewModel() {

    val isInProgress = MutableStateFlow(false)

    val commands = MutableSharedFlow<Command>(replay = 0)

    val spaceIconView : MutableStateFlow<SpaceIconView> = MutableStateFlow(
        SpaceIconView.Placeholder(
            color = SystemColor.entries.random()
        )
    )

    init {
        viewModelScope.launch {
            analytics.sendEvent(eventName = EventsDictionary.screenSettingsSpaceCreate)
        }
    }

    val isDismissed = MutableStateFlow(false)

    fun onImageSelected(url: Url) {
        Timber.d("onImageSelected: $url")
        spaceIconView.value = SpaceIconView.Image(url = url)
    }

    fun onCreateSpace(name: String, withChat: Boolean) {
        Timber.d("onCreateSpace, withChat: $withChat")
        if (isDismissed.value) {
            return
        }
        if (isInProgress.value) {
            sendToast("Please wait...")
            return
        }
        val (uxType, useCase) = if (withChat) {
            SpaceUxType.CHAT to SpaceCreationUseCase.NONE
        } else {
            SpaceUxType.DATA to SpaceCreationUseCase.EMPTY_MOBILE
        }
        viewModelScope.launch {
            val params = CreateSpace.Params(
                details = mapOf(
                    Relations.NAME to name.trim(),
                    Relations.ICON_OPTION to when (val icon = spaceIconView.value) {
                        is SpaceIconView.Placeholder -> icon.color.index.toDouble()
                        else -> SystemColor.SKY.index.toDouble()
                    },
                    Relations.SPACE_UX_TYPE to uxType.code.toDouble()
                ),
                useCase = useCase,
                withChat = withChat
            )
            createSpace.stream(params = params).collect { result ->
                result.fold(
                    onLoading = { isInProgress.value = true },
                    onSuccess = { onSpaceCreated(it) },
                    onFailure = { onError(it) }
                )
            }
        }
    }

    private suspend fun onSpaceCreated(response: com.anytypeio.anytype.core_models.Command.CreateSpace.Result) {
        val spaceId = response.space.id
        analytics.sendEvent(
            eventName = EventsDictionary.createSpace,
            props = Props(
                mapOf(EventsPropertiesKey.route to EventsDictionary.Routes.navigation)
            )
        )
        spaceManager.set(spaceId)

        when (val icon = spaceIconView.value) {
            is SpaceIconView.Image -> uploadAndSetIcon(
                url = icon.url,
                spaceId = spaceId,
                startingObject = response.startingObject
            )
            else -> finishCreation(spaceId, response.startingObject)
        }
    }

    private suspend fun uploadAndSetIcon(url: Url, spaceId: Id, startingObject: Id?) {
        uploadFile.async(
            UploadFile.Params(
                path = url,
                space = Space(spaceId),
                type = Block.Content.File.Type.IMAGE
            )
        ).fold(
            onSuccess = { file ->
                setSpaceDetails.async(
                    SetSpaceDetails.Params(
                        space = Space(spaceId),
                        details = mapOf(Relations.ICON_IMAGE to file.id)
                    )
                )
                finishCreation(spaceId, startingObject)
            },
            onFailure = { onError(it) }
        )
    }

    private suspend fun finishCreation(spaceId: Id, startingObject: Id?) {
        Timber.d("Space created: %s", spaceId)
        isInProgress.value = false
        commands.emit(
            Command.SwitchSpace(
                space = Space(spaceId),
                startingObject = startingObject
            )
        )
    }

    private fun onError(error: Throwable) {
        Timber.e(error, "Error creating space")
        sendToast("Error while creating space, please try again.")
        isInProgress.value = false
    }

    fun onSpaceIconRemovedClicked() {
        proceedWithResettingRandomSpaceGradient()
    }

    private fun proceedWithResettingRandomSpaceGradient() {
        spaceIconView.value = SpaceIconView.Placeholder(
            color = SystemColor.entries.random()
        )
    }

    class Factory @Inject constructor(
        private val createSpace: CreateSpace,
        private val spaceManager: SpaceManager,
        private val analytics: Analytics,
        private val uploadFile: UploadFile,
        private val setSpaceDetails: SetSpaceDetails
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = CreateSpaceViewModel(
            createSpace = createSpace,
            spaceManager = spaceManager,
            analytics = analytics,
            uploadFile = uploadFile,
            setSpaceDetails = setSpaceDetails
        ) as T
    }

    sealed class Command {
        data class SwitchSpace(
            val space: Space,
            val startingObject: Id?
        ): Command()
    }
}