package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.core_utils.tools.UrlValidator
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.linking.LinkToObjectOrWebViewModel
import com.anytypeio.anytype.presentation.linking.LinkToObjectOrWebViewModelFactory
import com.anytypeio.anytype.ui.linking.LinkToObjectOrWebPagesFragment
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    modules = [LinkToObjectOrWebModule::class],
    dependencies = [LinkToObjectOrWebDependencies::class, EditorComponentDependencies::class]
)
@PerModal
interface LinkToObjectOrWebSubComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance params: LinkToObjectOrWebViewModel.VmParams,
            dependencies: LinkToObjectOrWebDependencies,
            editorDependencies: EditorComponentDependencies
        ): LinkToObjectOrWebSubComponent
    }

    fun inject(fragment: LinkToObjectOrWebPagesFragment)
}

interface LinkToObjectOrWebDependencies : ComponentDependencies {
    fun analyticSpaceHelperDelegate(): AnalyticSpaceHelperDelegate
    fun urlBuilder(): UrlBuilder
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun searchObjects(): SearchObjects
    fun analytics(): Analytics
    fun urlValidator(): UrlValidator
}

interface EditorComponentDependencies : ComponentDependencies {
    fun stores(): Editor.Storage
}

@Module
object LinkToObjectOrWebModule {

    @JvmStatic
    @PerModal
    @Provides
    fun provideLinkToObjectViewModelFactory(
        vmParams: LinkToObjectOrWebViewModel.VmParams,
        urlBuilder: UrlBuilder,
        storeOfObjectTypes: StoreOfObjectTypes,
        searchObjects: SearchObjects,
        analytics: Analytics,
        stores: Editor.Storage,
        urlValidator: UrlValidator,
        analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
    ): LinkToObjectOrWebViewModelFactory = LinkToObjectOrWebViewModelFactory(
        vmParams = vmParams,
        urlBuilder = urlBuilder,
        storeOfObjectTypes = storeOfObjectTypes,
        searchObjects = searchObjects,
        analytics = analytics,
        stores = stores,
        urlValidator = urlValidator,
        analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
    )
}