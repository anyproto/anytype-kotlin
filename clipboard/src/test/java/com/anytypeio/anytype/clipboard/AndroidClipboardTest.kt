package com.anytypeio.anytype.clipboard

import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class AndroidClipboardTest {

    private lateinit var clipboard : AnytypeClipboard

    private lateinit var cm: ClipboardManager

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard = AnytypeClipboard(cm = cm)
    }

    @Test
    fun `should put only text and uri`() {
        val text = MockDataFactory.randomString()

        runBlocking {
            clipboard.put(
                text = text,
                html = null
            )
        }

        assertEquals(
            expected = text,
            actual = cm.primaryClip?.getItemAt(0)?.text
        )

        assertNull(cm.primaryClip?.getItemAt(0)?.htmlText)
        assertNotNull(cm.primaryClip?.getItemAt(1)?.uri)
    }

    @Test
    fun `should put text, html as first item and uri as second item`() {
        val text = MockDataFactory.randomString()
        val html = MockDataFactory.randomString()

        runBlocking {
            clipboard.put(
                text = text,
                html = html
            )
        }

        assertEquals(
            expected = text,
            actual = cm.primaryClip?.getItemAt(0)?.text
        )

        assertEquals(
            expected = html,
            actual = cm.primaryClip?.getItemAt(0)?.htmlText
        )

        assertNull(cm.primaryClip?.getItemAt(0)?.uri)
        assertNotNull(cm.primaryClip?.getItemAt(1)?.uri)
    }
}