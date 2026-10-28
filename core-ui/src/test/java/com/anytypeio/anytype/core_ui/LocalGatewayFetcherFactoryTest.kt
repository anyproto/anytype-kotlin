package com.anytypeio.anytype.core_ui

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import coil3.ImageLoader
import coil3.request.Options
import coil3.toUri
import com.anytypeio.anytype.core_ui.common.LocalGatewayFetcherFactory
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class LocalGatewayFetcherFactoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val imageLoader = ImageLoader(context)
    private val options = Options(context)
    private val factory = LocalGatewayFetcherFactory()

    @Test
    fun `should fetch an image from the middleware gateway`() {
        val fetcher = factory.create(
            data = "http://127.0.0.1:47800/image/bafyreid".toUri(),
            options = options,
            imageLoader = imageLoader
        )
        assertNotNull(fetcher)
    }

    @Test
    fun `should fetch a file from the gateway on localhost`() {
        val fetcher = factory.create(
            data = "http://localhost:47800/file/bafyreid".toUri(),
            options = options,
            imageLoader = imageLoader
        )
        assertNotNull(fetcher)
    }

    @Test
    fun `should leave a remote url to the default fetcher`() {
        val fetcher = factory.create(
            data = "https://example.com/image.png".toUri(),
            options = options,
            imageLoader = imageLoader
        )
        assertNull(fetcher)
    }

    @Test
    fun `should leave a remote host that starts with the loopback address`() {
        val fetcher = factory.create(
            data = "http://127.0.0.1.example.com/image.png".toUri(),
            options = options,
            imageLoader = imageLoader
        )
        assertNull(fetcher)
    }
}
