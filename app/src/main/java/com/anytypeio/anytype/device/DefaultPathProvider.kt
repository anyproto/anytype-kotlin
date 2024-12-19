package com.anytypeio.anytype.device

import android.content.Context
import android.os.Environment
import com.anytypeio.anytype.domain.device.PathProvider

class DefaultPathProvider(
    private val context: Context
) : PathProvider {
    override fun providePath(): String = context.filesDir.path

    override fun provideSharedPath(): String {
        val textDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        return textDir?.path ?: ""
    }
}