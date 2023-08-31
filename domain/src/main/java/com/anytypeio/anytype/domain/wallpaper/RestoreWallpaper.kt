package com.anytypeio.anytype.domain.wallpaper

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.FlowInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class RestoreWallpaper(
    private val repo: UserSettingsRepository,
    private val spaceManager: SpaceManager,
    private val store: WallpaperStore,
    dispatchers: AppCoroutineDispatchers
) : FlowInteractor<Unit, Unit>(dispatchers.io) {

    override fun build(): Flow<Unit> = spaceManager
        .observe()
        .map {
            val wallpaper = repo.getWallpaper(space = spaceManager.get())
            store.set(wallpaper)
        }
        .catch {
            // Do nothing.
        }
}