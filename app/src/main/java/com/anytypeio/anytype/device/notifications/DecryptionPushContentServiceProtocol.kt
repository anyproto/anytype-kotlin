package com.anytypeio.anytype.device.notifications

import android.content.SharedPreferences
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

interface DecryptionPushContentServiceProtocol {
    fun decrypt(encryptedData: ByteArray, keyId: String): DecryptedPushContent?
}

class DecryptionPushContentService(
    private val encryptedPrefs: SharedPreferences,
    private val cryptoService: CryptoServiceProtocol = CryptoService(),
//    private val encryptionKeyService: EncryptionKeyServiceProtocol = AppContainer.shared.encryptionKeyService(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : DecryptionPushContentServiceProtocol {

    @OptIn(ExperimentalEncodingApi::class)
    override fun decrypt(encryptedData: ByteArray, keyId: String): DecryptedPushContent? {
        return try {
            // Получаем ключ в виде Base64-строки
            val keyString = "test1983"//   encryptedPrefs.getString(keyId)
            val keyData = byteArrayOf()//Base64.decode(keyString, Base64.DEFAULT)

            // Расшифровываем данные
            val decryptedData = cryptoService.decryptAESGCM(data = encryptedData, keyData = keyData)

            // Декодируем JSON в объект
            json.decodeFromStream<DecryptedPushContent>(decryptedData.inputStream())
        } catch (e: Exception) {
            // Логируем или обрабатываем ошибку при необходимости
            // например: Log.e("DecryptionService", "Decryption failed", e)
            null
        }
    }
}

// Исключения, если захочется их поднимать
sealed class DecryptionPushContentError(message: String) : Throwable(message) {
    class KeyEncodeFailed : DecryptionPushContentError("Failed to decode key from Base64")
}