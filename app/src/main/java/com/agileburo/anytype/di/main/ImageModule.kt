package com.agileburo.anytype.di.main

import com.agileburo.anytype.data.auth.other.ImageDataLoader
import com.agileburo.anytype.data.auth.other.ImageLoaderRemote
import com.agileburo.anytype.domain.image.ImageLoader
import com.agileburo.anytype.middleware.interactor.Middleware
import com.agileburo.anytype.middleware.interactor.MiddlewareImageLoader
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ImageModule {

    @Provides
    @Singleton
    fun provideImageLoader(remote: ImageLoaderRemote): ImageLoader {
        return ImageDataLoader(
            remote = remote
        )
    }

    @Provides
    @Singleton
    fun provideImageLoaderRemote(middleware: Middleware): ImageLoaderRemote {
        return MiddlewareImageLoader(
            middleware = middleware
        )
    }
}