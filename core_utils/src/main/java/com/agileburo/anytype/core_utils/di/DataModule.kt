package com.agileburo.anytype.core_utils.di

import android.content.Context
import android.content.SharedPreferences
import com.agileburo.anytype.core_utils.data.UserCache
import com.agileburo.anytype.core_utils.data.UserCacheImpl
import com.agileburo.anytype.db.AnytypeDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideAnytypeDatabase(context: Context): AnytypeDatabase {
        return AnytypeDatabase.get(context)
    }

    @Provides
    @Singleton
    fun provideUserCache(db: AnytypeDatabase): UserCache {
        return UserCacheImpl(db)
    }

}