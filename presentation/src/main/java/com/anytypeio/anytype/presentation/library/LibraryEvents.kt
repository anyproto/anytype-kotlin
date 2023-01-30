package com.anytypeio.anytype.presentation.library

sealed class LibraryEvent {

    sealed class Query(open val query: String) : LibraryEvent() {
        class MyTypes(override val query: String) : Query(query)
        class LibraryTypes(override val query: String) : Query(query)
        class MyRelations(override val query: String) : Query(query)
        class LibraryRelations(override val query: String) : Query(query)
    }

}