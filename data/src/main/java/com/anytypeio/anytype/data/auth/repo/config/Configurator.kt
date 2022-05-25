package com.anytypeio.anytype.data.auth.repo.config

import com.anytypeio.anytype.core_models.Config


interface Configurator {
    fun configure(): Config
    fun release()
}