package com.anytypeio.anytype.domain.chats

/**
 * Utility functions for chat state management
 */
object ChatStateUtils {
    
    /**
     * Determines whether a new ChatState should be applied based on order comparison.
     * Only applies new states if their order is higher than the current state order.
     * 
     * @param newOrder The order of the new ChatState
     * @param currentOrder The order of the current ChatState (null defaults to -1)
     * @return true if the new state should be applied, false otherwise
     */
    fun shouldApplyNewChatState(newOrder: Long, currentOrder: Long?): Boolean {
        return newOrder > (currentOrder ?: -1L)
    }
    
    /**
     * Overload for non-nullable currentOrder
     */
    fun shouldApplyNewChatState(newOrder: Long, currentOrder: Long): Boolean {
        return newOrder > currentOrder
    }
}