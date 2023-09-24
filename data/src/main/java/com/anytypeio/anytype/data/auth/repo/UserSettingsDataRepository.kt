package com.anytypeio.anytype.data.auth.repo

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ThemeMode
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.WidgetSession
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.domain.config.UserSettingsRepository

class UserSettingsDataRepository(private val cache: UserSettingsCache) : UserSettingsRepository {

    override suspend fun setWallpaper(space: Id, wallpaper: Wallpaper) {
        cache.setWallpaper(space, wallpaper)
    }

    override suspend fun getWallpaper(space: Id): Wallpaper = cache.getWallpaper(space)

    override suspend fun setDefaultObjectType(
        space: SpaceId,
        type: TypeId
    ): Unit = cache.setDefaultObjectType(
        space = space,
        type = type
    )

    override suspend fun getDefaultObjectType(
        space: SpaceId
    ): TypeId? = cache.getDefaultObjectType(space = space)

    override suspend fun setThemeMode(mode: ThemeMode) {
        cache.setThemeMode(mode)
    }

    override suspend fun getThemeMode(): ThemeMode = cache.getThemeMode()

    override suspend fun getWidgetSession(): WidgetSession = cache.getWidgetSession()

    override suspend fun saveWidgetSession(session: WidgetSession) = cache.saveWidgetSession(
        session = session
    )

    override suspend fun clear() = cache.clear()

    override suspend fun setCurrentSpace(space: SpaceId) = cache.setCurrentSpace(space)

    override suspend fun getCurrentSpace(): SpaceId? = cache.getCurrentSpace()
}