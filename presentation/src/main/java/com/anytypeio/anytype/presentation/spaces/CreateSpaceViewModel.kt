package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.shareSpace
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SpaceCreationUseCase
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.multiplayer.ChannelCreationType
import com.anytypeio.anytype.core_models.multiplayer.InviteType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.ui.SpaceMemberIconView
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.SpaceIconView
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.multiplayer.AddSpaceMembers
import com.anytypeio.anytype.domain.multiplayer.GenerateSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.MakeSpaceShareable
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.multiplayer.sharedSpaceCount
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.domain.spaces.CreateSpace
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsShareSpaceNewLink
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateSpaceViewModel(
    private val vmParams: VmParams,
    private val createSpace: CreateSpace,
    private val spaceManager: SpaceManager,
    private val analytics: Analytics,
    private val uploadFile: UploadFile,
    private val setSpaceDetails: SetSpaceDetails,
    private val saveCurrentSpace: SaveCurrentSpace,
    private val makeSpaceShareable: MakeSpaceShareable,
    private val generateSpaceInviteLink: GenerateSpaceInviteLink,
    private val addSpaceMembers: AddSpaceMembers,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val permissions: UserPermissionProvider,
    private val profileContainer: ProfileSubscriptionManager,
    private val urlBuilder: UrlBuilder
) : BaseViewModel() {

    val isInProgress = MutableStateFlow(false)

    val commands = MutableSharedFlow<Command>(replay = 0)

    val spaceIconView: MutableStateFlow<SpaceIconView> = MutableStateFlow(
        SpaceIconView.DataSpace.Placeholder(
            color = SystemColor.entries.random()
        )
    )

    init {
        Timber.d("CreateSpaceViewModel initialized with channelType: %s", vmParams.channelType)
        viewModelScope.launch {
            analytics.sendEvent(eventName = EventsDictionary.screenSettingsSpaceCreate)
        }
    }

    val isDismissed = MutableStateFlow(false)

    val selectedMembersView: StateFlow<List<SpaceMemberView>> = spaceViews.observe()
        .map { spaces ->
            val identities = vmParams.selectedMemberIdentities
            if (identities.isEmpty()) return@map emptyList()
            spaces
                .filter { space ->
                    space.spaceUxType == SpaceUxType.ONE_TO_ONE
                        && space.oneToOneIdentity in identities
                }
                .mapNotNull { space ->
                    val identity = space.oneToOneIdentity ?: return@mapNotNull null
                    val name = space.name.orEmpty()
                    val iconImage = space.iconImage
                    val icon = if (!iconImage.isNullOrEmpty()) {
                        SpaceMemberIconView.Image(
                            url = urlBuilder.thumbnail(iconImage),
                            name = name
                        )
                    } else {
                        SpaceMemberIconView.Placeholder(name = name)
                    }
                    SpaceMemberView(
                        identity = identity,
                        name = name,
                        icon = icon
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Group channels should be made shareable and have invite links created.
     * Personal channels are private and don't need invite links.
     */
    private val shouldCreateInviteLink: Boolean
        get() = vmParams.channelType == ChannelCreationType.GROUP

    private val _createSpaceError = MutableStateFlow<CreateSpaceError?>(null)
    val createSpaceError: StateFlow<CreateSpaceError?> = _createSpaceError.asStateFlow()

    fun onImageSelected(url: Url) {
        Timber.d("onImageSelected: $url")
        spaceIconView.value = SpaceIconView.DataSpace.Image(url = url)
    }

    fun onCreateSpace(name: String) {
        Timber.d("onCreateSpace, channelType: %s", vmParams.channelType)
        if (isDismissed.value) {
            return
        }
        if (isInProgress.value) {
            sendToast("Please wait...")
            return
        }
        viewModelScope.launch {
            val params = CreateSpace.Params(
                details = mapOf(
                    Relations.NAME to name.trim(),
                    Relations.ICON_OPTION to when (val icon = spaceIconView.value) {
                        is SpaceIconView.DataSpace.Placeholder -> icon.color.index.toDouble()
                        else -> SystemColor.SKY.index.toDouble()
                    }
                ),
                useCase = SpaceCreationUseCase.NONE
            )
            createSpace.stream(params = params).collect { result ->
                result.fold(
                    onLoading = { isInProgress.value = true },
                    onSuccess = {
                        onSpaceCreated(
                            response = it,
                            shouldCreateInviteLink = shouldCreateInviteLink
                        )
                    },
                    onFailure = { onError(it) }
                )
            }
        }
    }

    private suspend fun onSpaceCreated(
        response: com.anytypeio.anytype.core_models.Command.CreateSpace.Result,
        shouldCreateInviteLink: Boolean
    ) {
        val icon = spaceIconView.value
        val spaceId = response.space.id
        analytics.sendEvent(
            eventName = EventsDictionary.createSpace,
            props = Props(
                mapOf(
                    EventsPropertiesKey.route to EventsDictionary.Routes.navigation,
                    EventsPropertiesKey.uxType to when (vmParams.channelType) {
                        ChannelCreationType.PERSONAL -> "Personal"
                        ChannelCreationType.GROUP -> "Group"
                    }
                )
            )
        )

        val proceed: suspend () -> Unit = if (shouldCreateInviteLink) {
            {
                proceedWithMakeGroupChannelShareable(
                    spaceId = SpaceId(spaceId)
                )
            }
        } else {
            {
                finishSpaceCreation(spaceId = spaceId)
            }
        }

        maybeUploadIconAndProceed(icon = icon, spaceId = spaceId, onSuccess = proceed)
    }

    private suspend fun uploadAndSetIcon(
        url: Url,
        spaceId: Id,
        onSuccess: suspend () -> Unit
    ) {
        uploadFile.async(
            UploadFile.Params(
                path = url,
                space = Space(spaceId),
                type = Block.Content.File.Type.IMAGE,
                createdInContext = spaceId,
                createdInContextRef = Relations.ICON_IMAGE
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

    private suspend fun maybeUploadIconAndProceed(
        icon: SpaceIconView,
        spaceId: Id,
        onSuccess: suspend () -> Unit
    ) {
        when (icon) {
            is SpaceIconView.DataSpace.Image -> {
                uploadAndSetIcon(
                    url = icon.url,
                    spaceId = spaceId,
                    onSuccess = onSuccess
                )
            }
            else -> onSuccess()
        }
    }

    private suspend fun finishSpaceCreation(spaceId: Id) {
        Timber.d("Space created: %s", spaceId)
        isInProgress.value = false
        spaceManager.set(space = spaceId).fold(
            onSuccess = { _ ->
                saveCurrentSpace.async(params = SaveCurrentSpace.Params(SpaceId(spaceId)))
                commands.emit(
                    Command.SwitchSpaceWithHomepagePicker(
                        space = Space(spaceId)
                    )
                )
            },
            onFailure = { error ->
                Timber.e(error, "Error setting created space")
                onError(error)
            }
        )
    }

    private fun onError(error: Throwable) {
        Timber.e(error, "Error creating space")
        isInProgress.value = false
        _createSpaceError.value = CreateSpaceError(msg = error.message ?: "Unknown error")
    }

    fun clearCreateSpaceError() {
        _createSpaceError.value = null
    }

    fun onSpaceIconRemovedClicked() {
        proceedWithResettingRandomSpaceGradient()
    }

    private fun proceedWithResettingRandomSpaceGradient() {
        val color = SystemColor.entries.random()
        spaceIconView.value = SpaceIconView.DataSpace.Placeholder(color)
    }

    //region Share and Create Group Channel Link
    // Note: Only GROUP channels create invite links.
    // PERSONAL channels are private and don't need invite links.
    private suspend fun proceedWithMakeGroupChannelShareable(
        spaceId: SpaceId
    ) {
        // Check if shareable space limit is reached
        if (isSharableLimitReached()) {
            Timber.d("Shareable space limit reached, skipping share and finishing creation")
            finishSpaceCreation(spaceId = spaceId.id)
            return
        }

        makeSpaceShareable.async(params = spaceId).fold(
            onSuccess = {
                Timber.d("Successfully made space shareable")
                analytics.sendEvent(eventName = shareSpace)
                generateInviteLink(spaceId = spaceId)
            },
            onFailure = { error ->
                Timber.e(error, "Error while making space shareable")
                if (vmParams.selectedMemberIdentities.isNotEmpty()) {
                    onError(error)
                } else {
                    sendToast("Failed to make space shareable")
                    finishSpaceCreation(spaceId = spaceId.id)
                }
            }
        )
    }

    /**
     * Check if the shareable space limit has been reached.
     * When the limit is reached, we cannot share any more spaces.
     */
    private suspend fun isSharableLimitReached(): Boolean {
        val sharedSpaceCountFlow = spaceViews.sharedSpaceCount(permissions.all())
        val sharedSpaceLimitFlow = profileContainer
            .observe()
            .map { wrapper ->
                wrapper.getValue<Double?>(Relations.SHARED_SPACES_LIMIT)?.toInt() ?: 0
            }

        val (count, limit) = combine(
            sharedSpaceCountFlow,
            sharedSpaceLimitFlow
        ) { count, limit ->
            count to limit
        }.first()

        return limit > 0 && count >= limit
    }

    private suspend fun generateInviteLink(
        spaceId: SpaceId
    ) {
        generateSpaceInviteLink.async(
            params = GenerateSpaceInviteLink.Params(
                space = spaceId,
                inviteType = CHAT_SPACE_INVITE_TYPE,
                permissions = CHAT_SPACE_DEFAULT_PERMISSIONS
            )
        ).fold(
            onSuccess = { inviteLink ->
                Timber.d("Successfully generated invite link: ${inviteLink.scheme}")

                // Analytics: Track chat space invite link generation
                analytics.sendAnalyticsShareSpaceNewLink(
                    inviteType = CHAT_SPACE_INVITE_TYPE,
                    permissions = CHAT_SPACE_DEFAULT_PERMISSIONS
                )

                proceedWithAddMembers(spaceId = spaceId)
            },
            onFailure = { error ->
                Timber.e(error, "Error while generating invite link")
                if (vmParams.selectedMemberIdentities.isNotEmpty()) {
                    onError(error)
                } else {
                    sendToast("Failed to generate invite link")
                    finishSpaceCreation(spaceId = spaceId.id)
                }
            }
        )
    }

    private suspend fun proceedWithAddMembers(
        spaceId: SpaceId
    ) {
        val identities = vmParams.selectedMemberIdentities
        if (identities.isNotEmpty()) {
            addSpaceMembers.async(
                AddSpaceMembers.Params(
                    space = spaceId,
                    identities = identities
                )
            ).fold(
                onSuccess = {
                    Timber.d("Successfully added ${identities.size} members to space")
                },
                onFailure = { e ->
                    Timber.e(e, "Failed to add members to space")
                    sendToast("Failed to add members")
                }
            )
        }
        finishSpaceCreation(spaceId = spaceId.id)
    }

    //endregion

    class Factory @Inject constructor(
        private val vmParams: VmParams,
        private val createSpace: CreateSpace,
        private val spaceManager: SpaceManager,
        private val analytics: Analytics,
        private val uploadFile: UploadFile,
        private val setSpaceDetails: SetSpaceDetails,
        private val saveCurrentSpace: SaveCurrentSpace,
        private val makeSpaceShareable: MakeSpaceShareable,
        private val generateSpaceInviteLink: GenerateSpaceInviteLink,
        private val addSpaceMembers: AddSpaceMembers,
        private val spaceViews: SpaceViewSubscriptionContainer,
        private val permissions: UserPermissionProvider,
        private val profileContainer: ProfileSubscriptionManager,
        private val urlBuilder: UrlBuilder
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
            saveCurrentSpace = saveCurrentSpace,
            makeSpaceShareable = makeSpaceShareable,
            generateSpaceInviteLink = generateSpaceInviteLink,
            addSpaceMembers = addSpaceMembers,
            spaceViews = spaceViews,
            permissions = permissions,
            profileContainer = profileContainer,
            urlBuilder = urlBuilder
        ) as T
    }

    sealed class Command {
        data class SwitchSpaceWithHomepagePicker(
            val space: Space
        ) : Command()
    }

    data class VmParams(
        val channelType: ChannelCreationType,
        val selectedMemberIdentities: List<String> = emptyList()
    )

    data class CreateSpaceError(val msg: String)

    data class SpaceMemberView(
        val identity: Id,
        val name: String,
        val icon: SpaceMemberIconView
    )

    companion object {
        private val CHAT_SPACE_INVITE_TYPE = InviteType.WITHOUT_APPROVE
        private val CHAT_SPACE_DEFAULT_PERMISSIONS = SpaceMemberPermissions.WRITER
    }
}