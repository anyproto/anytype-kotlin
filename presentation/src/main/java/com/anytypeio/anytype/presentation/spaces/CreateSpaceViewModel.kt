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
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.spaces.CreateSpace
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.domain.media.UploadFile
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
        viewModelScope.launch {
            createSpace.stream(
                CreateSpace.Params(
                    details = mapOf(
                        Relations.NAME to name,
                        Relations.ICON_OPTION to when (val icon = spaceIconView.value) {
                            is SpaceIconView.Placeholder -> icon.color.index.toDouble()
                            else -> SystemColor.SKY.index.toDouble()
                        }
                    ),
                    shouldApplyEmptyUseCase = true,
                    withChat = withChat
                )
            ).collect { result ->
                result.fold(
                    onLoading = { isInProgress.value = true },
                    onSuccess = { response ->
                        val space = response.space.id
                        analytics.sendEvent(
                            eventName = EventsDictionary.createSpace,
                            props = Props(
                                mapOf(EventsPropertiesKey.route to EventsDictionary.Routes.navigation)
                            )
                        )
                        setNewSpaceAsCurrentSpace(space)
                        
                        // Handle image upload if an image was selected
                        when (val icon = spaceIconView.value) {
                            is SpaceIconView.Image -> {
                                uploadFile.async(
                                    UploadFile.Params(
                                        path = icon.url,
                                        space = Space(space),
                                        type = Block.Content.File.Type.IMAGE,
                                        createTypeWidgetIfMissing = false
                                    )
                                ).fold(
                                    onSuccess = { file ->
                                        // Set the uploaded file as space icon
                                        setSpaceDetails.async(
                                            SetSpaceDetails.Params(
                                                space = Space(space),
                                                details = mapOf(
                                                    Relations.ICON_IMAGE to file.id
                                                )
                                            )
                                        ).fold(
                                            onSuccess = {
                                                Timber.d("Successfully set space icon: $file")
                                            },
                                            onFailure = { error ->
                                                Timber.e(error, "Error setting space icon")
                                            }
                                        )
                                        Timber.d("Successfully created space: $space").also {
                                            isInProgress.value = false
                                            commands.emit(
                                                Command.SwitchSpace(
                                                    space = Space(space),
                                                    startingObject = response.startingObject
                                                )
                                            )
                                        }
                                    },
                                    onFailure = { error ->
                                        Timber.e(error, "Error uploading space icon")
                                        sendToast("Error while creating space, please try again.")
                                        isInProgress.value = false
                                    }
                                )
                            }
                            else -> {
                                Timber.d("Successfully created space: $space").also {
                                    isInProgress.value = false
                                    commands.emit(
                                        Command.SwitchSpace(
                                            space = Space(space),
                                            startingObject = response.startingObject
                                        )
                                    )
                                }
                            }
                        }
                    },
                    onFailure = {
                        Timber.e(it, "Error while creating space").also {
                            sendToast("Error while creating space, please try again.")
                            isInProgress.value = false
                        }
                    }
                )
            }
        }
    }

    private suspend fun setNewSpaceAsCurrentSpace(space: Id) {
        spaceManager.set(space)
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