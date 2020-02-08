package com.agileburo.anytype.domain.config

import com.agileburo.anytype.domain.common.Id

/**
 * Anytype app configuration properties.
 * @property home id of the home dashboard
 */
data class Config(val home: Id)