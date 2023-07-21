package com.anytypeio.anytype.other

import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.domain.debugging.DebugConfig
import javax.inject.Inject

class DefaultDebugConfig @Inject constructor(): DebugConfig {
    override val traceSubscriptions: Boolean get() = BuildConfig.DEBUG
}