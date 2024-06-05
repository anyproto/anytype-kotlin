package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.test_utils.MockDataFactory

fun StubDataView(
    id: Id = MockDataFactory.randomUuid(),
    views: List<DVViewer> = emptyList(),
    relationLinks: List<RelationLink> = emptyList(),
    targetObjectId: Id = MockDataFactory.randomUuid(),
    isCollection: Boolean = false,
    objectOrder: List<ObjectOrder> = emptyList()
): Block = Block(
    id = id,
    content = DV(
        relationLinks = relationLinks,
        viewers = views,
        targetObjectId = targetObjectId,
        isCollection = isCollection,
        objectOrders = objectOrder
    ),
    children = emptyList(),
    fields = Block.Fields.empty()
)

fun StubDataViewView(
    id: Id = MockDataFactory.randomUuid(),
    filters: List<DVFilter> = emptyList(),
    sorts: List<DVSort> = emptyList(),
    type: DVViewerType = DVViewerType.GRID,
    name: String = MockDataFactory.randomString(),
    viewerRelations: List<DVViewerRelation> = emptyList(),
    cardSize: DVViewerCardSize = DVViewerCardSize.SMALL,
    hideIcon: Boolean = false,
    coverFit: Boolean = false,
    coverRelationKey: String? = null,
    defaultObjectType: Id? = null,
    defaultTemplateId: Id? = null
): DVViewer = DVViewer(
    id = id,
    filters = filters,
    sorts = sorts,
    type = type,
    name = name,
    viewerRelations = viewerRelations,
    cardSize = cardSize,
    hideIcon = hideIcon,
    coverFit = coverFit,
    coverRelationKey = coverRelationKey,
    defaultObjectType = defaultObjectType,
    defaultTemplate = defaultTemplateId
)

fun StubDataViewViewRelation(
    key: Key = MockDataFactory.randomUuid(),
    isVisible: Boolean = MockDataFactory.randomBoolean()
): DVViewerRelation = DVViewerRelation(
    key = key,
    isVisible = isVisible
)

fun StubRelationLink(
    key: Key = MockDataFactory.randomUuid(),
    format: RelationFormat = RelationFormat.LONG_TEXT,
): RelationLink = RelationLink(
    key = key,
    format = format
)

fun StubSort(
    id: Id = MockDataFactory.randomUuid(),
    relationKey: Key = MockDataFactory.randomUuid(),
    type: DVSortType = DVSortType.ASC
): DVSort = DVSort(
    id = id,
    relationKey = relationKey,
    type = type,
    relationFormat = RelationFormat.LONG_TEXT
)

fun StubFilter(
    id: Id = MockDataFactory.randomUuid(),
    relationKey: Key = MockDataFactory.randomUuid(),
    relationFormat: RelationFormat = RelationFormat.LONG_TEXT,
    operator: DVFilterOperator = DVFilterOperator.AND,
    condition: DVFilterCondition = DVFilterCondition.EQUAL,
    quickOption: DVFilterQuickOption = DVFilterQuickOption.EXACT_DATE,
    value: Any? = null
): DVFilter = DVFilter(
    id = id,
    relation = relationKey,
    relationFormat = relationFormat,
    operator = operator,
    condition = condition,
    quickOption = quickOption,
    value = value
)

fun StubSpaceView(
    id: Id = MockDataFactory.randomUuid(),
    targetSpaceId: Id = MockDataFactory.randomUuid(),
    spaceAccessType: SpaceAccessType = SpaceAccessType.DEFAULT,
    sharedSpaceLimit: Int? = null

) = ObjectWrapper.SpaceView(
    map = mapOf(
        Relations.ID to id,
        Relations.TARGET_SPACE_ID to targetSpaceId,
        Relations.SPACE_ACCESS_TYPE to spaceAccessType.code.toDouble(),
        Relations.SHARED_SPACES_LIMIT to sharedSpaceLimit?.toDouble()
    )
)