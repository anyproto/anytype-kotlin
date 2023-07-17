package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.SearchHighlightSpan
import com.anytypeio.anytype.core_ui.common.SearchTargetHighlightSpan
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkArchiveBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkCardMediumIconBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkCardMediumIconCoverBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkCardSmallIconBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkCardSmallIconCoverBinding
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.TestFragment
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.P],
    instrumentedPackages = [
        "androidx.loader.content"
    ]
)
class LinkToObjectTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: FragmentScenario<TestFragment>

    private lateinit var linkToObject: LinkToObject
    private lateinit var linkToObjectArchived: LinkToObjectArchive
    private lateinit var linkToObjectCardSmallIcon: LinkToObjectCardSmallIcon
    private lateinit var linkToObjectCardSmallIconCover: LinkToObjectCardSmallIconCover
    private lateinit var linkToObjectCardMediumIcon: LinkToObjectCardMediumIcon
    private lateinit var linkToObjectCardMediumIconCover: LinkToObjectCardMediumIconCover

    private lateinit var item: BlockView.LinkToObject.Default.Text
    private lateinit var itemArchived: BlockView.LinkToObject.Archived
    private lateinit var itemCardSmallIcon: BlockView.LinkToObject.Default.Card.SmallIcon
    private lateinit var itemCardSmallIconCover: BlockView.LinkToObject.Default.Card.SmallIconCover
    private lateinit var itemCardMediumIcon: BlockView.LinkToObject.Default.Card.MediumIcon
    private lateinit var itemCardMediumIconCover: BlockView.LinkToObject.Default.Card.MediumIconCover


    @Before
    fun setup() {
        context.setTheme(R.style.Theme_MaterialComponents)
        scenario = launchFragmentInContainer()

        val layoutInflater = LayoutInflater.from(context)
        linkToObject = LinkToObject(ItemBlockObjectLinkBinding.inflate(layoutInflater))
        linkToObjectArchived = LinkToObjectArchive(ItemBlockObjectLinkArchiveBinding.inflate(layoutInflater))
        linkToObjectCardSmallIcon = LinkToObjectCardSmallIcon(ItemBlockObjectLinkCardSmallIconBinding.inflate(layoutInflater))
        linkToObjectCardSmallIconCover = LinkToObjectCardSmallIconCover(
            ItemBlockObjectLinkCardSmallIconCoverBinding.inflate(layoutInflater))
        linkToObjectCardMediumIcon = LinkToObjectCardMediumIcon(
            ItemBlockObjectLinkCardMediumIconBinding.inflate(layoutInflater))
        linkToObjectCardMediumIconCover = LinkToObjectCardMediumIconCover(
            ItemBlockObjectLinkCardMediumIconCoverBinding.inflate(layoutInflater))

        val id = MockDataFactory.randomUuid()

        val searchField = BlockView.Searchable.Field(
            highlights = listOf(IntRange(0, 4), IntRange(6, 10)),
            target = IntRange(0, 4)
        )

        item = BlockView.LinkToObject.Default.Text(
            id = id,
            indent = MockDataFactory.randomInt(),
            isSelected = false,
            icon = ObjectIcon.None,
            searchFields = listOf(searchField),
            text = "Text1 Text2"
        )

        itemArchived = BlockView.LinkToObject.Archived(
            id = id,
            indent = MockDataFactory.randomInt(),
            isSelected = false,
            searchFields = listOf(searchField),
            text = "Text1 Text2"
        )

        itemCardSmallIcon = BlockView.LinkToObject.Default.Card.SmallIcon(
            id = id,
            indent = MockDataFactory.randomInt(),
            isSelected = false,
            icon = ObjectIcon.None,
            searchFields = listOf(searchField),
            text = "Text1 Text2",
            background = ThemeColor.BLUE,
            isPreviousBlockMedia = false
        )

        itemCardSmallIconCover = BlockView.LinkToObject.Default.Card.SmallIconCover(
            id = id,
            indent = MockDataFactory.randomInt(),
            isSelected = false,
            icon = ObjectIcon.None,
            searchFields = listOf(searchField),
            text = "Text1 Text2",
            background = ThemeColor.BLUE,
            isPreviousBlockMedia = false,
            cover = null
        )

        itemCardMediumIcon = BlockView.LinkToObject.Default.Card.MediumIcon(
            id = id,
            indent = MockDataFactory.randomInt(),
            isSelected = false,
            icon = ObjectIcon.None,
            searchFields = listOf(searchField),
            text = "Text1 Text2",
            background = ThemeColor.BLUE,
            isPreviousBlockMedia = false
        )

        itemCardMediumIconCover = BlockView.LinkToObject.Default.Card.MediumIconCover(
            id = id,
            indent = MockDataFactory.randomInt(),
            isSelected = false,
            icon = ObjectIcon.None,
            searchFields = listOf(searchField),
            text = "Text1 Text2",
            background = ThemeColor.BLUE,
            isPreviousBlockMedia = false,
            cover = null
        )
    }

    @Test
    fun `test linkToObject block`() {
        linkToObject.bind(item, {})
        assertSearchSpans(linkToObject.title)
    }

    @Test
    fun `test linkToObjectArchive block`() {
        linkToObjectArchived.bind(itemArchived, {})
        assertSearchSpans(linkToObjectArchived.title)
    }

    @Test
    fun `test linkToObjectCard block`() {
        linkToObjectCardSmallIcon.bind(itemCardSmallIcon, {})
        assertSearchSpans(linkToObjectCardSmallIcon.titleView)
    }

    @Test
    fun `test linkToObjectCardCover block`() {
        linkToObjectCardSmallIconCover.bind(itemCardSmallIconCover, {})
        assertSearchSpans(linkToObjectCardSmallIconCover.titleView)
    }

    @Test
    fun `test linkToObjectCardMediumIcon block`() {
        linkToObjectCardMediumIcon.bind(itemCardMediumIcon, {})
        assertSearchSpans(linkToObjectCardMediumIcon.titleView)
    }

    @Test
    fun `test linkToObjectCardMediumIconCover block`() {
        linkToObjectCardMediumIconCover.bind(itemCardMediumIconCover, {})
        assertSearchSpans(linkToObjectCardMediumIconCover.titleView)
    }

    private fun assertSearchSpans(textView: TextView) {
        // verify
        val spans = textView.editableText.getSpans(0, item.text!!.length, SearchHighlightSpan::class.java)
        assertThat(spans.size, equalTo(2))
        assertThat(textView.editableText.getSpanStart(spans[0]), equalTo(0))
        assertThat(textView.editableText.getSpanEnd(spans[0]), equalTo(4))
        assertThat(textView.editableText.getSpanStart(spans[1]), equalTo(6))
        assertThat(textView.editableText.getSpanEnd(spans[1]), equalTo(10))

        val targetSpans = textView.editableText.getSpans(0, textView.text.length, SearchTargetHighlightSpan::class.java)
        assertThat(targetSpans.size, equalTo(1))
        assertThat(textView.editableText.getSpanStart(targetSpans[0]), equalTo(0))
        assertThat(textView.editableText.getSpanEnd(targetSpans[0]), equalTo(4))
    }
}
