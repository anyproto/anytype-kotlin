package com.anytypeio.anytype.middleware.interactor

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.wire.WireTypeAdapterFactory
import javax.inject.Inject

interface ProtobufConverterProvider {

    fun provideConverter(): Gson

    class Impl @Inject constructor() : ProtobufConverterProvider {
        private val provider = GsonBuilder()
            .registerTypeAdapterFactory(WireTypeAdapterFactory())
            .setPrettyPrinting()
            .create()

        override fun provideConverter(): Gson {
            return provider
        }
    }
}