package com.anytypeio.anytype.domain.deeplink

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Default implementation of PendingDeeplinkProcessor that handles invite deeplinks
 * received while user is not authenticated.
 */
class DefaultPendingDeeplinkProcessor @Inject constructor(
    private val processPendingDeeplink: ProcessPendingDeeplink,
    private val savePendingDeeplink: SavePendingDeeplink,
    private val dispatchers: AppCoroutineDispatchers
) : PendingDeeplinkProcessor {

    override fun processPendingDeeplinks(): Flow<DeepLinkResolver.Action> = flow {
        val pendingAction = processPendingDeeplink.run(Unit).fold(
            fnL = { null },
            fnR = { it }
        )
        
        pendingAction?.let { action ->
            emit(action)
        }
    }.flowOn(dispatchers.io)

    override suspend fun saveIfNotAuthenticated(deeplink: String): Boolean {
        return savePendingDeeplink.run(SavePendingDeeplink.Params(deeplink)).fold(
            fnL = { false },
            fnR = { it }
        )
    }
} 