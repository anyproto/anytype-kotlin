package com.anytypeio.anytype.presentation.home

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.StubLinkToObjectBlock
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.widgets.parseWidgets
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class ParseWidgetTest {


    var urlBuilder: UrlBuilder = mock()

    @Test
    fun `should hide widgets with archived source`() {

        val invalidSource = StubObject(
            isArchived = true
        )

        val validSource = StubObject(
            isArchived = false,
            isDeleted = false
        )

        val widgetContent = Block.Content.Widget(
            layout = listOf(
                Block.Content.Widget.Layout.TREE,
                Block.Content.Widget.Layout.LIST,
                Block.Content.Widget.Layout.LINK,
                Block.Content.Widget.Layout.COMPACT_LIST
            ).random()
        )

        val firstWidgetLink = StubLinkToObjectBlock(
            target = invalidSource.id
        )

        val secondWidgetLink = StubLinkToObjectBlock(
            target = validSource.id
        )

        val firstWidgetBlock = Block(
            id = MockDataFactory.randomUuid(),
            content = widgetContent,
            children = listOf(firstWidgetLink.id),
            fields = Block.Fields.empty()
        )

        val secondWidgetBlock = Block(
            id = MockDataFactory.randomUuid(),
            content = widgetContent,
            children = listOf(secondWidgetLink.id),
            fields = Block.Fields.empty()
        )

        val smartBlock = StubSmartBlock(
            id = HomeScreenViewModelTest.WIDGET_OBJECT_ID,
            children = listOf(firstWidgetBlock.id, secondWidgetBlock.id)
        )

        val blocks = listOf(
            smartBlock,
            firstWidgetBlock,
            firstWidgetLink,
            secondWidgetBlock,
            secondWidgetLink
        )

        val result = blocks.parseWidgets(
            root = smartBlock.id,
            details = buildMap {
                put(invalidSource.id, invalidSource.map)
                put(validSource.id, validSource.map)
            },
            config = StubConfig(),
            urlBuilder
        )

        assertTrue {
            result.size == 1
        }

        assertTrue {
            result.first().id == secondWidgetBlock.id
        }
    }

    @Test
    fun `should hide widgets with deleted source`() {

        val invalidSource = StubObject(
            isDeleted = true
        )

        val validSource = StubObject(
            isArchived = false,
            isDeleted = false
        )

        val widgetContent = Block.Content.Widget(
            layout = listOf(
                Block.Content.Widget.Layout.TREE,
                Block.Content.Widget.Layout.LIST,
                Block.Content.Widget.Layout.LINK,
                Block.Content.Widget.Layout.COMPACT_LIST
            ).random()
        )

        val firstWidgetLink = StubLinkToObjectBlock(
            target = invalidSource.id
        )

        val secondWidgetLink = StubLinkToObjectBlock(
            target = validSource.id
        )

        val firstWidgetBlock = Block(
            id = MockDataFactory.randomUuid(),
            content = widgetContent,
            children = listOf(firstWidgetLink.id),
            fields = Block.Fields.empty()
        )

        val secondWidgetBlock = Block(
            id = MockDataFactory.randomUuid(),
            content = widgetContent,
            children = listOf(secondWidgetLink.id),
            fields = Block.Fields.empty()
        )

        val smartBlock = StubSmartBlock(
            id = HomeScreenViewModelTest.WIDGET_OBJECT_ID,
            children = listOf(firstWidgetBlock.id, secondWidgetBlock.id)
        )

        val blocks = listOf(
            smartBlock,
            firstWidgetBlock,
            firstWidgetLink,
            secondWidgetBlock,
            secondWidgetLink
        )

        val result = blocks.parseWidgets(
            root = smartBlock.id,
            details = buildMap {
                put(invalidSource.id, invalidSource.map)
                put(validSource.id, validSource.map)
            },
            config = StubConfig(),
            urlBuilder
        )

        assertTrue {
            result.size == 1
        }

        assertTrue {
            result.first().id == secondWidgetBlock.id
        }
    }
    @Test
    fun `should hide widgets with invalid source because of empty details`() {

        val invalidSource = StubObject()

        val validSource = StubObject(
            isArchived = false,
            isDeleted = false
        )

        val widgetContent = Block.Content.Widget(
            layout = listOf(
                Block.Content.Widget.Layout.TREE,
                Block.Content.Widget.Layout.LIST,
                Block.Content.Widget.Layout.LINK,
                Block.Content.Widget.Layout.COMPACT_LIST
            ).random()
        )

        val firstWidgetLink = StubLinkToObjectBlock(
            target = invalidSource.id
        )

        val secondWidgetLink = StubLinkToObjectBlock(
            target = validSource.id
        )

        val firstWidgetBlock = Block(
            id = MockDataFactory.randomUuid(),
            content = widgetContent,
            children = listOf(firstWidgetLink.id),
            fields = Block.Fields.empty()
        )

        val secondWidgetBlock = Block(
            id = MockDataFactory.randomUuid(),
            content = widgetContent,
            children = listOf(secondWidgetLink.id),
            fields = Block.Fields.empty()
        )

        val smartBlock = StubSmartBlock(
            id = HomeScreenViewModelTest.WIDGET_OBJECT_ID,
            children = listOf(firstWidgetBlock.id, secondWidgetBlock.id)
        )

        val blocks = listOf(
            smartBlock,
            firstWidgetBlock,
            firstWidgetLink,
            secondWidgetBlock,
            secondWidgetLink
        )

        val result = blocks.parseWidgets(
            root = smartBlock.id,
            details = buildMap {
                put(invalidSource.id, emptyMap())
                put(validSource.id, validSource.map)
            },
            config = StubConfig(),
            urlBuilder
        )

        assertTrue {
            result.size == 1
        }

        assertTrue {
            result.first().id == secondWidgetBlock.id
        }
    }
}