package com.anytypeio.anytype.core_ui

import android.Manifest.permission.ACCESS_NETWORK_STATE
import android.app.Application
import android.net.ConnectivityManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import coil3.ImageLoader
import coil3.network.HttpException
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.Options
import coil3.toUri
import com.anytypeio.anytype.core_ui.common.LocalGatewayFetcherFactory
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Reproduces #3322 without a device: Android reports no default network,
 * and a server on the loopback address stands in for the middleware gateway.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class LocalGatewayOfflineFetchTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val requests = AtomicInteger(0)
    private lateinit var gateway: ServerSocket
    private lateinit var url: String

    @Before
    fun setup() {
        // Coil checks connectivity only when the app may read the network state.
        shadowOf(context).grantPermissions(ACCESS_NETWORK_STATE)
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        shadowOf(connectivityManager).setDefaultNetworkActive(false)

        gateway = ServerSocket(0, 50, InetAddress.getLoopbackAddress())
        thread(isDaemon = true) {
            while (!gateway.isClosed) {
                val socket = try {
                    gateway.accept()
                } catch (e: SocketException) {
                    break
                }
                socket.use { serve(it) }
            }
        }
        url = "http://127.0.0.1:${gateway.localPort}/image/bafyreid"
    }

    @After
    fun tearDown() {
        gateway.close()
    }

    private fun serve(socket: Socket) {
        val reader = socket.getInputStream().bufferedReader()
        while (!reader.readLine().isNullOrEmpty()) {
            // Skip the request line and the headers.
        }
        requests.incrementAndGet()
        val headers = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: image/png\r\n" +
                "Content-Length: ${IMAGE.size}\r\n" +
                "Connection: close\r\n\r\n"
        socket.getOutputStream().apply {
            write(headers.toByteArray())
            write(IMAGE)
            flush()
        }
    }

    @Test
    fun `default fetcher does not reach the gateway when the device is offline`() {
        val imageLoader = ImageLoader(context)
        val fetcher = OkHttpNetworkFetcherFactory().create(
            data = url.toUri(),
            options = Options(context),
            imageLoader = imageLoader
        )

        val error = assertFailsWith<HttpException> {
            runBlocking(Dispatchers.IO) { fetcher!!.fetch() }
        }

        assertEquals(504, error.response.code)
        assertEquals(0, requests.get())
    }

    @Test
    fun `gateway fetcher loads the file when the device is offline`() {
        val imageLoader = ImageLoader(context)
        val fetcher = LocalGatewayFetcherFactory().create(
            data = url.toUri(),
            options = Options(context),
            imageLoader = imageLoader
        )

        val result = runBlocking(Dispatchers.IO) { fetcher!!.fetch() }

        assertNotNull(result)
        assertEquals(1, requests.get())
    }

    companion object {
        private val IMAGE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    }
}
