package com.anytypeio.anytype.feature_discussions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.media.PreloadFile
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import javax.inject.Inject

class DiscussionViewModelFactory @Inject constructor(
    private val params: DiscussionViewModel.Params,
    private val chatContainer: ChatContainer,
    private val members: ActiveSpaceMemberSubscriptionContainer,
    private val getAccount: GetAccount,
    private val urlBuilder: UrlBuilder,
    private val dispatchers: AppCoroutineDispatchers,
    private val addChatMessage: AddComment,
    private val deleteComment: DeleteComment,
    private val toggleCommentReaction: ToggleCommentReaction,
    private val dateProvider: DateProvider,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val fieldParser: FieldParser,
    private val subscription: StorelessSubscriptionContainer,
    private val uploadFile: UploadFile,
    private val preloadFile: PreloadFile,
    private val copyFileToCacheDirectory: CopyFileToCacheDirectory
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = DiscussionViewModel(
        vmParams = params,
        chatContainer = chatContainer,
        members = members,
        getAccount = getAccount,
        urlBuilder = urlBuilder,
        dispatchers = dispatchers,
        addChatMessage = addChatMessage,
        deleteComment = deleteComment,
        toggleCommentReaction = toggleCommentReaction,
        dateProvider = dateProvider,
        storeOfObjectTypes = storeOfObjectTypes,
        fieldParser = fieldParser,
        subscription = subscription,
        uploadFile = uploadFile,
        preloadFile = preloadFile,
        copyFileToCacheDirectory = copyFileToCacheDirectory
    ) as T
}
