package com.agileburo.anytype.data.auth.repo

import com.agileburo.anytype.domain.database.DatabaseMock
import com.agileburo.anytype.domain.database.model.DatabaseView
import com.agileburo.anytype.domain.database.model.ViewType
import com.agileburo.anytype.domain.database.repo.DatabaseRepository

class DatabaseDataRepository : DatabaseRepository {

    private var db: DatabaseView

    init {
        db = DatabaseMock.getDatabaseView(DatabaseMock.ID)
    }

    override fun getDatabase(id: String): DatabaseView = db

    override fun updateDatabase(databse: DatabaseView) {
        db = databse
    }

    override fun updateViewType(type: ViewType) {
        db.let {
            val list = it.content.displays
            list[0] = list.first().copy(type = type)
            db = it.copy(content = it.content.copy(displays = list))
        }
    }
}