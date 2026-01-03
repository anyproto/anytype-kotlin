package com.anytypeio.anytype.di.feature.appwidget

import com.anytypeio.anytype.appwidget.WidgetDataProvider
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.workspace.SpaceManager
import dagger.Component

@Component(dependencies = [AppWidgetDependencies::class])
interface AppWidgetComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: AppWidgetDependencies): AppWidgetComponent
    }

    fun widgetDataProvider(): WidgetDataProvider
}

interface AppWidgetDependencies : ComponentDependencies {
    fun spaceManager(): SpaceManager
    fun blockRepo(): BlockRepository
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun userRepo(): UserSettingsRepository
    fun urlBuilder(): UrlBuilder
    fun userPermissionProvider(): UserPermissionProvider
    fun stringResProvider(): StringResourceProvider
    fun fieldParser(): FieldParser
}
