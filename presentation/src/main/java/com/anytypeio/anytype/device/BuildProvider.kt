package com.anytypeio.anytype.device

import android.os.Build
import com.anytypeio.anytype.presentation.BuildConfig

interface BuildProvider {
    fun getManufacturer(): String
    fun getModel(): String
    fun isDebug(): Boolean
}

class DefaultBuildProvider : BuildProvider {
    override fun getManufacturer(): String {
        return Build.MANUFACTURER
    }

    override fun getModel(): String {
        return Build.MODEL
    }

    override fun isDebug(): Boolean {
        return BuildConfig.DEBUG
    }
}