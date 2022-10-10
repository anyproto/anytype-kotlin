package com.anytypeio.anytype.presentation.objects.appearance

import com.anytypeio.anytype.core_models.Block.Content.Link
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubLinkContent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.InEditor.Description
import org.junit.Test
import kotlin.test.assertEquals

class LinkAppearanceInEditorTest {

    private val defaultLinkAppearance = StubLinkContent(
        iconSize = Link.IconSize.NONE,
        cardStyle = Link.CardStyle.TEXT,
        description = Link.Description.NONE,
        relations = emptySet()
    )

    @Test
    fun `should create appearance params with default values`() {

        val factory = LinkAppearanceFactory(
            content = defaultLinkAppearance,
            layout = ObjectType.Layout.BASIC
        )

        val actual = factory.createInEditorLinkAppearance()
        val expected = BlockView.Appearance.InEditor(
            isCard = false,
            showIcon = false,
            description = Description.NONE,
            showCover = false,
            showType = false,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `when layout is null - create appearance params with default values`() {
        val factory = LinkAppearanceFactory(
            content = defaultLinkAppearance,
            layout = null
        )

        val actual = factory.createInEditorLinkAppearance()
        val expected = BlockView.Appearance.InEditor(
            isCard = false,
            showIcon = false,
            description = Description.NONE,
            showCover = false,
            showType = false,
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `should create appearance params without cover`() {
        val factory = LinkAppearanceFactory(
            content = defaultLinkAppearance.copy(
                relations = defaultLinkAppearance.relations + Link.Relation.COVER
            ),
            layout = ObjectType.Layout.BASIC
        )

        val actual = factory.createInEditorLinkAppearance()

        val expected = BlockView.Appearance.InEditor(
            isCard = false,
            showIcon = false,
            description = Description.NONE,
            showCover = false,
            showType = false,
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `when layout is todo`() {
        val factory = LinkAppearanceFactory(
            content = defaultLinkAppearance,
            layout = ObjectType.Layout.TODO
        )

        val actual = factory.createInEditorLinkAppearance()

        val expected = BlockView.Appearance.InEditor(
            isCard = false,
            showIcon = true,
            description = Description.NONE,
            showCover = false,
            showType = false,
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `when layout is note`() {
        val factory = LinkAppearanceFactory(
            content = defaultLinkAppearance.copy(
                iconSize = Link.IconSize.SMALL
            ),
            layout = ObjectType.Layout.NOTE
        )

        val actual = factory.createInEditorLinkAppearance()

        val expected = BlockView.Appearance.InEditor(
            isCard = false,
            showIcon = false,
            description = Description.NONE,
            showCover = false,
            showType = false,
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `when iconSize is none - no icon`() {
        val factory = LinkAppearanceFactory(
            content = defaultLinkAppearance.copy(
                iconSize = Link.IconSize.NONE
            ),
            layout = ObjectType.Layout.BASIC
        )

        val actual = factory.createInEditorLinkAppearance()

        val expected = BlockView.Appearance.InEditor(
            isCard = false,
            showIcon = false,
            description = Description.NONE,
            showCover = false,
            showType = false,
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `when card style text - relatoin description`() {
        val factory = LinkAppearanceFactory(
            content = defaultLinkAppearance.copy(
                cardStyle = Link.CardStyle.TEXT,
                description = Link.Description.ADDED
            ),
            layout = ObjectType.Layout.BASIC
        )

        val actual = factory.createInEditorLinkAppearance()

        val expected = BlockView.Appearance.InEditor(
            isCard = false,
            showIcon = false,
            description = Description.RELATION,
            showCover = false,
            showType = false,
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `when card style text - snippet description`() {
        val factory = LinkAppearanceFactory(
            content = defaultLinkAppearance.copy(
                cardStyle = Link.CardStyle.TEXT,
                description = Link.Description.CONTENT
            ),
            layout = ObjectType.Layout.BASIC
        )

        val actual = factory.createInEditorLinkAppearance()

        val expected = BlockView.Appearance.InEditor(
            isCard = false,
            showIcon = false,
            description = Description.SNIPPET,
            showCover = false,
            showType = false,
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `when card style card - there is description`() {
        val factory = LinkAppearanceFactory(
            content = defaultLinkAppearance.copy(
                cardStyle = Link.CardStyle.CARD,
                description = Link.Description.ADDED
            ),
            layout = ObjectType.Layout.BASIC
        )

        val actual = factory.createInEditorLinkAppearance()

        val expected = BlockView.Appearance.InEditor(
            isCard = true,
            showIcon = false,
            description = Description.RELATION,
            showCover = false,
            showType = false,
        )

        assertEquals(expected, actual)
    }
}