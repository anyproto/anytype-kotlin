package com.anytypeio.anytype.persistence.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.anytypeio.anytype.persistence.common.Config
import com.anytypeio.anytype.persistence.common.Provider
import com.anytypeio.anytype.persistence.dao.AccountDao
import com.anytypeio.anytype.persistence.model.AccountTable

@Database(
    entities = [AccountTable::class],
    exportSchema = false,
    version = 1
)
abstract class AnytypeDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao

    companion object : Provider<Context, AnytypeDatabase>() {
        override fun create(param: Context) = databaseBuilder(
            param.applicationContext,
            AnytypeDatabase::class.java,
            Config.DATABASE_NAME
        ).build()
    }
}