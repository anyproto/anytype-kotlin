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

    /**
     * Enhanced state reconciliation that handles edge cases for multi-device sync.
     * Provides more robust state management with timestamp-based fallback.
     *
     * @param newOrder The order of the new ChatState
     * @param currentOrder The order of the current ChatState
     * @param newTimestamp Optional timestamp of the new state for fallback comparison
     * @param currentTimestamp Optional timestamp of the current state for fallback comparison
     * @param allowEqualOrder Whether to accept states with equal order (default: false)
     * @return true if the new state should be applied, false otherwise
     */
    fun shouldApplyNewChatStateEnhanced(
        newOrder: Long,
        currentOrder: Long?,
        newTimestamp: Long? = null,
        currentTimestamp: Long? = null,
        allowEqualOrder: Boolean = false
    ): Boolean {
        val safeCurrentOrder = currentOrder ?: -1L

        return when {
            // Standard case: new order is higher
            newOrder > safeCurrentOrder -> true

            // Standard case: new order is lower
            newOrder < safeCurrentOrder -> false

            // Equal order case: use additional criteria
            newOrder == safeCurrentOrder -> {
                when {
                    // Allow equal order if explicitly enabled (useful for state corrections)
                    allowEqualOrder -> true

                    // Use timestamp as tiebreaker if available
                    newTimestamp != null && currentTimestamp != null -> {
                        newTimestamp > currentTimestamp
                    }

                    // If only new timestamp is available, prefer it (newer information)
                    newTimestamp != null && currentTimestamp == null -> true

                    // Default: reject equal order to prevent unnecessary updates
                    else -> false
                }
            }

            else -> false
        }
    }

    /**
     * Validates if an order value is reasonable to prevent malicious or corrupted updates.
     *
     * @param order The order value to validate
     * @param currentOrder The current order for comparison
     * @return true if the order is valid, false otherwise
     */
    fun isOrderValid(order: Long, currentOrder: Long?): Boolean {
        val safeCurrentOrder = currentOrder ?: -1L

        // Prevent absurdly large jumps that might indicate corruption or attack
        val maxReasonableJump = 1_000_000L

        return when {
            // Negative orders are invalid (except initial -1)
            order < -1L -> false

            // Prevent extremely large forward jumps
            order > safeCurrentOrder + maxReasonableJump -> false

            // Otherwise valid
            else -> true
        }
    }
}