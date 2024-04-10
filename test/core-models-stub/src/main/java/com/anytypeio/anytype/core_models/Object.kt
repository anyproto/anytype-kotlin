package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.test_utils.MockDataFactory

fun StubObject(
    id: String = MockDataFactory.randomUuid(),
    space: Id = MockDataFactory.randomUuid(),
    uniqueKey: String? = MockDataFactory.randomUuid(),
    name: String = MockDataFactory.randomString(),
    objectType: String = MockDataFactory.randomString(),
    layout: Double = ObjectType.Layout.BASIC.code.toDouble(),
    smartBlockTypes: List<Double> = emptyList(),
    isDeleted: Boolean? = null,
    isArchived: Boolean? = null,
    description: String? = null,
    iconEmoji: String? = null,
    isReadOnly: Boolean? = null,
    isHidden: Boolean? = null,
    links: List<Id> = emptyList(),
    targetObjectType: Id? = null
): ObjectWrapper.Basic = ObjectWrapper.Basic(
    map = mapOf(
        Relations.ID to id,
        Relations.SPACE_ID to space,
        Relations.NAME to name,
        Relations.TYPE to objectType,
        Relations.LAYOUT to layout,
        Relations.SMARTBLOCKTYPES to smartBlockTypes,
        Relations.IS_ARCHIVED to isArchived,
        Relations.IS_DELETED to isDeleted,
        Relations.DESCRIPTION to description,
        Relations.ICON_EMOJI to iconEmoji,
        Relations.IS_READ_ONLY to isReadOnly,
        Relations.IS_HIDDEN to isHidden,
        Relations.LINKS to links,
        Relations.TARGET_OBJECT_TYPE to targetObjectType,
        Relations.UNIQUE_KEY to uniqueKey,
    )
)

fun StubObjectMinim(
    id: String = MockDataFactory.randomUuid(),
    name: String = MockDataFactory.randomString()
): ObjectWrapper.Basic = ObjectWrapper.Basic(
    map = mapOf(
        Relations.ID to id,
        Relations.NAME to name
    )
)

fun StubObjectView(
    root: Id,
    blocks: List<Block> = emptyList(),
    details: Map<Id, Struct> = emptyMap(),
    relations: List<RelationLink> = emptyList(),
    objectRestrictions: List<ObjectRestriction> = emptyList(),
    dataViewRestrictions: List<DataViewRestrictions> = emptyList()
): ObjectView = ObjectView(
    root = root,
    blocks = blocks,
    details = details,
    relations = relations,
    objectRestrictions = objectRestrictions,
    dataViewRestrictions = dataViewRestrictions
)

fun StubObjectType(
    id: String = MockDataFactory.randomUuid(),
    uniqueKey: String? = MockDataFactory.randomUuid(),
    name: String = MockDataFactory.randomString(),
    objectType: String = MockDataFactory.randomString(),
    layout: Double = ObjectType.Layout.BASIC.code.toDouble(),
    smartBlockTypes: List<Double> = emptyList(),
    isDeleted: Boolean? = null,
    isArchived: Boolean? = null,
    description: String? = null,
    iconEmoji: String? = null,
    isReadOnly: Boolean? = null,
    isHidden: Boolean? = null,
    sourceObject: Id? = null,
    recommendedLayout: Double? = null
): ObjectWrapper.Type = ObjectWrapper.Type(
    map = mapOf(
        Relations.ID to id,
        Relations.NAME to name,
        Relations.TYPE to objectType,
        Relations.LAYOUT to layout,
        Relations.SMARTBLOCKTYPES to smartBlockTypes,
        Relations.IS_ARCHIVED to isArchived,
        Relations.IS_DELETED to isDeleted,
        Relations.DESCRIPTION to description,
        Relations.ICON_EMOJI to iconEmoji,
        Relations.IS_READ_ONLY to isReadOnly,
        Relations.IS_HIDDEN to isHidden,
        Relations.SOURCE_OBJECT to sourceObject,
        Relations.RECOMMENDED_LAYOUT to recommendedLayout,
        Relations.UNIQUE_KEY to uniqueKey,
    )
)

fun StubSpaceView(
    id: String = MockDataFactory.randomUuid(),
    name: String? = MockDataFactory.randomString(),
    iconImage: String? = MockDataFactory.randomString(),
    iconOption: Double? = null,
    targetSpaceId: String? = null,
    spaceAccountStatus: Double? = null,
    spaceAccessType: Double? = null,
    writersLimit: Int? = null,
    readersLimit: Int? = null
): ObjectWrapper.SpaceView = ObjectWrapper.SpaceView(
    map = mapOf(
        Relations.ID to id,
        Relations.NAME to name,
        Relations.ICON_IMAGE to iconImage,
        Relations.ICON_OPTION to iconOption,
        Relations.TARGET_SPACE_ID to targetSpaceId,
        Relations.SPACE_ACCOUNT_STATUS to spaceAccountStatus,
        Relations.SPACE_ACCESS_TYPE to spaceAccessType,
        Relations.WRITERS_LIMIT to writersLimit,
        Relations.READERS_LIMIT to readersLimit
    )
)
