package com.anytypeio.anytype.domain.network

import com.anytypeio.anytype.core_models.NetworkModeConfig

/**
 * Interface for providing and managing network mode configuration
 */
interface NetworkModeProvider {
    /**
     * Set a new network mode configuration
     * @param networkModeConfig The network mode configuration to set
     */
    fun set(networkModeConfig: NetworkModeConfig)

    /**
     * Get the current network mode configuration
     * @return The current NetworkModeConfig
     */
    fun get(): NetworkModeConfig

    /**
     * Clear the current network mode configuration
     */
    fun clear()
} 