package com.anytypeio.anytype.feature_discussions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.chats.AddChatMessage
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.domain.chats.DeleteChatMessage
import com.anytypeio.anytype.domain.chats.EditChatMessage
import com.anytypeio.anytype.domain.chats.ToggleChatMessageReaction
import com.anytypeio.anytype.domain.media.FileDrop
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import javax.inject.Inject

class DiscussionViewModelFactory @Inject constructor(
    private val params: DiscussionViewModel.Params,
    private val setObjectDetails: SetObjectDetails,
    private val openObject: OpenObject,
    private val chatContainer: ChatContainer,
    private val addChatMessage: AddChatMessage,
    private val editChatMessage: EditChatMessage,
    private val deleteChatMessage: DeleteChatMessage,
    private val toggleChatMessageReaction: ToggleChatMessageReaction,
    private val members: ActiveSpaceMemberSubscriptionContainer,
    private val getAccount: GetAccount,
    private val urlBuilder: UrlBuilder,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val dispatchers: AppCoroutineDispatchers,
    private val uploadFile: UploadFile,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val copyFileToCacheDirectory: CopyFileToCacheDirectory,
    private val emojiProvider: EmojiProvider
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = DiscussionViewModel(
        vmParams = params,
        setObjectDetails = setObjectDetails,
        openObject = openObject,
        chatContainer = chatContainer,
        addChatMessage = addChatMessage,
        toggleChatMessageReaction = toggleChatMessageReaction,
        members = members,
        getAccount = getAccount,
        deleteChatMessage = deleteChatMessage,
        urlBuilder = urlBuilder,
        editChatMessage = editChatMessage,
        spaceViews = spaceViews,
        dispatchers = dispatchers,
        uploadFile = uploadFile,
        storeOfObjectTypes = storeOfObjectTypes,
        copyFileToCacheDirectory = copyFileToCacheDirectory,
        emojiProvider = emojiProvider
    ) as T
}