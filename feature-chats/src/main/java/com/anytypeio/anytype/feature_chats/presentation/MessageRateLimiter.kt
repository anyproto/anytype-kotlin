package com.anytypeio.anytype.feature_chats.presentation

class MessageRateLimiter(
    private val maxMessages: Int = DEFAULT_MAX_MESSAGES,
    private val timeWindowSeconds: Int = DEFAULT_TIME_WINDOW_SECONDS
) {
    private val messageTimestamps = mutableListOf<Long>()

    fun shouldShowRateLimitWarning(): Boolean {
        val currentTime = System.currentTimeMillis()
        val windowStart = currentTime - (timeWindowSeconds * 1000)

        // Remove timestamps outside the current window
        messageTimestamps.removeAll { it < windowStart }

        // Add current message timestamp
        messageTimestamps.add(currentTime)

        // Return true if we've exceeded the rate limit
        return messageTimestamps.size > maxMessages
    }

    companion object {
        private const val DEFAULT_MAX_MESSAGES = 5
        private const val DEFAULT_TIME_WINDOW_SECONDS = 5
    }
}