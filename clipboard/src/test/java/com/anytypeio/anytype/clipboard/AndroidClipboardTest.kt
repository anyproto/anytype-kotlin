package com.anytypeio.anytype.clipboard

import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.clipboard.BuildConfig.ANYTYPE_CLIPBOARD_URI
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class AndroidClipboardTest {

    private lateinit var clipboard: AnytypeClipboard

    private lateinit var cm: ClipboardManager

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard = AnytypeClipboard(cm = cm)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should put only text with anytype extras`() = runTest(testDispatcher) {
        val text = MockDataFactory.randomString()

        clipboard.put(
            text = text,
            html = null,
            ignoreHtml = MockDataFactory.randomBoolean()
        )

        assertEquals(
            expected = text,
            actual = cm.primaryClip?.getItemAt(0)?.text
        )

        assertNull(cm.primaryClip?.getItemAt(0)?.htmlText)

        // Should have only one item (no URI item)
        assertEquals(expected = 1, actual = cm.primaryClip?.itemCount)

        // Anytype source should be in extras
        assertEquals(
            expected = ANYTYPE_CLIPBOARD_URI,
            actual = cm.primaryClip?.description?.extras?.getString(AnytypeClipboard.EXTRAS_KEY_SOURCE)
        )
    }

    @Test
    fun `should put text and ignore html with anytype extras`() = runTest(testDispatcher) {
        val text = MockDataFactory.randomString()
        val html = MockDataFactory.randomString()

        clipboard.put(
            text = text,
            html = html,
            ignoreHtml = true
        )

        assertEquals(
            expected = text,
            actual = cm.primaryClip?.getItemAt(0)?.text
        )

        assertEquals(
            expected = null,
            actual = cm.primaryClip?.getItemAt(0)?.htmlText
        )

        // Should have only one item (no URI item)
        assertEquals(expected = 1, actual = cm.primaryClip?.itemCount)

        // Anytype source should be in extras
        assertEquals(
            expected = ANYTYPE_CLIPBOARD_URI,
            actual = cm.primaryClip?.description?.extras?.getString(AnytypeClipboard.EXTRAS_KEY_SOURCE)
        )
    }

    @Test
    fun `should put text and html as first item with anytype extras`() = runTest(testDispatcher) {
        val text = MockDataFactory.randomString()
        val html = MockDataFactory.randomString()

        clipboard.put(
            text = text,
            html = html,
            ignoreHtml = false
        )

        assertEquals(
            expected = text,
            actual = cm.primaryClip?.getItemAt(0)?.text
        )

        assertEquals(
            expected = html,
            actual = cm.primaryClip?.getItemAt(0)?.htmlText
        )

        // Should have only one item (no URI item)
        assertEquals(expected = 1, actual = cm.primaryClip?.itemCount)

        // Anytype source should be in extras
        assertEquals(
            expected = ANYTYPE_CLIPBOARD_URI,
            actual = cm.primaryClip?.description?.extras?.getString(AnytypeClipboard.EXTRAS_KEY_SOURCE)
        )
    }
}
