package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.test_utils.MockDataFactory

fun StubDataView(
    id : Id = MockDataFactory.randomUuid(),
    views: List<DVViewer> = emptyList(),
    relations: List<RelationLink> = emptyList(),
    sources: List<Id> = emptyList()
) : Block = Block(
    id = id,
    content = DV(
        sources = sources,
        relations = emptyList(),
        relationsIndex=  relations,
        viewers = views
    ),
    children = emptyList(),
    fields = Block.Fields.empty()
)

fun StubDataViewView(
    id : Id = MockDataFactory.randomUuid(),
    filters: List<DVFilter> = emptyList(),
    sorts: List<DVSort> = emptyList(),
    type: DVViewerType = DVViewerType.GRID,
    name: String = MockDataFactory.randomString(),
    viewerRelations: List<DVViewerRelation> = emptyList()
) : DVViewer = DVViewer(
    id = id,
    filters = filters,
    sorts = sorts,
    type = type,
    name = name,
    viewerRelations = viewerRelations
)

fun StubDataViewViewRelation(
    key: Key = MockDataFactory.randomUuid(),
    isVisible: Boolean = MockDataFactory.randomBoolean()
) : DVViewerRelation = DVViewerRelation(
    key = key,
    isVisible = isVisible
)