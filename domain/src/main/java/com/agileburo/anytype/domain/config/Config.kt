package com.agileburo.anytype.domain.config

import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.common.Url

/**
 * Anytype app configuration properties.
 * @property home id of the home dashboard
 * @property gateway url of the gateway for fetching files.
 */
data class Config(
    val home: Id,
    val gateway: Url
)