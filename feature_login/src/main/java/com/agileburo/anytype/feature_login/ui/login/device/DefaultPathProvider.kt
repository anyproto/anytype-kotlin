package com.agileburo.anytype.feature_login.ui.login.device

import android.content.Context
import com.agileburo.anytype.feature_login.ui.login.domain.common.PathProvider

class DefaultPathProvider(
    private val context: Context
) : PathProvider {
    override fun providePath(): String = context.filesDir.path
}