package com.agileburo.anytype


class ChangeBlockContentTypeTest : BaseNavigationTest() {
    override fun navigateToTestStartDestination() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    //    @Test
//    fun testRecyclerViewSize() {
//        onView(withId(R.id.blockList)).check(matches(withItemCount(3)))
//    }
//
//    @Test
//    fun testShowHideToolbar() {
//        onView(withId(R.id.blockList))
//            .perform(
//                TestUtils.actionOnItemViewAtPosition<EditorAdapter.ViewHolder.ParagraphHolder>(
//                    0,
//                    R.id.btnEditable,
//                    click()
//                )
//            )
//        onView(withContentDescription("blockMenu")).inRoot(RootMatchers.isPlatformPopup()).check(matches(isDisplayed()))
//        onView(withId(R.id.btn_menu_p)).inRoot(RootMatchers.isPlatformPopup()).check(matches(isSelected()))
//        onView(withId(R.id.btn_menu_h2)).inRoot(RootMatchers.isPlatformPopup()).check(matches(not(isSelected())))
//
//        onView(withId(R.id.btn_menu_bullet)).inRoot(RootMatchers.isPlatformPopup()).perform(click())
//
//        Thread.sleep(1000)
//
//        onView(withId(R.id.blockList))
//            .perform(
//                TestUtils.actionOnItemViewAtPosition<EditorAdapter.ViewHolder.BulletHolder>(
//                    0,
//                    R.id.btnBullet,
//                    click()
//                )
//            )
//        onView(withContentDescription("blockMenu")).inRoot(RootMatchers.isPlatformPopup()).check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun testFirstBlockContentType() {
//        onView(withId(R.id.blockList))
//            .perform(
//                TestUtils.actionOnItemViewAtPosition<EditorAdapter.ViewHolder.ParagraphHolder>(
//                    0,
//                    R.id.btnEditable,
//                    click()
//                )
//            )
//        onView(withContentDescription("blockMenu")).inRoot(RootMatchers.isPlatformPopup()).check(matches(isDisplayed()))
//        onView(withId(R.id.btn_menu_p)).inRoot(RootMatchers.isPlatformPopup()).check(matches(isSelected()))
//        onView(withId(R.id.btn_menu_h2)).inRoot(RootMatchers.isPlatformPopup()).check(matches(not(isSelected())))
//    }
//
//    @Test
//    fun testContentTypeShouldBeH4() {
//        onView(withId(R.id.blockList))
//            .perform(
//                TestUtils.actionOnItemViewAtPosition<EditorAdapter.ViewHolder.HeaderFourHolder>(
//                    2,
//                    R.id.btnHeaderFour,
//                    click()
//                )
//            )
//        onView(withContentDescription("blockMenu")).inRoot(RootMatchers.isPlatformPopup()).check(matches(isDisplayed()))
//        onView(withId(R.id.btn_menu_h4)).inRoot(RootMatchers.isPlatformPopup()).check(matches(isSelected()))
//        onView(withId(R.id.btn_menu_h2)).inRoot(RootMatchers.isPlatformPopup()).check(matches(not(isSelected())))
//    }
//
//    @Test
//    fun testBlocksText() {
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(0, R.id.textEditable))
//            .check(matches(withText("Первый")))
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(2, R.id.textHeaderFour))
//            .check(matches(not(withText("Первый")))).check(matches(withText("Пятый")))
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(1, R.id.textEditable))
//            .check(matches(not(withText("Первый")))).check(matches(withText("Третий")))
//    }
//
//    @Test
//    fun testChangeBlocksContentTypes() {
//        //{P, P, H4}
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(0, R.id.textEditable))
//            .check(matches(withText("Первый")))
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(1, R.id.textEditable))
//            .check(matches(withText("Третий")))
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(2, R.id.textHeaderFour))
//            .check(matches(withText("Пятый")))
//
//        //  { Convert block#1, P -> N }
//        onView(withId(R.id.blockList))
//            .perform(
//                TestUtils.actionOnItemViewAtPosition<EditorAdapter.ViewHolder.ParagraphHolder>(
//                    0,
//                    R.id.btnEditable,
//                    click()
//                )
//            )
//        onView(withContentDescription("blockMenu")).inRoot(RootMatchers.isPlatformPopup()).check(matches(isDisplayed()))
//        onView(withId(R.id.btn_menu_p)).inRoot(RootMatchers.isPlatformPopup()).check(matches(isSelected()))
//        onView(withId(R.id.btn_menu_numbered)).inRoot(RootMatchers.isPlatformPopup()).check(matches(not(isSelected())))
//
//        onView(withId(R.id.btn_menu_numbered)).inRoot(RootMatchers.isPlatformPopup()).perform(click())
//
//        //{N, P, H4}
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(0, R.id.contentText))
//            .check(matches(withText("Первый")))
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(1, R.id.textEditable))
//            .check(matches(withText("Третий")))
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(2, R.id.textHeaderFour))
//            .check(matches(withText("Пятый")))
//
//        Thread.sleep(1000)
//
//        //  { Convert block#1, N -> H3 }
//        onView(withId(R.id.blockList))
//            .perform(
//                TestUtils.actionOnItemViewAtPosition<EditorAdapter.ViewHolder.NumberedHolder>(
//                    0,
//                    R.id.btnNumbered,
//                    click()
//                )
//            )
//
//        // Numbered is selected
//        onView(withContentDescription("blockMenu")).inRoot(RootMatchers.isPlatformPopup()).check(matches(isDisplayed()))
//        onView(withId(R.id.btn_menu_numbered)).inRoot(RootMatchers.isPlatformPopup()).check(matches(isSelected()))
//        onView(withId(R.id.btn_menu_h3)).inRoot(RootMatchers.isPlatformPopup()).check(matches(not(isSelected())))
//
//        // Click on H3
//        onView(withId(R.id.btn_menu_h3)).perform(click())
//
//        // Check blocks content
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(0, R.id.textHeaderThree))
//            .check(matches(withText("Первый")))
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(1, R.id.textEditable))
//            .check(matches(withText("Третий")))
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(2, R.id.textHeaderFour))
//            .check(matches(withText("Пятый")))
//
//        Thread.sleep(1000)
//        //  { Convert block#2, P -> N }
//        onView(withId(R.id.blockList))
//            .perform(
//                TestUtils.actionOnItemViewAtPosition<EditorAdapter.ViewHolder.ParagraphHolder>(
//                    1,
//                    R.id.btnEditable,
//                    click()
//                )
//            )
//
//        // P is selected
//        onView(withContentDescription("blockMenu")).inRoot(RootMatchers.isPlatformPopup()).check(matches(isDisplayed()))
//        onView(withId(R.id.btn_menu_p)).inRoot(RootMatchers.isPlatformPopup()).check(matches(isSelected()))
//        onView(withId(R.id.btn_menu_numbered)).inRoot(RootMatchers.isPlatformPopup()).check(matches(not(isSelected())))
//
//        // Click on N
//        onView(withId(R.id.btn_menu_numbered)).inRoot(RootMatchers.isPlatformPopup()).perform(click())
//
//        // Check blocks content
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(0, R.id.textHeaderThree))
//            .check(matches(withText("Первый")))
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(1, R.id.contentText))
//            .check(matches(withText("Третий")))
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(2, R.id.textHeaderFour))
//            .check(matches(withText("Пятый")))
//
//        Thread.sleep(1000)
//        //  { Convert block#3, H4 -> P }
//        onView(withId(R.id.blockList))
//            .perform(
//                TestUtils.actionOnItemViewAtPosition<EditorAdapter.ViewHolder.HeaderFourHolder>(
//                    2,
//                    R.id.btnHeaderFour,
//                    click()
//                )
//            )
//
//        // H4 is selected
//        onView(withContentDescription("blockMenu")).inRoot(RootMatchers.isPlatformPopup()).check(matches(isDisplayed()))
//        onView(withId(R.id.btn_menu_h4)).inRoot(RootMatchers.isPlatformPopup()).check(matches(isSelected()))
//        onView(withId(R.id.btn_menu_p)).inRoot(RootMatchers.isPlatformPopup()).check(matches(not(isSelected())))
//
//        // Click on P
//        onView(withId(R.id.btn_menu_p)).inRoot(RootMatchers.isPlatformPopup()).perform(click())
//
//        // Check blocks content
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(0, R.id.textHeaderThree))
//            .check(matches(withText("Первый")))
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(1, R.id.contentText))
//            .check(matches(withText("Третий")))
//        onView(withRecyclerView(R.id.blockList).atPositionOnView(2, R.id.textEditable))
//            .check(matches(withText("Пятый")))
//    }
}

