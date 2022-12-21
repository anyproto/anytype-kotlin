package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.linking.LinkToObjectViewModelFactory
import com.anytypeio.anytype.ui.linking.LinkToObjectFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(
    modules = [LinkToObjectModule::class]
)
@PerScreen
interface LinkToObjectSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: LinkToObjectModule): Builder
        fun build(): LinkToObjectSubComponent
    }

    fun inject(fragment: LinkToObjectFragment)
}

@Module
object LinkToObjectModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun provideLinkToObjectViewModelFactory(
        urlBuilder: UrlBuilder,
        getObjectTypes: GetObjectTypes,
        searchObjects: SearchObjects,
        workspaceManager: WorkspaceManager,
        analytics: Analytics
    ): LinkToObjectViewModelFactory = LinkToObjectViewModelFactory(
        urlBuilder = urlBuilder,
        getObjectTypes = getObjectTypes,
        searchObjects = searchObjects,
        workspaceManager = workspaceManager,
        analytics = analytics
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun getObjectTypes(repo: BlockRepository): GetObjectTypes = GetObjectTypes(repo = repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun searchObjects(repo: BlockRepository): SearchObjects = SearchObjects(repo = repo)
}