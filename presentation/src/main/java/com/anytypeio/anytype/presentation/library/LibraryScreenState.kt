package com.anytypeio.anytype.presentation.library

import com.anytypeio.anytype.presentation.navigation.LibraryView

class LibraryScreenState(
    val types: Tabs.Types,
    val relations: Tabs.Relations
) {
    sealed class Tabs(
        val my: TabData,
        val lib: TabData
    ) {
        class Types(
            myTypes: TabData,
            libTypes: TabData
        ) : Tabs(myTypes, libTypes)

        class Relations(
            myRelations: TabData,
            libRelations: TabData
        ) : Tabs(myRelations, libRelations)

        data class TabData(
            val items: List<LibraryView> = emptyList()
        )

    }
}