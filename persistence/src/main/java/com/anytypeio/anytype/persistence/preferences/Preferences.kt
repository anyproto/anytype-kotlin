package com.anytypeio.anytype.persistence.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.anytypeio.anytype.persistence.SpacePreferences
import java.io.InputStream
import java.io.OutputStream

object PrefSerializer : Serializer<SpacePreferences> {
    override val defaultValue: SpacePreferences = SpacePreferences()

    override suspend fun readFrom(input: InputStream): SpacePreferences {
        return SpacePreferences.ADAPTER.decode(input)
    }

    override suspend fun writeTo(t: SpacePreferences, output: OutputStream) {
        SpacePreferences.ADAPTER.encode(
            stream = output,
            value = t
        )
    }
}

const val SPACE_PREFERENCE_FILENAME = "space-preferences.pb"