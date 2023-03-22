package com.anytypeio.anytype.presentation.library

sealed class LibraryEvent {

    sealed class BottomMenu : LibraryEvent() {
        class Back : BottomMenu()
        class Search : BottomMenu()
        class AddDoc : BottomMenu()
    }

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

    sealed class Type : LibraryEvent() {
        class Create(val name: String = "") : Type()
        class Edit(val item: LibraryView.MyTypeView) : Type()
    }

    sealed class Relation : LibraryEvent() {
        class Create(val name: String = "") : Relation()
        class Edit(val item: LibraryView.MyRelationView) : Relation()
    }

}

sealed class LibraryAnalyticsEvent {
    sealed class Ui {
        object Idle : Ui()
        sealed class TabView(val type: String, val view: String) : Ui() {
            object Types : TabView(type = TypeType, view = ViewYour)
            object Relations : TabView(type = TypeRelation, view = ViewYour)
            object LibTypes : TabView(type = TypeType, view = ViewLibrary)
            object LibRelations : TabView(type = TypeRelation, view = ViewLibrary)
        }
    }
}

private const val TypeType = "type"
private const val TypeRelation = "relation"
private const val ViewYour = "your"
private const val ViewLibrary = "library"