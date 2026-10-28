package com.anytypeio.anytype.core_ui.common

import coil3.ImageLoader
import coil3.Uri
import coil3.annotation.ExperimentalCoilApi
import coil3.fetch.Fetcher
import coil3.network.CacheStrategy
import coil3.network.ConnectivityChecker
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.Options

/**
 * Fetches the files that the middleware serves on the loopback address.
 *
 * When Android reports no internet, Coil reads only its cache and never sends a request.
 * The middleware gateway runs on the device and works offline, so this factory always
 * sends the request for a loopback url. Other urls are left to the default fetcher.
 */
@OptIn(ExperimentalCoilApi::class)
class LocalGatewayFetcherFactory(
    cacheStrategy: () -> CacheStrategy = { CacheStrategy.DEFAULT }
) : Fetcher.Factory<Uri> {

    private val delegate = OkHttpNetworkFetcherFactory(
        cacheStrategy = cacheStrategy,
        connectivityChecker = { ConnectivityChecker.ONLINE }
    )

    override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
        if (data.scheme != "http") return null
        val host = data.authority?.substringBefore(':') ?: return null
        if (host != LOOPBACK_HOST && host != LOCALHOST) return null
        return delegate.create(data, options, imageLoader)
    }

    companion object {
        private const val LOOPBACK_HOST = "127.0.0.1"
        private const val LOCALHOST = "localhost"

        /**
         * Shared instance for short-lived image loaders, so they reuse one http client.
         */
        val shared: LocalGatewayFetcherFactory by lazy { LocalGatewayFetcherFactory() }
    }
}
