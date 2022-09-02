package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.core_utils.tools.UrlValidator
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.linking.LinkToObjectOrWebViewModelFactory
import com.anytypeio.anytype.ui.linking.LinkToObjectOrWebPagesFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(
    modules = [LinkToObjectOrWebModule::class]
)
@PerModal
interface LinkToObjectOrWebSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: LinkToObjectOrWebModule): Builder
        fun build(): LinkToObjectOrWebSubComponent
    }

    fun inject(fragment: LinkToObjectOrWebPagesFragment)
}

@Module
object LinkToObjectOrWebModule {

    @JvmStatic
    @PerModal
    @Provides
    fun provideLinkToObjectViewModelFactory(
        urlBuilder: UrlBuilder,
        objectTypesProvider: ObjectTypesProvider,
        searchObjects: SearchObjects,
        analytics: Analytics,
        stores: Editor.Storage,
        urlValidator: UrlValidator
    ): LinkToObjectOrWebViewModelFactory = LinkToObjectOrWebViewModelFactory(
        urlBuilder = urlBuilder,
        objectTypesProvider = objectTypesProvider,
        searchObjects = searchObjects,
        analytics = analytics,
        stores = stores,
        urlValidator = urlValidator
    )
}