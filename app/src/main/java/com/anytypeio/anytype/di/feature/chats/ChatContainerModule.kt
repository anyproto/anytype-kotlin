package com.anytypeio.anytype.di.feature.chats

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.main.ConfigModule.DEFAULT_APP_COROUTINE_SCOPE
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.domain.chats.ChatEventChannel
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import dagger.Module
import dagger.Provides
import javax.inject.Named
import kotlinx.coroutines.CoroutineScope

/** Shared by chats and discussions, which both collect a ChatContainer. */
@Module
object ChatContainerModule {
    @JvmStatic
    @Provides
    @PerScreen
    fun provideChatContainer(
        repo: BlockRepository,
        channel: ChatEventChannel,
        logger: Logger,
        subscription: StorelessSubscriptionContainer,
        @Named(DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope,
        dispatchers: AppCoroutineDispatchers
    ): ChatContainer = ChatContainer(
        repo, channel, logger, subscription,
        readScope = CoroutineScope(scope.coroutineContext + dispatchers.io)
    )
}
