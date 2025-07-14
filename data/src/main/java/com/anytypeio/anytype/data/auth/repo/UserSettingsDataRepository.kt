package com.anytypeio.anytype.data.auth.repo

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.GlobalSearchHistory
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ThemeMode
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.WidgetSession
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.settings.VaultSettings
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import kotlinx.coroutines.flow.Flow

class UserSettingsDataRepository(private val cache: UserSettingsCache) : UserSettingsRepository {

    override suspend fun setWallpaper(space: Id, wallpaper: Wallpaper) {
        cache.setWallpaper(space, wallpaper)
    }

    override suspend fun getWallpaper(space: Id): Wallpaper = cache.getWallpaper(space)

    override suspend fun getWallpapers(): Map<Id, Wallpaper> {
        return cache.getWallpapers()
    }

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

    override suspend fun setPinnedObjectTypes(space: SpaceId, types: List<TypeId>) {
        cache.setPinnedObjectTypes(
            space = space,
            types = types
        )
    }

    override fun getPinnedObjectTypes(space: SpaceId): Flow<List<TypeId>> {
        return cache.getPinnedObjectTypes(space = space)
    }

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

    override suspend fun clearCurrentSpace() = cache.clearCurrentSpace()

    override suspend fun setLastOpenedObject(id: Id, space: SpaceId) {
        cache.setLastOpenedObject(id, space)
    }

    override suspend fun getLastOpenedObject(space: SpaceId): Id? {
        return cache.getLastOpenedObject(space)
    }

    override suspend fun clearLastOpenedObject(space: SpaceId) {
        cache.clearLastOpenedObject(space)
    }

    override suspend fun setGlobalSearchHistory(globalSearchHistory: GlobalSearchHistory, space: SpaceId) {
        cache.setGlobalSearchHistory(globalSearchHistory, space)
    }

    override suspend fun setGlobalSearchHistory(space: SpaceId): GlobalSearchHistory? {
        return cache.getGlobalSearchHistory(space)
    }

    override suspend fun clearGlobalSearchHistory(space: SpaceId) {
        cache.clearGlobalSearchHistory(space)
    }

    override suspend fun getVaultSettings(account: Account): VaultSettings {
        return cache.getVaultSettings(account)
    }

    override suspend fun observeVaultSettings(account: Account): Flow<VaultSettings> {
        return cache.observeVaultSettings(account)
    }

    override suspend fun getAllContentSort(space: SpaceId): Pair<Id, Boolean>? {
        return cache.getAllContentSort(space)
    }

    override suspend fun setAllContentSort(space: SpaceId, sort: Id, isAsc: Boolean) {
        cache.setAllContentSort(space, sort, isAsc)
    }

    override suspend fun setRelativeDates(
        account: Account,
        enabled: Boolean
    ) {
        cache.setRelativeDates(account, enabled)
    }

    override suspend fun setDateFormat(
        account: Account,
        format: String
    ) {
        cache.setDateFormat(account, format)
    }

    override suspend fun setRecentlyUsedChatReactions(account: Account, emojis: Set<String>) {
        cache.setRecentlyUsedChatReactions(account, emojis)
    }

    override fun observeRecentlyUsedChatReactions(account: Account,): Flow<List<String>> {
        return cache.observeRecentlyUsedChatReactions(account)
    }
}