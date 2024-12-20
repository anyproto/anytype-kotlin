package com.anytypeio.anytype.device

import android.content.Context
import com.anytypeio.anytype.domain.device.PathProvider

class DefaultPathProvider(
    private val context: Context
) : PathProvider {
    override fun providePath(): String = context.filesDir.path
}