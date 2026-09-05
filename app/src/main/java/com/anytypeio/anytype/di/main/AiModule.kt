package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.core_ai.MlKitTypeSuggestionEngine
import com.anytypeio.anytype.domain.ai.FlagGatedTypeSuggestionEngine
import com.anytypeio.anytype.domain.ai.TypeSuggestionEngine
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * On-device AI (quick capture type suggestions, spec §9). The ML Kit engine holds its
 * Gemini Nano client behind `by lazy`, and [FlagGatedTypeSuggestionEngine] checks the
 * feature flag before touching the delegate — together they keep AICore completely cold
 * for every user with the flag off, even though this singleton is constructed on the
 * first editor open.
 */
@Module
object AiModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideTypeSuggestionEngine(
        userSettingsRepository: UserSettingsRepository
    ): TypeSuggestionEngine = FlagGatedTypeSuggestionEngine(
        settings = userSettingsRepository,
        delegate = MlKitTypeSuggestionEngine()
    )
}
