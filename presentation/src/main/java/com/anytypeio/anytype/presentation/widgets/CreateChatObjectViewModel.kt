package com.anytypeio.anytype.presentation.widgets

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
import com.anytypeio.anytype.core_models.Name
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateChatObjectViewModel(
    private val vmParams: VmParams,
    private val analytics: Analytics,
    private val createObject: CreateObject,
    private val uploadFile: UploadFile,
    private val setObjectDetails: SetObjectDetails
) : BaseViewModel() {

    val commands = MutableSharedFlow<Command>(replay = 0)
    val isLoading = MutableStateFlow(false)
    val icon: MutableStateFlow<ObjectIcon> = MutableStateFlow(ObjectIcon.None)

    fun onCreateClicked(name: Name) {
        Timber.d("onCreateClicked, name: $name")
        if (isLoading.value) {
            sendToast("Please wait...")
            return
        }
        viewModelScope.launch {
            isLoading.value = true
            val prefilled = mutableMapOf<String, Any>()
            if (name.isNotEmpty()) {
                prefilled[Relations.NAME] = name
            }
            
            val params = CreateObject.Param(
                space = vmParams.space,
                type = TypeKey(ObjectTypeIds.CHAT_DERIVED),
                prefilled = prefilled
            )
            
            createObject.async(params).fold(
                onSuccess = { result ->
                    Timber.d("Chat object created successfully: ${result.objectId}")
                    maybeUploadIconAndFinish(
                        icon = icon.value,
                        objectId = result.objectId
                    )
                },
                onFailure = { error ->
                    Timber.e(error, "Error while creating chat object")
                    isLoading.value = false
                    sendToast("Error while creating chat object: ${error.message}")
                }
            )
        }
    }

    private suspend fun maybeUploadIconAndFinish(
        icon: ObjectIcon,
        objectId: Id
    ) {
        when (icon) {
            is ObjectIcon.Basic.Image -> {
                uploadAndSetIcon(url = icon.hash, objectId = objectId)
            }
            is ObjectIcon.Profile.Image -> {
                uploadAndSetIcon(url = icon.hash, objectId = objectId)
            }
            is ObjectIcon.Basic.Emoji -> {
                setIconEmoji(emoji = icon.unicode, objectId = objectId)
            }
            else -> {
                finishCreation(objectId)
            }
        }
    }

    private suspend fun uploadAndSetIcon(
        url: Url,
        objectId: Id
    ) {
        uploadFile.async(
            UploadFile.Params(
                path = url,
                space = Space(vmParams.space.id),
                type = Block.Content.File.Type.IMAGE
            )
        ).fold(
            onSuccess = { file ->
                setObjectDetails.async(
                    SetObjectDetails.Params(
                        ctx = objectId,
                        details = mapOf(Relations.ICON_IMAGE to file.id)
                    )
                )
                finishCreation(objectId)
            },
            onFailure = { error ->
                Timber.e(error, "Error while uploading icon")
                finishCreation(objectId)
            }
        )
    }

    private suspend fun setIconEmoji(
        emoji: String,
        objectId: Id
    ) {
        setObjectDetails.async(
            SetObjectDetails.Params(
                ctx = objectId,
                details = mapOf(Relations.ICON_EMOJI to emoji)
            )
        ).fold(
            onSuccess = {
                finishCreation(objectId)
            },
            onFailure = { error ->
                Timber.e(error, "Error while setting emoji icon")
                finishCreation(objectId)
            }
        )
    }

    private suspend fun finishCreation(objectId: Id) {
        // Fire CreateObject analytics event
        analytics.sendEvent(
            eventName = EventsDictionary.objectCreate,
            props = Props(
                mapOf(EventsPropertiesKey.objectType to "_otchatDerived")
            )
        )
        isLoading.value = false
        commands.emit(Command.ChatObjectCreated(objectId = objectId))
    }

    fun onIconUploadClicked() {
        viewModelScope.launch {
            commands.emit(Command.UploadImage)
        }
    }

    fun onIconRemoveClicked() {
        icon.value = ObjectIcon.None
    }

    fun onEmojiIconClicked() {
        viewModelScope.launch {
            commands.emit(Command.SelectEmoji)
        }
    }

    fun onImageSelected(url: Url) {
        Timber.d("onImageSelected: $url")
        icon.value = ObjectIcon.Profile.Image(hash = url, name = "")
    }

    fun onEmojiSelected(emoji: String) {
        Timber.d("onEmojiSelected: $emoji")
        icon.value = ObjectIcon.Basic.Emoji(emoji)
    }

    class Factory @Inject constructor(
        private val vmParams: VmParams,
        private val analytics: Analytics,
        private val createObject: CreateObject,
        private val uploadFile: UploadFile,
        private val setObjectDetails: SetObjectDetails
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = CreateChatObjectViewModel(
            vmParams = vmParams,
            analytics = analytics,
            createObject = createObject,
            uploadFile = uploadFile,
            setObjectDetails = setObjectDetails
        ) as T
    }

    data class VmParams(
        val space: SpaceId
    )

    sealed class Command {
        data class ChatObjectCreated(val objectId: Id) : Command()
        object UploadImage : Command()
        object SelectEmoji : Command()
    }
}
