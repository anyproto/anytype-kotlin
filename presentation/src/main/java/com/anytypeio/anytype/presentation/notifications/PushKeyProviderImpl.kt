package com.anytypeio.anytype.presentation.notifications

import android.content.SharedPreferences
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.chats.PushKeyChannel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

interface PushKeyProvider {
    fun getPushKey(): PushKey
}

data class PushKey(
    val key: String,
    val id: String
) {
    companion object {
        val EMPTY = PushKey(key = "", id = "")
    }
}

class PushKeyProviderImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val channel: PushKeyChannel,
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
                        pushKey = event.encryptionKey,
                        pushKeyId = event.encryptionKeyId
                    )
                }
        }
    }

    private fun savePushKey(pushKey: String?, pushKeyId: String?) {
        sharedPreferences.edit().apply {
            putString(PREF_PUSH_KEY, pushKey)
            putString(PREF_PUSH_KEY_ID, pushKeyId)
            apply()
        }
    }

    override fun getPushKey(): PushKey {
        val pushKey = sharedPreferences.getString(PREF_PUSH_KEY, "") ?: ""
        val pushKeyId = sharedPreferences.getString(PREF_PUSH_KEY_ID, "") ?: ""
        Timber.d("PushKeyProvider getPushKey: $pushKey, $pushKeyId")
        return PushKey(key = pushKey, id = pushKeyId)
    }

    companion object {
        const val PREF_PUSH_KEY = "pref.push_key"
        const val PREF_PUSH_KEY_ID = "pref.push_key_id"
    }
}
