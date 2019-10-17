package com.agileburo.anytype.core_utils.di

import android.content.Context
import android.content.SharedPreferences
import com.agileburo.anytype.core_utils.data.UserCache
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ContextModule::class,
        DataModule::class
    ]
)
interface CoreComponent {
    fun context(): Context
    fun cache(): UserCache
    fun prefs(): SharedPreferences
}