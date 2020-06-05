package com.agileburo.anytype.features.editor

import android.os.Bundle
import android.view.KeyEvent
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.event.model.Payload
import com.agileburo.anytype.features.editor.base.EditorTestSetup
import com.agileburo.anytype.features.editor.base.TestPageFragment
import com.agileburo.anytype.mocking.MockDataFactory
import com.agileburo.anytype.ui.page.PageFragment
import com.agileburo.anytype.utils.CoroutinesTestRule
import com.agileburo.anytype.utils.TestUtils.withRecyclerView
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.android.synthetic.main.fragment_page.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MergeBlockTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun shouldMergeTwoParagraphs() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val style = Block.Content.Text.Style.P

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo",
                marks = emptyList(),
                style = style
            )
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Bar",
                marks = emptyList(),
                style = style
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(a.id, b.id)
        )

        val document = listOf(page, a, b)

        val events = listOf(
            Event.Command.GranularChange(
                context = root,
                id = a.id,
                text = "FooBar"
            ),
            Event.Command.UpdateStructure(
                context = root,
                id = root,
                children = listOf(a.id)
            ),
            Event.Command.DeleteBlock(
                context = root,
                targets = listOf(b.id)
            )
        )

        val command = Command.Merge(
            context = root,
            pair = Pair(a.id, b.id)
        )

        stubInterceptEvents()
        stubOpenDocument(document)
        stubUpdateText()
        stubMergelocks(
            command = command,
            events = events
        )

        val scenario = launchFragment(args)

        val targetViewId = R.id.textContent

        // TESTING

        val target = Espresso.onView(
            withRecyclerView(R.id.recycler).atPositionOnView(2, targetViewId)
        )

        target.apply {
            perform(ViewActions.click())
        }

        // Set cursor at the beginning of B

        scenario.onFragment { fragment ->
            val item = fragment.recycler.getChildAt(2)
            val view = item.findViewById<TextInputWidget>(targetViewId)
            view.setSelection(0)
        }

        // Press BACKSPACE

        target.perform(ViewActions.pressKey(KeyEvent.KEYCODE_BACK))

        Thread.sleep(3000)
    }

    private fun stubMergelocks(
        command: Command.Merge,
        events: List<Event.Command>
    ) {
        repo.stub {
            onBlocking {
                merge(command = command)
            } doReturn Payload(context = root, events = events)
        }
    }

    private fun launchFragment(args: Bundle) : FragmentScenario<TestPageFragment> {
        return launchFragmentInContainer<TestPageFragment>(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }

}