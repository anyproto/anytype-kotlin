package com.anytypeio.anytype.presentation

import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.test_utils.MockDataFactory

object MockRelationFactory {

    //ID
    val relationId = Relation(
        key = "id",
        name = "Anytype ID",
        format = Relation.Format.OBJECT,
        source = Relation.Source.DERIVED,
        isHidden = true,
        isReadOnly = false,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf(),
        defaultValue = null
    )

    //OBJECT TYPE
    val relationObjType = Relation(
        key = "type",
        name = "Object type",
        format = Relation.Format.OBJECT,
        source = Relation.Source.DERIVED,
        isHidden = true,
        isReadOnly = true,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf("_otobjectType"),
        defaultValue = null
    )

    //LAYOUT
    val relationLayout = Relation(
        key = "layout",
        name = "Layout",
        format = Relation.Format.NUMBER,
        source = Relation.Source.DETAILS,
        isHidden = true,
        isReadOnly = false,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf(),
        defaultValue = null
    )


    //LAST MODIFIED DATE
    val relationLastModifiedDate = Relation(
        key = "lastModifiedDate",
        name = "Last modified date",
        format = Relation.Format.DATE,
        source = Relation.Source.DERIVED,
        isHidden = false,
        isReadOnly = true,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf(),
        defaultValue = null
    )

    //FEATURED RELATIONS
    val relationFeaturedRelations = Relation(
        key = "featuredRelations",
        name = "Featured Relations",
        format = Relation.Format.OBJECT,
        source = Relation.Source.DETAILS,
        isHidden = true,
        isReadOnly = false,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf("_otrelation"),
        defaultValue = null
    )

    //SNIPPET
    val relationSnippet = Relation(
        key = "snippet",
        name = "",
        format = Relation.Format.LONG_TEXT,
        source = Relation.Source.DERIVED,
        isHidden = true,
        isReadOnly = true,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf(),
        defaultValue = null
    )

    //NAME
    val relationName = Relation(
        key = "name",
        name = "Name",
        format = Relation.Format.SHORT_TEXT,
        source = Relation.Source.DETAILS,
        isHidden = true,
        isReadOnly = false,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf(),
        defaultValue = null
    )

    //DONE
    val relationDone = Relation(
        key = "done",
        name = "Done",
        format = Relation.Format.CHECKBOX,
        source = Relation.Source.DETAILS,
        isHidden = true,
        isReadOnly = false,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf(),
        defaultValue = null
    )

    //DESCRIPTION
    val relationDescription = Relation(
        key = "description",
        name = "Description",
        format = Relation.Format.LONG_TEXT,
        source = Relation.Source.DETAILS,
        isHidden = false,
        isReadOnly = false,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf(),
        defaultValue = null
    )

    //ICON EMOJI
    val relationIconEmoji = Relation(
        key = "iconEmoji",
        name = "Emoji",
        format = Relation.Format.EMOJI,
        source = Relation.Source.DETAILS,
        isHidden = true,
        isReadOnly = false,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf(),
        defaultValue = null
    )

    //ICON IMAGE
    val relationIconImage = Relation(
        key = "iconImage",
        name = "Image",
        format = Relation.Format.FILE,
        source = Relation.Source.DETAILS,
        isHidden = true,
        isReadOnly = false,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf(),
        defaultValue = null
    )

    //SET OF
    val relationSetOf = Relation(
        key = "setOf",
        name = "Set of",
        format = Relation.Format.OBJECT,
        source = Relation.Source.DETAILS,
        isHidden = false,
        isReadOnly = true,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf(),
        defaultValue = null
    )

    //COVER_X, SAME COVER_Y, COVER_TYPE, COVER_SCALE,
    val relationCoverX = Relation(
        key = "coverX",
        name = "Cover x offset",
        format = Relation.Format.NUMBER,
        source = Relation.Source.DETAILS,
        isHidden = true,
        isReadOnly = false,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf(),
        defaultValue = null
    )

    //LAST OPENED DATE
    val relationLastOpenedDate = Relation(
        key = "lastOpenedDate",
        name = "Last opened date",
        format = Relation.Format.DATE,
        source = Relation.Source.ACCOUNT,
        isHidden = false,
        isReadOnly = true,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf(),
        defaultValue = null
    )

    //IS HIDDEN
    val relationIsHidden = Relation(
        key = "isHidden",
        name = "Hidden",
        format = Relation.Format.CHECKBOX,
        source = Relation.Source.DETAILS,
        isHidden = true,
        isReadOnly = false,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf(),
        defaultValue = null
    )

    //IS ARCHIVED
    val relationIsArchived = Relation(
        key = "isArchived",
        name = "Archived",
        format = Relation.Format.CHECKBOX,
        source = Relation.Source.ACCOUNT,
        isHidden = true,
        isReadOnly = false,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf(),
        defaultValue = null
    )

    //IS FAVORITE
    val relationIsFavorite = Relation(
        key = "isFavorite",
        name = "Favorited",
        format = Relation.Format.CHECKBOX,
        source = Relation.Source.ACCOUNT,
        isHidden = true,
        isReadOnly = true,
        isMulti = false,
        selections = emptyList(),
        objectTypes = listOf(),
        defaultValue = null
    )

    val optionTag1 = Relation.Option(
        id = MockDataFactory.randomUuid(),
        text = MockDataFactory.randomString(),
        color = "purple",
        scope = Relation.OptionScope.RELATION
    )

    val optionTag2 = Relation.Option(
        id = MockDataFactory.randomUuid(),
        text = MockDataFactory.randomString(),
        color = "ice",
        scope = Relation.OptionScope.RELATION
    )

    val optionTag3 = Relation.Option(
        id = MockDataFactory.randomUuid(),
        text = MockDataFactory.randomString(),
        color = "red",
        scope = Relation.OptionScope.RELATION
    )

    //TAG
    val relationTag = Relation(
        key = "tag",
        name = "Tag",
        format = Relation.Format.TAG,
        source = Relation.Source.DETAILS,
        isHidden = false,
        isReadOnly = false,
        isMulti = false,
        selections = listOf(optionTag1, optionTag2, optionTag3),
        objectTypes = listOf(),
        defaultValue = null
    )

    val optionStatus = Relation.Option(
        id = MockDataFactory.randomUuid(),
        text = MockDataFactory.randomString(),
        color = "blue",
        scope = Relation.OptionScope.LOCAL
    )

    //STATUS
    val relationStatus = Relation(
        key = "status",
        name = "Status",
        format = Relation.Format.STATUS,
        source = Relation.Source.DETAILS,
        isHidden = false,
        isReadOnly = false,
        isMulti = false,
        selections = listOf(optionStatus),
        objectTypes = listOf(),
        defaultValue = null
    )

    //CUSTOM TEXT RELATION
    val relationCustomText = Relation(
        key = MockDataFactory.randomUuid(),
        name = MockDataFactory.randomString(),
        format = Relation.Format.LONG_TEXT,
        source = Relation.Source.DETAILS,
        isHidden = false,
        isReadOnly = false,
        isMulti = false,
        selections = listOf(),
        objectTypes = listOf(),
        defaultValue = null
    )

    //CUSTOM NUMBER RELATION
    val relationCustomNumber = Relation(
        key = MockDataFactory.randomUuid(),
        name = MockDataFactory.randomString(),
        format = Relation.Format.NUMBER,
        source = Relation.Source.DETAILS,
        isHidden = false,
        isReadOnly = false,
        isMulti = false,
        selections = listOf(),
        objectTypes = listOf(),
        defaultValue = null
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