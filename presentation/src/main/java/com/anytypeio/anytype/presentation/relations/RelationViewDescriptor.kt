package com.anytypeio.anytype.presentation.relations

interface RelationViewDescriptor {
    val key: String
    val name: String
    val isReadOnly: Boolean
    val isMulti: Boolean
}