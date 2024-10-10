package com.anytypeio.anytype.persistence.preferences

import androidx.datastore.core.Serializer
import com.anytypeio.anytype.persistence.SpacePreferences
import com.anytypeio.anytype.persistence.VaultPreferences
import java.io.InputStream
import java.io.OutputStream

object SpacePrefSerializer : Serializer<SpacePreferences> {
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

object VaultPrefsSerializer : Serializer<VaultPreferences> {
    override val defaultValue: VaultPreferences = VaultPreferences(
        preferences = emptyMap()
    )

    override suspend fun readFrom(input: InputStream): VaultPreferences {
        return VaultPreferences.ADAPTER.decode(input)
    }

    override suspend fun writeTo(t: VaultPreferences, output: OutputStream) {
        VaultPreferences.ADAPTER.encode(
            stream = output,
            value = t
        )
    }
}

const val SPACE_PREFERENCE_FILENAME = "space-preferences.pb"
const val VAULT_PREFERENCE_FILENAME = "vault-preferences.pb"