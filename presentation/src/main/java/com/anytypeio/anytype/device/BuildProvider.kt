package com.anytypeio.anytype.device

import android.os.Build

interface BuildProvider {
    fun getManufacturer(): String
    fun getModel(): String
}

class DefaultBuildProvider() : BuildProvider {
    override fun getManufacturer(): String {
        return Build.MANUFACTURER
    }

    override fun getModel(): String {
        return Build.MODEL
    }
}