package com.anytypeio.anytype.domain.platform

import com.anytypeio.anytype.core_models.Command

interface InitialParamsProvider {
    fun getVersion(): String
    fun getPlatform(): String
    val workDir: String
    val defaultLogLevel: String
    fun toCommand(): Command.SetInitialParams
}