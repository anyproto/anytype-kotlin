package com.anytypeio.anytype.presentation.objects.appearance

import com.anytypeio.anytype.core_models.Block.Content.Link
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubLinkContent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.MenuItem
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseIconViewModel
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView
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
            description = MenuItem.Description.NONE,
            objectType = MenuItem.ObjectType.WITHOUT,
            iconMenus = listOf(
                ObjectAppearanceChooseSettingsView.Icon.None(false),
                ObjectAppearanceChooseSettingsView.Icon.Small(true)
            )
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
            description = MenuItem.Description.NONE,
            objectType = MenuItem.ObjectType.WITHOUT,
            iconMenus = listOf()
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `when is todo layout and card style without cover - no icon`() {
        val factory = LinkAppearanceFactory(
            content = StubLinkContent(
                iconSize = Link.IconSize.SMALL,
                cardStyle = Link.CardStyle.CARD,
                description = Link.Description.NONE,
                relations = setOf(Link.Relation.NAME)
            ),
            layout = ObjectType.Layout.TODO
        )

        val actual = factory.createAppearanceMenuItems()
        val expected = BlockView.Appearance.Menu(
            preview = MenuItem.PreviewLayout.CARD,
            icon = null,
            cover = MenuItem.Cover.WITHOUT,
            description = MenuItem.Description.NONE,
            objectType = MenuItem.ObjectType.WITHOUT,
            iconMenus = listOf()
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `when is todo layout and card style with cover - no icon`() {
        val factory = LinkAppearanceFactory(
            content = StubLinkContent(
                iconSize = Link.IconSize.SMALL,
                cardStyle = Link.CardStyle.CARD,
                description = Link.Description.NONE,
                relations = setOf(Link.Relation.NAME, Link.Relation.COVER)
            ),
            layout = ObjectType.Layout.TODO
        )

        val actual = factory.createAppearanceMenuItems()
        val expected = BlockView.Appearance.Menu(
            preview = MenuItem.PreviewLayout.CARD,
            icon = null,
            cover = MenuItem.Cover.WITH,
            description = MenuItem.Description.NONE,
            objectType = MenuItem.ObjectType.WITHOUT,
            iconMenus = listOf()
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `when is note - no icon and no description`() {
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
            description = null,
            objectType = MenuItem.ObjectType.WITHOUT,
            iconMenus = listOf()
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
            cover = MenuItem.Cover.WITHOUT,
            description = MenuItem.Description.ADDED,
            objectType = MenuItem.ObjectType.WITHOUT,
            iconMenus = listOf(
                ObjectAppearanceChooseSettingsView.Icon.None(false),
                ObjectAppearanceChooseSettingsView.Icon.Small(true),
                ObjectAppearanceChooseSettingsView.Icon.Medium(false)
            )
        )
        assertEquals(expected, actual)
    }

}