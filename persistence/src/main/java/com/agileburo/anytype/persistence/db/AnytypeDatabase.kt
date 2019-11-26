package com.agileburo.anytype.persistence.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.agileburo.anytype.persistence.common.Config
import com.agileburo.anytype.persistence.common.Provider
import com.agileburo.anytype.persistence.dao.AccountDao
import com.agileburo.anytype.persistence.model.AccountTable
import com.agileburo.anytype.persistence.util.Converters

@Database(
    entities = [AccountTable::class],
    exportSchema = false,
    version = 1
)
@TypeConverters(Converters::class)
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