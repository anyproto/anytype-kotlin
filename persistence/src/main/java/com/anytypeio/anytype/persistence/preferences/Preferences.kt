package com.anytypeio.anytype.persistence.preferences

import androidx.datastore.core.Serializer
import com.anytypeio.anytype.persistence.SpacePreferences
import com.anytypeio.anytype.persistence.VaultPreferences
import java.io.InputStream
import java.io.OutputStream
import timber.log.Timber

object SpacePrefSerializer : Serializer<SpacePreferences> {
    override val defaultValue: SpacePreferences = SpacePreferences()

    override suspend fun readFrom(input: InputStream): SpacePreferences {
        return try {
            SpacePreferences.ADAPTER.decode(input)
        } catch (e: Exception) {
            // Handle corrupted protobuf files gracefully
            // This can happen when:
            // - File is corrupted
            // - Schema has changed
            // - File contains invalid data (e.g., "Unexpected tag 0")
            Timber.e(e, "Failed to decode SpacePreferences, returning default value")
            defaultValue
        }
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
        return try {
            VaultPreferences.ADAPTER.decode(input)
        } catch (e: Exception) {
            // Handle corrupted protobuf files gracefully
            // This can happen when:
            // - File is corrupted
            // - Schema has changed
            // - File contains invalid data (e.g., "Unexpected tag 0")
            Timber.e(e, "Failed to decode VaultPreferences, returning default value")
            defaultValue
        }
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