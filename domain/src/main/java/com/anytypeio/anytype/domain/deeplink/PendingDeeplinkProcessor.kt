package com.anytypeio.anytype.domain.deeplink

import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import kotlinx.coroutines.flow.Flow

/**
 * Interface for processing pending deeplinks when user authentication is established.
 * This is used to handle invite deeplinks that were received while user was not logged in.
 */
interface PendingDeeplinkProcessor {
    
    /**
     * Processes any pending invite deeplinks and returns them as a flow.
     * This should be called when authentication is established (e.g., in GlobalSubscriptionManager.onStart()).
     * 
     * @return Flow of deeplink actions that were pending and now ready to be processed
     */
    fun processPendingDeeplinks(): Flow<DeepLinkResolver.Action>
    
    /**
     * Checks and saves an invite deeplink if user is not authenticated.
     * 
     * @param deeplink The deeplink to potentially save
     * @return true if the deeplink was saved (because user was not authenticated), false otherwise
     */
    suspend fun saveIfNotAuthenticated(deeplink: String): Boolean
} 