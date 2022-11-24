package com.anytypeio.anytype.presentation

import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectTypeIds.OBJECT_TYPE
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.test_utils.MockDataFactory

object MockRelationFactory {

    //ID
    val relationId = StubRelationObject(
        key = "id",
        name = "Anytype ID",
        format = Relation.Format.OBJECT,
        isHidden = true,
        isReadOnly = false,
        objectTypes = listOf(),
    )

    //OBJECT TYPE
    val relationObjType = StubRelationObject(
        key = "type",
        name = "Object type",
        format = Relation.Format.OBJECT,
        isHidden = true,
        isReadOnly = true,
        objectTypes = listOf(OBJECT_TYPE)
    )

    //LAYOUT
    val relationLayout = StubRelationObject(
        key = "layout",
        name = "Layout",
        format = Relation.Format.NUMBER,
        isHidden = true,
        isReadOnly = false,
        objectTypes = listOf()
    )


    //LAST MODIFIED DATE
    val relationLastModifiedDate = StubRelationObject(
        key = "lastModifiedDate",
        name = "Last modified date",
        format = Relation.Format.DATE,
        isHidden = false,
        isReadOnly = true,
        objectTypes = listOf()
    )

    //FEATURED RELATIONS
    val relationFeaturedRelations = StubRelationObject(
        key = "featuredRelations",
        name = "Featured Relations",
        format = Relation.Format.OBJECT,
        isHidden = true,
        isReadOnly = false,
        objectTypes = listOf(ObjectTypeIds.RELATION)
    )

    //SNIPPET
    val relationSnippet = StubRelationObject(
        key = "snippet",
        name = "",
        format = Relation.Format.LONG_TEXT,
        isHidden = true,
        isReadOnly = true,
        objectTypes = listOf()
    )

    //NAME
    val relationName = StubRelationObject(
        key = "name",
        name = "Name",
        format = Relation.Format.SHORT_TEXT,
        isHidden = true,
        isReadOnly = false,
        objectTypes = listOf()
    )

    //DONE
    val relationDone = StubRelationObject(
        key = "done",
        name = "Done",
        format = Relation.Format.CHECKBOX,
        isHidden = true,
        isReadOnly = false,
        objectTypes = listOf()
    )

    //DESCRIPTION
    val relationDescription = StubRelationObject(
        key = "description",
        name = "Description",
        format = Relation.Format.LONG_TEXT,
        isHidden = false,
        isReadOnly = false,
        objectTypes = listOf(),
    )

    //ICON EMOJI
    val relationIconEmoji = StubRelationObject(
        key = "iconEmoji",
        name = "Emoji",
        format = Relation.Format.EMOJI,
        isHidden = true,
        isReadOnly = false,
        objectTypes = listOf()
    )

    //ICON IMAGE
    val relationIconImage = StubRelationObject(
        key = "iconImage",
        name = "Image",
        format = Relation.Format.FILE,
        isHidden = true,
        isReadOnly = false,
        objectTypes = listOf()
    )

    //SET OF
    val relationSetOf = StubRelationObject(
        key = "setOf",
        name = "Set of",
        format = Relation.Format.OBJECT,
        isHidden = false,
        isReadOnly = true,
        objectTypes = listOf()
    )

    //COVER_X, SAME COVER_Y, COVER_TYPE, COVER_SCALE,
    val relationCoverX = StubRelationObject(
        key = "coverX",
        name = "Cover x offset",
        format = Relation.Format.NUMBER,
        isHidden = true,
        isReadOnly = false,
        objectTypes = listOf(),
    )

    //LAST OPENED DATE
    val relationLastOpenedDate = StubRelationObject(
        key = "lastOpenedDate",
        name = "Last opened date",
        format = Relation.Format.DATE,
        isHidden = false,
        isReadOnly = true,
        objectTypes = listOf(),
    )

    //IS HIDDEN
    val relationIsHidden = StubRelationObject(
        key = "isHidden",
        name = "Hidden",
        format = Relation.Format.CHECKBOX,
        isHidden = true,
        isReadOnly = false,
        objectTypes = listOf(),
    )

    //IS ARCHIVED
    val relationIsArchived = StubRelationObject(
        key = "isArchived",
        name = "Archived",
        format = Relation.Format.CHECKBOX,
        isHidden = true,
        isReadOnly = false,
        objectTypes = listOf(),
    )

    //IS FAVORITE
    val relationIsFavorite = StubRelationObject(
        key = "isFavorite",
        name = "Favorited",
        format = Relation.Format.CHECKBOX,
        isHidden = true,
        isReadOnly = true,
        objectTypes = listOf(),
    )

    val optionTag1 = Relation.Option(
        id = MockDataFactory.randomUuid(),
        text = MockDataFactory.randomString(),
        color = "purple"
    )

    val optionTag2 = Relation.Option(
        id = MockDataFactory.randomUuid(),
        text = MockDataFactory.randomString(),
        color = "ice"
    )

    val optionTag3 = Relation.Option(
        id = MockDataFactory.randomUuid(),
        text = MockDataFactory.randomString(),
        color = "red"
    )

    //TAG
    val relationTag = StubRelationObject(
        key = "tag",
        name = "Tag",
        format = Relation.Format.TAG,
        isHidden = false,
        isReadOnly = false,
        relationOptionsDict = listOf(
            optionTag1.id, optionTag2.id, optionTag3.id
        ),
        objectTypes = listOf(),
    )

    val optionStatus = Relation.Option(
        id = MockDataFactory.randomUuid(),
        text = MockDataFactory.randomString(),
        color = "blue"
    )

    //STATUS
    val relationStatus = StubRelationObject(
        key = "status",
        name = "Status",
        format = Relation.Format.STATUS,
        isHidden = false,
        isReadOnly = false,
        relationOptionsDict = listOf(optionStatus.id),
        objectTypes = listOf(),
    )

    //CUSTOM TEXT RELATION
    val relationCustomText = StubRelationObject(
        key = MockDataFactory.randomUuid(),
        name = MockDataFactory.randomString(),
        format = Relation.Format.LONG_TEXT,
        isHidden = false,
        isReadOnly = false,
        objectTypes = listOf(),
    )

    //CUSTOM NUMBER RELATION
    val relationCustomNumber = StubRelationObject(
        key = MockDataFactory.randomUuid(),
        name = MockDataFactory.randomString(),
        format = Relation.Format.NUMBER,
        isHidden = false,
        isReadOnly = false,
        objectTypes = listOf(),
    )

    fun getAllRelations() = listOf(
        relationId, relationCoverX, relationCustomNumber, relationCustomText,
        relationDescription, relationDone, relationFeaturedRelations, relationIconEmoji,
        relationIconImage, relationIsArchived, relationIsFavorite, relationIsHidden,
        relationLastModifiedDate, relationLastOpenedDate, relationLayout,
        relationName, relationObjType, relationSetOf, relationSnippet,
        relationStatus, relationTag
    )
}