package com.anytypeio.anytype.presentation.library

interface QueryListenerMyTypes {
    fun onQueryMyTypes(string: String)
}

interface QueryListenerMyRelations {
    fun onQueryMyRelations(string: String)
}

interface QueryListenerLibTypes {
    fun onQueryLibTypes(string: String)
}

interface QueryListenerLibRelations {
    fun onQueryLibRelations(string: String)
}