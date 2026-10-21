package com.anytypeio.anytype.features.vault

import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.feature_vault.presentation.VaultUiState
import com.anytypeio.anytype.features.vault.VaultScrollTestFragment.Companion.space
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class VaultScrollTest {
    @Test
    fun insertingSpaceAtTopKeepsEveryFrameAtTop() = withVault { scenario ->
        update(scenario) { it.copy(mainSpaces = listOf(space("new-space")) + it.mainSpaces) }
        assertAtTop(scenario, "new-space")
    }

    @Test
    fun movingSpaceFromFiveToZeroKeepsEveryFrameAtTop() = withVault { scenario ->
        update(scenario) { sections ->
            val spaces = sections.mainSpaces.toMutableList()
            spaces.add(0, spaces.removeAt(5))
            sections.copy(mainSpaces = spaces)
        }
        assertAtTop(scenario, "space-5")
    }

    @Test
    fun pinningSpaceAboveMainSectionKeepsEveryFrameAtTop() = withVault { scenario ->
        update(scenario) {
            it.copy(pinnedSpaces = listOf(space("space-5", pinned = true)),
                mainSpaces = it.mainSpaces.filterNot { space -> space.space.id == "space-5" })
        }
        assertAtTop(scenario, "space-5")
    }

    @Test
    fun reorderingPinnedSpacesKeepsEveryFrameAtTop() = withVault { scenario ->
        update(scenario) { it.copy(pinnedSpaces = (0..5).map { space("pinned-$it", pinned = true) }) }
        update(scenario) { it.copy(pinnedSpaces = it.pinnedSpaces.reversed()) }
        assertAtTop(scenario, "pinned-5")
    }

    @Test
    fun insertingSpacePreservesScrolledCardAndOffset() = withVault(index = 8, offset = 37) { scenario ->
        update(scenario) { it.copy(mainSpaces = listOf(space("new-space")) + it.mainSpaces) }
        assertAnchor(scenario, "space-8", 37)
    }

    @Test
    fun movingSpacePreservesPartiallyScrolledFirstCard() = withVault(offset = 37) { scenario ->
        update(scenario) { sections ->
            val spaces = sections.mainSpaces.toMutableList()
            spaces.add(0, spaces.removeAt(5))
            sections.copy(mainSpaces = spaces)
        }
        assertAnchor(scenario, "space-0", 37)
    }

    @Test
    fun subscriptionUpdateDoesNotCancelAnActiveScroll() = withVault { scenario ->
        lateinit var scroll: Job
        scenario.onFragment { fixture ->
            scroll = fixture.scope.launch(start = CoroutineStart.UNDISPATCHED) {
                fixture.listState.scroll { awaitCancellation() }
            }
            assertTrue(fixture.listState.isScrollInProgress)
        }
        try {
            update(scenario) { it.copy(mainSpaces = listOf(space("new-space")) + it.mainSpaces) }
            scenario.onFragment { assertTrue("Subscription update cancelled scrolling", scroll.isActive) }
        } finally {
            scenario.onFragment { scroll.cancel() }
        }
    }

    private fun assertAtTop(scenario: FragmentScenario<VaultScrollTestFragment>, key: String) {
        scenario.onFragment { fixture ->
            assertEquals(key, fixture.viewport().key)
            assertTrue("No rendered frames recorded", fixture.frames.isNotEmpty())
            fixture.frames.forEach { frame ->
                assertEquals("Viewport jumped: $frame", 0, frame.index)
                assertEquals("Viewport jumped: $frame", 0, frame.offset)
            }
        }
    }

    private fun assertAnchor(scenario: FragmentScenario<VaultScrollTestFragment>, key: String, offset: Int) {
        scenario.onFragment { fixture ->
            assertTrue("No rendered frames recorded", fixture.frames.isNotEmpty())
            fixture.frames.forEach { frame ->
                assertEquals("Visible card changed: $frame", key, frame.key)
                assertEquals("Viewport jumped: $frame", offset, frame.offset)
            }
        }
    }

    private fun update(
        scenario: FragmentScenario<VaultScrollTestFragment>,
        transform: (VaultUiState.Sections) -> VaultUiState.Sections
    ) {
        lateinit var updated: VaultUiState.Sections
        scenario.onFragment { fixture ->
            fixture.frames.clear()
            updated = transform(fixture.sections.value)
            fixture.sections.value = updated
        }
        await(scenario) { it.renderedSections == updated && it.frames.isNotEmpty() }
        // Keep item animations enabled and sample draws throughout the placement transition.
        SystemClock.sleep(500)
    }

    private fun withVault(index: Int = 0, offset: Int = 0, block: (FragmentScenario<VaultScrollTestFragment>) -> Unit) {
        val scenario = launchFragmentInContainer<VaultScrollTestFragment>(
            fragmentArgs = Bundle().apply { putInt("index", index); putInt("offset", offset) },
            themeResId = R.style.AppTheme
        )
        try {
            await(scenario) { it.frames.isNotEmpty() }
            scenario.onFragment {
                assertEquals(index, it.viewport().index)
                assertEquals(offset, it.viewport().offset)
            }
            block(scenario)
        } finally {
            scenario.close()
        }
    }

    private fun await(scenario: FragmentScenario<VaultScrollTestFragment>, ready: (VaultScrollTestFragment) -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 5_000
        do {
            var satisfied = false
            scenario.onFragment { satisfied = ready(it) }
            if (satisfied) return
            SystemClock.sleep(16)
        } while (SystemClock.uptimeMillis() < deadline)
        scenario.onFragment {
            throw AssertionError("Vault list did not finish rendering: " +
                "state=${it.viewport()}, frames=${it.frames.size}, " +
                "updated=${it.renderedSections == it.sections.value}")
        }
    }
}
