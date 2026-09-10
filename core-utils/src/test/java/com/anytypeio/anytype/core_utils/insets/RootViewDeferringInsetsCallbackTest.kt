package com.anytypeio.anytype.core_utils.insets

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28, 30])
class RootViewDeferringInsetsCallbackTest {
    private val root = View(RuntimeEnvironment.getApplication())
    private var controlInset = -1
    private val callback = RootViewDeferringInsetsCallback(
        persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
        deferredInsetTypes = WindowInsetsCompat.Type.ime(),
        onPersistentBottomInset = { controlInset = it }
    )

    @Test fun bottomEdgeIsOpenForBothGestureAndButtonNavigationWithoutChangingOtherEdges() {
        for (navigationBottom in listOf(24, 48, 0)) {
            repeat(2) { callback.onApplyWindowInsets(root, insets(navigationBottom)) }
            assertEquals(0, root.paddingBottom)
            assertEquals(navigationBottom, controlInset)
            assertEquals(7, root.paddingLeft)
            assertEquals(32, root.paddingTop)
            assertEquals(9, root.paddingRight)
        }
    }

    @Test fun keyboardStillResizesTheRootWithoutAddingNavigationPaddingTwice() {
        callback.onApplyWindowInsets(root, insets(navigationBottom = 24, keyboardBottom = 300))
        assertEquals(300, root.paddingBottom)
        assertEquals(0, controlInset)
        callback.onApplyWindowInsets(root, insets(navigationBottom = 24))
        assertEquals(0, root.paddingBottom)
        assertEquals(24, controlInset)
    }

    // Typed IME insets survive the platform redispatch on API 30+. Legacy APIs infer
    // them from an attached window, which this isolated synthetic View does not have.
    @Test
    @Config(sdk = [30])
    fun keyboardResizeIsDeferredUntilAnimationEnds() {
        ViewCompat.setOnApplyWindowInsetsListener(root, callback)
        val animation = WindowInsetsAnimationCompat(WindowInsetsCompat.Type.ime(), null, 200)
        callback.onPrepare(animation)
        callback.onApplyWindowInsets(root, insets(navigationBottom = 24, keyboardBottom = 300))
        assertEquals(0, root.paddingBottom)
        assertEquals(24, controlInset)
        callback.onEnd(animation)
        assertEquals(300, root.paddingBottom)
        assertEquals(0, controlInset)
    }

    @Test fun otherScreensKeepTheirExistingRootPadding() {
        val unchanged = RootViewDeferringInsetsCallback(
            WindowInsetsCompat.Type.systemBars(), WindowInsetsCompat.Type.ime()
        )
        unchanged.onApplyWindowInsets(root, insets(navigationBottom = 48))
        assertEquals(48, root.paddingBottom)
    }

    private fun insets(navigationBottom: Int, keyboardBottom: Int = 0) =
        WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(7, 32, 9, 0))
            .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, navigationBottom))
            .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, keyboardBottom))
            .build()
}
