package com.agileburo.anytype.di.app

import com.agileburo.anytype.core_utils.ext.BaseSchedulerProvider
import com.agileburo.anytype.core_utils.ext.SchedulerProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 02.04.2019.
 */
@Module
class RxJavaModule {

    @Singleton
    @Provides
    fun provideSchedulerProvider(): BaseSchedulerProvider =
        SchedulerProvider()
}