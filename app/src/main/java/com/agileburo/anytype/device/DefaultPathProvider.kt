package com.agileburo.anytype.device

import android.content.Context
import com.agileburo.anytype.domain.auth.repo.PathProvider

class DefaultPathProvider(
    private val context: Context
) : PathProvider {
    override fun providePath(): String = context.filesDir.path
}