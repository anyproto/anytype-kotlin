package com.agileburo.anytype.data.auth.repo.config

import com.agileburo.anytype.data.auth.model.ConfigEntity

interface Configurator {
    fun configure(): ConfigEntity
    fun release()
}