package com.agileburo.anytype

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.agileburo.anytype.CustomMatchers.Companion.withItemCount
import com.agileburo.anytype.TestUtils.withRecyclerView
import com.agileburo.anytype.feature_editor.ui.EditorAdapter
import org.hamcrest.CoreMatchers.*
import org.junit.Test


class ChangeBlockContentTypeTest : BaseNavigationTest() {

    override fun navigateToTestStartDestination() {
    }

    @Test
    fun testRecyclerViewSize() {
        onView(withId(R.id.blockList)).check(matches(withItemCount(3)))
    }

    @Test
    fun testFirstBlockContentType() {
        onView(withId(R.id.blockList))
            .perform(
                TestUtils.actionOnItemViewAtPosition<EditorAdapter.ViewHolder.ParagraphHolder>(
                    0,
                    R.id.btnEditable,
                    click()
                )
            )
        onView(withId(R.id.content_type_toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_cont_type_toolbar_p)).check(matches(isSelected()))
        onView(withId(R.id.btn_cont_type_toolbar_h1)).check(matches(not(isSelected())))
    }

    @Test
    fun testContentTypeShouldBeH4() {
        onView(withId(R.id.blockList))
            .perform(
                TestUtils.actionOnItemViewAtPosition<EditorAdapter.ViewHolder.HeaderFourHolder>(
                    2,
                    R.id.btnHeaderFour,
                    click()
                )
            )
        onView(withId(R.id.content_type_toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_cont_type_toolbar_h4)).check(matches(isSelected()))
        onView(withId(R.id.btn_cont_type_toolbar_h1)).check(matches(not(isSelected())))
    }

    @Test
    fun testBlocksText() {
        onView(withRecyclerView(R.id.blockList).atPositionOnView(0, R.id.textEditable))
            .check(matches(withText("Первый")))
        onView(withRecyclerView(R.id.blockList).atPositionOnView(2, R.id.textHeaderFour))
            .check(matches(not(withText("Первый")))).check(matches(withText("Пятый")))
        onView(withRecyclerView(R.id.blockList).atPositionOnView(1, R.id.textEditable))
            .check(matches(not(withText("Первый")))).check(matches(withText("Третий")))
    }

    @Test
    fun testChangeBlocksContentTypes() {
        //{P, P, H4}
        onView(withRecyclerView(R.id.blockList).atPositionOnView(0, R.id.textEditable))
            .check(matches(withText("Первый")))
        onView(withRecyclerView(R.id.blockList).atPositionOnView(1, R.id.textEditable))
            .check(matches(withText("Третий")))
        onView(withRecyclerView(R.id.blockList).atPositionOnView(2, R.id.textHeaderFour))
            .check(matches(withText("Пятый")))

        //  { Convert block#1, P -> N }
        onView(withId(R.id.blockList))
            .perform(
                TestUtils.actionOnItemViewAtPosition<EditorAdapter.ViewHolder.ParagraphHolder>(
                    0,
                    R.id.btnEditable,
                    click()
                )
            )
        onView(withId(R.id.content_type_toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_cont_type_toolbar_p)).check(matches(isSelected()))
        onView(withId(R.id.btn_cont_type_toolbar_numbered)).check(matches(not(isSelected())))

        onView(withId(R.id.btn_cont_type_toolbar_numbered)).perform(click())
        onView(withId(R.id.content_type_toolbar)).check(matches(not(isDisplayed())))

        //{N, P, H4}
        onView(withRecyclerView(R.id.blockList).atPositionOnView(0, R.id.contentText))
            .check(matches(withText("Первый")))
        onView(withRecyclerView(R.id.blockList).atPositionOnView(1, R.id.textEditable))
            .check(matches(withText("Третий")))
        onView(withRecyclerView(R.id.blockList).atPositionOnView(2, R.id.textHeaderFour))
            .check(matches(withText("Пятый")))

        Thread.sleep(1000)

        //  { Convert block#1, N -> H3 }
        onView(withId(R.id.blockList))
            .perform(
                TestUtils.actionOnItemViewAtPosition<EditorAdapter.ViewHolder.NumberedHolder>(
                    0,
                    R.id.btnNumbered,
                    click()
                )
            )

        // Numbered is selected
        onView(withId(R.id.content_type_toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_cont_type_toolbar_numbered)).check(matches(isSelected()))
        onView(withId(R.id.btn_cont_type_toolbar_h3)).check(matches(not(isSelected())))

        // Click on H3
        onView(withId(R.id.btn_cont_type_toolbar_h3)).perform(click())
        onView(withId(R.id.content_type_toolbar)).check(matches(not(isDisplayed())))

        // Check blocks content
        onView(withRecyclerView(R.id.blockList).atPositionOnView(0, R.id.textHeaderThree))
            .check(matches(withText("Первый")))
        onView(withRecyclerView(R.id.blockList).atPositionOnView(1, R.id.textEditable))
            .check(matches(withText("Третий")))
        onView(withRecyclerView(R.id.blockList).atPositionOnView(2, R.id.textHeaderFour))
            .check(matches(withText("Пятый")))

        Thread.sleep(1000)
        //  { Convert block#2, P -> N }
        onView(withId(R.id.blockList))
            .perform(
                TestUtils.actionOnItemViewAtPosition<EditorAdapter.ViewHolder.ParagraphHolder>(
                    1,
                    R.id.btnEditable,
                    click()
                )
            )

        // P is selected
        onView(withId(R.id.content_type_toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_cont_type_toolbar_p)).check(matches(isSelected()))
        onView(withId(R.id.btn_cont_type_toolbar_numbered)).check(matches(not(isSelected())))

        // Click on N
        onView(withId(R.id.btn_cont_type_toolbar_numbered)).perform(click())
        onView(withId(R.id.content_type_toolbar)).check(matches(not(isDisplayed())))

        // Check blocks content
        onView(withRecyclerView(R.id.blockList).atPositionOnView(0, R.id.textHeaderThree))
            .check(matches(withText("Первый")))
        onView(withRecyclerView(R.id.blockList).atPositionOnView(1, R.id.contentText))
            .check(matches(withText("Третий")))
        onView(withRecyclerView(R.id.blockList).atPositionOnView(2, R.id.textHeaderFour))
            .check(matches(withText("Пятый")))

        Thread.sleep(1000)
        //  { Convert block#3, H4 -> P }
        onView(withId(R.id.blockList))
            .perform(
                TestUtils.actionOnItemViewAtPosition<EditorAdapter.ViewHolder.HeaderFourHolder>(
                    2,
                    R.id.btnHeaderFour,
                    click()
                )
            )

        // H4 is selected
        onView(withId(R.id.content_type_toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_cont_type_toolbar_h4)).check(matches(isSelected()))
        onView(withId(R.id.btn_cont_type_toolbar_p)).check(matches(not(isSelected())))

        // Click on P
        onView(withId(R.id.btn_cont_type_toolbar_p)).perform(click())
        onView(withId(R.id.content_type_toolbar)).check(matches(not(isDisplayed())))

        // Check blocks content
        onView(withRecyclerView(R.id.blockList).atPositionOnView(0, R.id.textHeaderThree))
            .check(matches(withText("Первый")))
        onView(withRecyclerView(R.id.blockList).atPositionOnView(1, R.id.contentText))
            .check(matches(withText("Третий")))
        onView(withRecyclerView(R.id.blockList).atPositionOnView(2, R.id.textEditable))
            .check(matches(withText("Пятый")))
    }
}

