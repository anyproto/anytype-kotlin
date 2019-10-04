package com.agileburo.anytype.core_utils.di

import android.content.Context
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ContextModule::class])
interface CoreComponent {
    fun context(): Context
}