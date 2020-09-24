package com.anytypeio.anytype.data.auth.repo.config

import com.anytypeio.anytype.data.auth.model.ConfigEntity

interface Configurator {
    fun configure(): ConfigEntity
    fun release()
}