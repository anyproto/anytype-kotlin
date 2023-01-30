package com.anytypeio.anytype.presentation.library

import com.anytypeio.anytype.core_models.ObjectWrapper

class LibraryScreenState(
    val types: Tabs.Types,
    val relations: Tabs.Relations
) {
    sealed class Tabs(
        val my: TabData,
        val lib: TabData
    ) {
        class Types(
            private val myTypes: TabData,
            private val libTypes: TabData
        ) : Tabs(myTypes, libTypes)

        class Relations(
            private val myRelations: TabData,
            private val libRelations: TabData
        ) : Tabs(myRelations, libRelations)

        data class TabData(
            val items: List<ObjectWrapper> = emptyList()
        )

    }
}