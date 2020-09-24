package com.anytypeio.anytype.domain.database.repo

import com.anytypeio.anytype.domain.database.model.DatabaseView
import com.anytypeio.anytype.domain.database.model.ViewType

interface DatabaseRepository {
    fun getDatabase(id: String): DatabaseView
    fun updateDatabase(databse: DatabaseView)
    fun updateViewType(type: ViewType)
}