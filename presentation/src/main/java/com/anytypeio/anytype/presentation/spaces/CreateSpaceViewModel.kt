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
import com.anytypeio.anytype.core_models.SpaceCreationUseCase
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.multiplayer.InviteType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.multiplayer.GenerateSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.MakeSpaceShareable
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.spaces.CreateSpace
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateSpaceViewModel(
    private val vmParams: VmParams,
    private val createSpace: CreateSpace,
    private val spaceManager: SpaceManager,
    private val analytics: Analytics,
    private val uploadFile: UploadFile,
    private val setSpaceDetails: SetSpaceDetails,
    private val spaceViewContainer: SpaceViewSubscriptionContainer,
    private val makeSpaceShareable: MakeSpaceShareable,
    private val generateSpaceInviteLink: GenerateSpaceInviteLink
) : BaseViewModel() {

    val isInProgress = MutableStateFlow(false)

    val commands = MutableSharedFlow<Command>(replay = 0)

    val spaceIconView: MutableStateFlow<SpaceIconView> = MutableStateFlow(
        if (vmParams.spaceUxType == SpaceUxType.CHAT)
            SpaceIconView.ChatSpace.Placeholder(
                color = SystemColor.entries.random()
            )
        else
            SpaceIconView.DataSpace.Placeholder(
                color = SystemColor.entries.random()
            )
    )

    init {
        Timber.d("CreateSpaceViewModel initialized with spaceUxType: %s", vmParams.spaceUxType)
        viewModelScope.launch {
            analytics.sendEvent(eventName = EventsDictionary.screenSettingsSpaceCreate)
        }
    }

    val isDismissed = MutableStateFlow(false)

    fun onImageSelected(url: Url) {
        Timber.d("onImageSelected: $url")
        if (vmParams.spaceUxType == SpaceUxType.CHAT) {
            spaceIconView.value = SpaceIconView.ChatSpace.Image(url = url)
        } else {
            spaceIconView.value = SpaceIconView.DataSpace.Image(url = url)
        }
    }

    fun onCreateSpace(name: String) {
        Timber.d("onCreateSpace, spaceUxType: %s, name: %s", vmParams.spaceUxType, name)
        if (isDismissed.value) {
            return
        }
        if (isInProgress.value) {
            sendToast("Please wait...")
            return
        }
        val (uxType, useCase) = if (vmParams.spaceUxType == SpaceUxType.CHAT) {
            SpaceUxType.CHAT to SpaceCreationUseCase.NONE
        } else {
            SpaceUxType.DATA to SpaceCreationUseCase.EMPTY_MOBILE
        }
        viewModelScope.launch {
            val params = CreateSpace.Params(
                details = mapOf(
                    Relations.NAME to name.trim(),
                    Relations.ICON_OPTION to when (val icon = spaceIconView.value) {
                        is SpaceIconView.ChatSpace.Placeholder -> icon.color.index.toDouble()
                        is SpaceIconView.DataSpace.Placeholder -> icon.color.index.toDouble()
                        else -> SystemColor.SKY.index.toDouble()
                    },
                    Relations.SPACE_UX_TYPE to uxType.code.toDouble()
                ),
                useCase = useCase,
                withChat = vmParams.spaceUxType == SpaceUxType.CHAT
            )
            createSpace.stream(params = params).collect { result ->
                result.fold(
                    onLoading = { isInProgress.value = true },
                    onSuccess = {
                        onSpaceCreated(
                            response = it,
                            isChatSpace = vmParams.spaceUxType == SpaceUxType.CHAT
                        )
                    },
                    onFailure = { onError(it) }
                )
            }
        }
    }

    fun onCreateChatSpace(name: String, iconView: SpaceIconView.ChatSpace) {
        Timber.d("onCreateSpace, spaceUxType: %s, name: %s", vmParams.spaceUxType, name)
        if (isDismissed.value) {
            return
        }
        if (isInProgress.value) {
            sendToast("Please wait...")
            return
        }
        val (uxType, useCase) = SpaceUxType.CHAT to SpaceCreationUseCase.NONE

        viewModelScope.launch {
            val params = CreateSpace.Params(
                details = mapOf(
                    Relations.NAME to name.trim(),
                    Relations.ICON_OPTION to when (iconView) {
                        is SpaceIconView.ChatSpace.Placeholder -> iconView.color.index.toDouble()
                        else -> SystemColor.SKY.index.toDouble()
                    },
                    Relations.SPACE_UX_TYPE to uxType.code.toDouble()
                ),
                useCase = useCase,
                withChat = true
            )
            createSpace.stream(params = params).collect { result ->
                result.fold(
                    onLoading = { isInProgress.value = true },
                    onSuccess = {
                        proceedWithChatSpaceCreated(response = it)
                    },
                    onFailure = { onError(it) }
                )
            }
        }
    }

    private suspend fun onSpaceCreated(response: com.anytypeio.anytype.core_models.Command.CreateSpace.Result, isChatSpace: Boolean) {
        val spaceId = response.space.id
        analytics.sendEvent(
            eventName = EventsDictionary.createSpace,
            props = Props(
                mapOf(EventsPropertiesKey.route to EventsDictionary.Routes.navigation)
            )
        )
        spaceManager.set(spaceId)
        if (isChatSpace) {
            spaceViewContainer.observe(space = SpaceId(spaceId))
                .distinctUntilChanged()
                .catch {
                    Timber.e(it, "Error observing space view updates for space: $spaceId")
                    onError(it)
                }
                .collect { spaceView ->
                    Timber.d("Space view updated: %s", spaceView)
                    if (spaceView.chatId != null) {
                        when (val icon = spaceIconView.value) {
                            is SpaceIconView.ChatSpace.Image -> uploadAndSetIcon(
                                url = icon.url,
                                spaceId = spaceId,
                                startingObject = response.startingObject
                            )

                            is SpaceIconView.DataSpace.Image -> uploadAndSetIconForDataSpace(
                                url = icon.url,
                                spaceId = spaceId,
                                startingObject = response.startingObject
                            )

                            else -> finishCreation(spaceId, response.startingObject)
                        }
                    }
                }
        } else {
            when (val icon = spaceIconView.value) {
                is SpaceIconView.ChatSpace.Image -> uploadAndSetIcon(
                    url = icon.url,
                    spaceId = spaceId,
                    startingObject = response.startingObject
                )

                is SpaceIconView.DataSpace.Image -> uploadAndSetIconForDataSpace(
                    url = icon.url,
                    spaceId = spaceId,
                    startingObject = response.startingObject
                )

                else -> finishCreation(spaceId, response.startingObject)
            }
        }
    }

    private suspend fun proceedWithChatSpaceCreated(response: com.anytypeio.anytype.core_models.Command.CreateSpace.Result, iconView: SpaceIconView.ChatSpace) {
        val spaceId = response.space.id
        analytics.sendEvent(
            eventName = EventsDictionary.createSpace,
            props = Props(
                mapOf(EventsPropertiesKey.route to EventsDictionary.Routes.navigation)
            )
        )
        spaceManager.set(spaceId)
        spaceViewContainer.observe(space = SpaceId(spaceId))
            .distinctUntilChanged()
            .catch {
                Timber.e(it, "Error observing space view updates for space: $spaceId")
                onError(it)
            }
            .collect { spaceView ->
                Timber.d("Space view updated: %s", spaceView)
                if (spaceView.chatId != null) {
                    if (spaceIconView.value is SpaceIconView.ChatSpace.Image) {
                        val icon = spaceIconView.value as SpaceIconView.ChatSpace.Image
                        uploadAndSetIcon(
                            url = icon.url,
                            spaceId = spaceId,
                        ) {

                        }
                    }
                }
            }
    }

    private suspend fun uploadAndSetIcon(
        url: Url,
        spaceId: Id,
        onSuccess: () -> Unit
    ) {
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
                onSuccess()
            },
            onFailure = { onError(it) }
        )
    }

    private suspend fun finishCreation(spaceId: Id, startingObject: Id?) {
        Timber.d("Space created: %s", spaceId)
        isInProgress.value = false
        if (vmParams.spaceUxType == SpaceUxType.CHAT) {
            commands.emit(
                Command.SwitchSpaceChat(
                    space = Space(spaceId),
                    startingObject = startingObject
                )
            )
        } else {
            commands.emit(
                Command.SwitchSpace(
                    space = Space(spaceId),
                    startingObject = startingObject
                )
            )
        }
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
        val color = SystemColor.entries.random()
        spaceIconView.value = if (vmParams.spaceUxType == SpaceUxType.CHAT) {
            SpaceIconView.ChatSpace.Placeholder(color)
        } else {
            SpaceIconView.DataSpace.Placeholder(color)
        }
    }

    //region Share and Create Chat Space Link
    private suspend fun proceedWithMakeSpaceSharable(
        spaceId: SpaceId,
        startingObject: Id?,
        inviteType: InviteType = InviteType.WITHOUT_APPROVE,
        permissions: SpaceMemberPermissions = SpaceMemberPermissions.WRITER
    ) {
        makeSpaceShareable.async(
            params = spaceId
        ).fold(
            onSuccess = {
                Timber.d("Successfully made space shareable")
                generateInviteLink(
                    spaceId = spaceId,
                    inviteType = inviteType,
                    permissions = permissions,
                    startingObject = startingObject
                )
            },
            onFailure = { error ->
                Timber.e(error, "Error while making space shareable")
                onError(error)
            }
        )
    }

    private suspend fun generateInviteLink(
        spaceId: SpaceId,
        startingObject: Id?,
        inviteType: InviteType,
        permissions: SpaceMemberPermissions
    ) {
        generateSpaceInviteLink.async(
            params = GenerateSpaceInviteLink.Params(
                space = spaceId,
                inviteType = inviteType,
                permissions = permissions
            )
        ).fold(
            onSuccess = { inviteLink ->
                Timber.d("Successfully generated invite link: ${inviteLink.scheme}")
                finishCreation(spaceId.id, startingObject)
            },
            onFailure = { error ->
                Timber.e(error, "Error while generating invite link")
                onError(error)
            }
        )
    }

    //endregion

    class Factory @Inject constructor(
        private val vmParams: VmParams,
        private val createSpace: CreateSpace,
        private val spaceManager: SpaceManager,
        private val analytics: Analytics,
        private val uploadFile: UploadFile,
        private val setSpaceDetails: SetSpaceDetails,
        private val spaceViewContainer: SpaceViewSubscriptionContainer,
        private val makeSpaceShareable: MakeSpaceShareable,
        private val generateSpaceInviteLink: GenerateSpaceInviteLink
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = CreateSpaceViewModel(
            createSpace = createSpace,
            spaceManager = spaceManager,
            analytics = analytics,
            uploadFile = uploadFile,
            setSpaceDetails = setSpaceDetails,
            vmParams = vmParams,
            spaceViewContainer = spaceViewContainer,
            makeSpaceShareable = makeSpaceShareable,
            generateSpaceInviteLink = generateSpaceInviteLink
        ) as T
    }

    sealed class Command {
        data class SwitchSpace(
            val space: Space,
            val startingObject: Id?
        ) : Command()
        data class SwitchSpaceChat(
            val space: Space,
            val startingObject: Id?
        ) : Command()
    }

    data class VmParams(
        val spaceUxType: SpaceUxType
    )

    companion object {
        const val MAX_SPACE_COUNT = 50
    }
}