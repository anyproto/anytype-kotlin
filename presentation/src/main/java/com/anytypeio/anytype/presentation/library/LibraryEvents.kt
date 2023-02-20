package com.anytypeio.anytype.presentation.library

sealed class LibraryEvent {

    sealed class Query(open val query: String) : LibraryEvent() {
        class MyTypes(override val query: String) : Query(query)
        class LibraryTypes(override val query: String) : Query(query)
        class MyRelations(override val query: String) : Query(query)
        class LibraryRelations(override val query: String) : Query(query)
    }

    sealed class ToggleInstall(open val item: LibraryView) : LibraryEvent() {
        class Type(override val item: LibraryView) : ToggleInstall(item)
        class Relation(override val item: LibraryView) : ToggleInstall(item)
    }

    sealed class Type: LibraryEvent() {
        class Create(val name: String = "") : Type()
        class Edit(val item: LibraryView.MyTypeView) : Type()
    }

    sealed class Relation: LibraryEvent() {
        class Create(val name: String = "") : Relation()
        class Edit(val item: LibraryView.MyRelationView) : Relation()
    }

}