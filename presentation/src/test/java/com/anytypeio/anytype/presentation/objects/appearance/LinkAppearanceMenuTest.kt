package com.anytypeio.anytype.presentation.objects.appearance

import com.anytypeio.anytype.core_models.Block.Content.Link
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubLinkContent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.MenuItem
import org.junit.Test
import kotlin.test.assertEquals

class LinkAppearanceMenuTest {

    private val defaultLinkAppearance = StubLinkContent(
        iconSize = Link.IconSize.SMALL,
        cardStyle = Link.CardStyle.TEXT,
        description = Link.Description.NONE,
        relations = setOf(Link.Relation.NAME)
    )

    @Test
    fun `default`() {
        val factory = LinkAppearanceFactory(
            content = defaultLinkAppearance,
            layout = ObjectType.Layout.BASIC
        )

        val actual = factory.createAppearanceMenuItems()
        val expected = BlockView.Appearance.Menu(
            preview = MenuItem.PreviewLayout.TEXT,
            icon = MenuItem.Icon.SMALL,
            cover = null,
            description = null
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `when is todo layout - no icon`() {
        val factory = LinkAppearanceFactory(
            content = defaultLinkAppearance,
            layout = ObjectType.Layout.TODO
        )

        val actual = factory.createAppearanceMenuItems()
        val expected = BlockView.Appearance.Menu(
            preview = MenuItem.PreviewLayout.TEXT,
            icon = null,
            cover = null,
            description = null
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `when is todo note - no icon and no description`() {
        val factory = LinkAppearanceFactory(
            content = defaultLinkAppearance.copy(
                description = Link.Description.ADDED
            ),
            layout = ObjectType.Layout.NOTE
        )

        val actual = factory.createAppearanceMenuItems()
        val expected = BlockView.Appearance.Menu(
            preview = MenuItem.PreviewLayout.TEXT,
            icon = null,
            cover = null,
            description = null
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `when card style is card - has description`() {
        val factory = LinkAppearanceFactory(
            content = defaultLinkAppearance.copy(
                description = Link.Description.ADDED,
                cardStyle = Link.CardStyle.CARD,
            ),
            layout = ObjectType.Layout.BASIC
        )

        val actual = factory.createAppearanceMenuItems()
        val expected = BlockView.Appearance.Menu(
            preview = MenuItem.PreviewLayout.CARD,
            icon = MenuItem.Icon.SMALL,
            cover = null,
            description = MenuItem.Description.WITH
        )
        assertEquals(expected, actual)
    }

}