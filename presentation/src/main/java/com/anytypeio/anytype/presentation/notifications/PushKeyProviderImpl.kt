package com.anytypeio.anytype.presentation.notifications

import android.content.SharedPreferences
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.chats.PushKeyChannel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

interface PushKeyProvider {
    fun getPushKey(): Map<String, PushKey>
}

data class PushKey(
    val id: String,
    val value: String
) {
    companion object {
        val EMPTY = PushKey(value = "", id = "")
    }
}

class PushKeyProviderImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val channel: PushKeyChannel,
    private val gson: Gson,
    dispatchers: AppCoroutineDispatchers,
    scope: CoroutineScope
) : PushKeyProvider {

    init {
        Timber.d("PushKeyProvider initialized")
        scope.launch(dispatchers.io) {
            channel
                .observe()
                .collect { event ->
                    Timber.d("New push key updates: $event")
                    savePushKey(
                        id = event.encryptionKeyId,
                        value = event.encryptionKey
                    )
                }
        }
    }

    private fun savePushKey(id: String?, value: String?) {
        val storedKeys = getStoredPushKeys().toMutableMap()

        if (!value.isNullOrEmpty() && !id.isNullOrEmpty()) {
            storedKeys[id] = PushKey(id = id, value = value)
        }

        sharedPreferences.edit().apply {
            putString(PREF_PUSH_KEYS, gson.toJson(storedKeys))
            apply()
        }
    }

    override fun getPushKey(): Map<String, PushKey> {
        val storedKeysJson = sharedPreferences.getString(PREF_PUSH_KEYS, "{}") ?: "{}"
        return gson.fromJson(storedKeysJson, object : TypeToken<Map<String, PushKey>>() {}.type)
    }

    private fun getStoredPushKeys(): Map<String, PushKey> {
        val storedKeysJson = sharedPreferences.getString(PREF_PUSH_KEYS, "{}") ?: "{}"
        return gson.fromJson(storedKeysJson, object : TypeToken<Map<String, PushKey>>() {}.type)
    }

    companion object {
        const val PREF_PUSH_KEYS = "pref.push_keys"
    }
}
