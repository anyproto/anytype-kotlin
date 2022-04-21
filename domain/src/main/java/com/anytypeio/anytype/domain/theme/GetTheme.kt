package com.anytypeio.anytype.domain.theme

import com.anytypeio.anytype.core_models.ThemeMode
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.UserSettingsRepository

class GetTheme(
    private val repo: UserSettingsRepository
) : BaseUseCase<ThemeMode, BaseUseCase.None>() {
    override suspend fun run(params: None) = safe {
        repo.getThemeMode()
    }
}