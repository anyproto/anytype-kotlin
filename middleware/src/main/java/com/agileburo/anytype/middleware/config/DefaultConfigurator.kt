package com.agileburo.anytype.middleware.config

import anytype.Commands.Rpc.Config
import com.agileburo.anytype.data.auth.model.ConfigEntity
import com.agileburo.anytype.data.auth.repo.config.Configurator
import lib.Lib

/**
 * Obtains middleware configuration data.
 */
class DefaultConfigurator : Configurator {

    override fun configure() = get()

    private val builder: () -> ConfigEntity = {
        fetchConfig().let { response ->
            ConfigEntity(
                home = response.homeBlockId,
                gateway = response.gatewayUrl,
                profile = response.profileBlockId
            )
        }
    }

    private var instance: ConfigEntity? = null

    fun get() = instance ?: builder().also { instance = it }

    fun new() = builder().also { instance = it }

    fun release() {
        instance = null
    }

    private fun fetchConfig(): Config.Get.Response {
        val request = Config.Get.Request.newBuilder().build()
        val encoded = Lib.configGet(request.toByteArray())
        val response = Config.Get.Response.parseFrom(encoded)
        return parseResponse(response)
    }

    private fun parseResponse(response: Config.Get.Response): Config.Get.Response {
        return if (response.error != null && response.error.code != Config.Get.Response.Error.Code.NULL) {
            throw Exception(response.error.description)
        } else {
            response
        }
    }
}