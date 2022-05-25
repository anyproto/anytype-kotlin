package com.anytypeio.anytype.domain.theme

import com.anytypeio.anytype.core_models.ThemeMode
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.UserSettingsRepository

class SetTheme(
    private val repo: UserSettingsRepository
) : BaseUseCase<Unit, ThemeMode>() {
    override suspend fun run(params: ThemeMode) = safe {
        repo.setThemeMode(params)
    }
}