package com.anytypeio.anytype.features.editor

import androidx.core.os.bundleOf
import androidx.test.espresso.action.ViewActions
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.CoverType
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.utils.checkHasText
import com.anytypeio.anytype.utils.checkIsDisplayed
import com.anytypeio.anytype.utils.onItemView
import com.anytypeio.anytype.utils.rVMatcher
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ProfileTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    private val args = bundleOf(EditorFragment.ID_KEY to root)

    private val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = "Object's layout testing",
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    private val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    val page = Block(
        id = root,
        fields = Block.Fields(emptyMap()),
        content = Block.Content.Smart(SmartBlockType.PAGE),
        children = listOf(header.id)
    )

    val document = listOf(page, header, title)

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun shouldRenderProfileLetterAvatarWhenNoImage() {

        // SETUP

        val details = givenBlockDetailsWithOutImage()

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubAnalytics()
        stubOpenDocument(
            document = document,
            details = details
        )

        // TESTING

        launch(args)


        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.cover).checkIsDisplayed()
            onItemView(0, R.id.imageText).checkHasText("O")
            onItemView(0, R.id.title).checkHasText(title.content<Block.Content.Text>().text)
        }

        val title = R.id.recycler.rVMatcher().run {
            onItemView(0, R.id.title)
        }


        title.apply {
            perform(ViewActions.click())
            perform(ViewActions.replaceText("FooBar"))
        }

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.cover).checkIsDisplayed()
            onItemView(0, R.id.imageText).checkHasText("F")
        }


        title.apply {
            perform(ViewActions.click())
            perform(ViewActions.replaceText(""))
        }

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.cover).checkIsDisplayed()
            onItemView(0, R.id.imageText).checkHasText("U")
        }
    }

    @Test
    fun shouldNotRenderProfileLetterAvatarWhenImage() {

        // SETUP

        val details = givenBlockDetailsWithImage()

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubAnalytics()
        stubOpenDocument(
            document = document,
            details = details
        )

        // TESTING

        launch(args)

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.cover).checkIsDisplayed()
            onItemView(0, R.id.imageText).checkHasText("")
        }

        val title = R.id.recycler.rVMatcher().run {
            onItemView(0, R.id.title)
        }


        title.apply {
            perform(ViewActions.click())
            perform(ViewActions.replaceText("FooBar"))
        }

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.cover).checkIsDisplayed()
            onItemView(0, R.id.imageText).checkHasText("")
        }


        title.apply {
            perform(ViewActions.click())
            perform(ViewActions.replaceText(""))
        }

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.cover).checkIsDisplayed()
            onItemView(0, R.id.imageText).checkHasText("")
        }
    }

    private fun givenBlockDetailsWithImage(): Block.Details {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "iconImage" to "anyimage",
                        "layout" to ObjectType.Layout.PROFILE.code.toDouble(),
                        "coverType" to CoverType.COLOR.code.toDouble(),
                        "coverId" to CoverColor.BLUE.code,
                    )
                )
            )
        )
    }

    private fun givenBlockDetailsWithOutImage(): Block.Details {
        return Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "layout" to ObjectType.Layout.PROFILE.code.toDouble(),
                        "coverType" to CoverType.COLOR.code.toDouble(),
                        "coverId" to CoverColor.BLUE.code,
                    )
                )
            )
        )
    }
}