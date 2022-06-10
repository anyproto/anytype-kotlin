package com.anytypeio.anytype.presentation

import com.anytypeio.anytype.core_models.Block.Content.Link
import com.anytypeio.anytype.test_utils.MockDataFactory

object MockBlockContentFactory {

    fun StubLinkContent(
        target: String = MockDataFactory.randomUuid(),
        type: Link.Type = Link.Type.PAGE,
        iconSize: Link.IconSize = Link.IconSize.SMALL,
        cardStyle: Link.CardStyle = Link.CardStyle.TEXT,
        description: Link.Description = Link.Description.NONE,
        relations: Set<Link.Relation> = emptySet(),
    ): Link = Link(
        target = target,
        type = type,
        iconSize = iconSize,
        cardStyle = cardStyle,
        description = description,
        relations = relations
    )

}