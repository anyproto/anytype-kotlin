package com.anytypeio.anytype.presentation.navigation

import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NavigationViewModelTest {

    private class TestNavigationViewModel : NavigationViewModel<String>()

    @Test
    fun `should buffer navigation destination emitted before a collector subscribes`() = runTest {
        // GIVEN
        val vm = TestNavigationViewModel()

        // WHEN — a destination is emitted while NOBODY is collecting `navigation`.
        // This reproduces a deeplink resolved during the activity stop -> resume gap
        // on cold start, before the screen's navigation collector has attached.
        vm.navigation("destination-1")

        // THEN — a collector subscribing afterwards must still receive it. With a
        // `replay = 0` SharedFlow the destination was silently dropped; the buffered
        // Channel delivers it to the late subscriber (DROID-4523).
        vm.navigation.test {
            assertEquals("destination-1", awaitItem())
        }
    }
}
