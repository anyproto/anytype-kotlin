package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.GlobalSearchHistory
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ThemeMode
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.WidgetSession
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.settings.VaultSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {

    suspend fun getVaultSettings(account: Account): VaultSettings
    suspend fun observeVaultSettings(account: Account): Flow<VaultSettings>

    suspend fun setCurrentSpace(space: SpaceId)
    suspend fun getCurrentSpace(): SpaceId?
    suspend fun clearCurrentSpace()

    suspend fun setWallpaper(space: Id, wallpaper: Wallpaper)
    suspend fun getWallpaper(space: Id): Wallpaper
    suspend fun getWallpapers(): Map<Id, Wallpaper>

    suspend fun setDefaultObjectType(space: SpaceId, type: TypeId)
    suspend fun getDefaultObjectType(space: SpaceId): TypeId?

    suspend fun setPinnedObjectTypes(space: SpaceId, types: List<TypeId>)
    fun getPinnedObjectTypes(space: SpaceId) : Flow<List<TypeId>>

    suspend fun setLastOpenedObject(id: Id, space: SpaceId)
    suspend fun getLastOpenedObject(space: SpaceId) : Id?
    suspend fun clearLastOpenedObject(space: SpaceId)

    suspend fun setGlobalSearchHistory(globalSearchHistory: GlobalSearchHistory, space: SpaceId)
    suspend fun setGlobalSearchHistory(space: SpaceId): GlobalSearchHistory?
    suspend fun clearGlobalSearchHistory(space: SpaceId)

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun getThemeMode(): ThemeMode

    suspend fun getWidgetSession() : WidgetSession
    suspend fun saveWidgetSession(session: WidgetSession)

    suspend fun getAllContentSort(space: SpaceId): Pair<Id, Boolean>?
    suspend fun setAllContentSort(space: SpaceId, sort: Id, isAsc: Boolean)

    suspend fun clear()

    suspend fun setRelativeDates(account: Account, enabled: Boolean)
    suspend fun setDateFormat(account: Account, format: String)

    suspend fun setRecentlyUsedChatReactions(account: Account, emojis: Set<String>)
    fun observeRecentlyUsedChatReactions(account: Account): Flow<List<String>>
}