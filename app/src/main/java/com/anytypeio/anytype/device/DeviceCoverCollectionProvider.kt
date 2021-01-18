package com.anytypeio.anytype.device

import android.content.Context
import com.anytypeio.anytype.core_utils.ext.getJsonDataFromAsset
import com.anytypeio.anytype.domain.cover.CoverCollectionProvider
import com.anytypeio.anytype.domain.cover.CoverImage
import com.google.gson.Gson

class DeviceCoverCollectionProvider(
    private val context: Context,
    private val gson: Gson
) : CoverCollectionProvider {

    override fun provide(): List<CoverImage> {
        val json = context.getJsonDataFromAsset(COVER_FILE)
        return if (json != null) {
            gson.fromJson(json, Array<CoverImage>::class.java).toList()
        } else {
            emptyList()
        }
    }

    companion object {
        const val COVER_FILE = "covers.json"
    }
}