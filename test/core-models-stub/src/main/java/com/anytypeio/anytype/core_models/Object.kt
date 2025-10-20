package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
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
    targetObjectType: Id? = null,
    identity: Id? = null,
    fileExt: String? = null,
    extraFields: Map<String, Any> = emptyMap()
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
        Relations.IDENTITY to identity,
        Relations.FILE_EXT to fileExt
    ) + extraFields
)

fun StubSpaceMember(
    id: String = MockDataFactory.randomUuid(),
    space: Id = MockDataFactory.randomUuid(),
    name: String = MockDataFactory.randomString(),
    iconEmoji: String? = null,
    identity: Id? = null,
    memberPermissions: List<SpaceMemberPermissions> = emptyList(),
    memberStatus: ParticipantStatus = ParticipantStatus.ACTIVE
    ): ObjectWrapper.Basic = ObjectWrapper.Basic(
    map = mapOf(
        Relations.ID to id,
        Relations.SPACE_ID to space,
        Relations.NAME to name,
        Relations.ICON_EMOJI to iconEmoji,
        Relations.IDENTITY to identity,
        Relations.PARTICIPANT_PERMISSIONS to memberPermissions,
        Relations.PARTICIPANT_STATUS to memberStatus.code.toDouble()
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
    objectRestrictions: List<ObjectRestriction> = emptyList(),
    dataViewRestrictions: List<DataViewRestrictions> = emptyList()
): ObjectView = ObjectView(
    root = root,
    blocks = blocks,
    details = details,
    objectRestrictions = objectRestrictions,
    dataViewRestrictions = dataViewRestrictions
)

fun StubObjectType(
    id: String = MockDataFactory.randomUuid(),
    uniqueKey: String? = MockDataFactory.randomUuid(),
    name: String = MockDataFactory.randomString(),
    objectType: String = MockDataFactory.randomString(),
    layout: Double = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
    smartBlockTypes: List<Double> = emptyList(),
    isDeleted: Boolean? = null,
    isArchived: Boolean? = null,
    description: String? = null,
    iconEmoji: String? = null,
    isReadOnly: Boolean? = null,
    isHidden: Boolean? = null,
    sourceObject: Id? = null,
    recommendedLayout: Double? = null,
    recommendedRelations: List<String> = emptyList(),
    recommendedHiddenRelations: List<String> = emptyList(),
    recommendedFeaturedRelations: List<String> = emptyList(),
    recommendedFileRelations: List<String> = emptyList(),
    space: Id? = null,
    orderId: String? = null
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
        Relations.RECOMMENDED_RELATIONS to recommendedRelations,
        Relations.RECOMMENDED_HIDDEN_RELATIONS to recommendedHiddenRelations,
        Relations.RECOMMENDED_FEATURED_RELATIONS to recommendedFeaturedRelations,
        Relations.RECOMMENDED_FILE_RELATIONS to recommendedFileRelations,
        Relations.SPACE_ID to space,
        Relations.ORDER_ID to orderId
    )
)