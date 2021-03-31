package com.anytypeio.anytype.core_models

/**
 * Anytype app configuration properties.
 * @property home id of the home dashboard
 * @property profile id of the current profile
 * @property gateway url of the gateway for fetching files.
 */
data class Config(
    val home: Id,
    val profile: Id,
    val gateway: Url
)