package com.anytypeio.anytype.persistence

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.persistence.repo.DefaultUserSettingsCache
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class UserSettingsCacheTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val defaultPrefs = RuntimeEnvironment.getApplication().getSharedPreferences(
        "Default prefs",
        Context.MODE_PRIVATE
    )

    @Test
    fun `should save and return default wallpaper`() = runTest {

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs
        )

        val space = MockDataFactory.randomUuid()

        cache.setWallpaper(
            space = space,
            wallpaper = Wallpaper.Default
        )

        val expected = Wallpaper.Default
        val actual = cache.getWallpaper(space)

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should save and return gradient wallpaper`() = runTest {

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs
        )

        val space = MockDataFactory.randomUuid()

        val givenWallpaper = Wallpaper.Gradient(
            code = MockDataFactory.randomString()
        )

        cache.setWallpaper(
            space = space,
            wallpaper = givenWallpaper
        )

        val actual = cache.getWallpaper(space)

        assertEquals(
            expected = givenWallpaper,
            actual = actual
        )
    }

    @Test
    fun `should not save wallpaper if space id is empty, should return default`() = runTest {

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs
        )

        val space = ""

        val givenWallpaper = Wallpaper.Gradient(
            code = MockDataFactory.randomString()
        )

        cache.setWallpaper(
            space = space,
            wallpaper = givenWallpaper
        )

        val expected = Wallpaper.Default
        val actual = cache.getWallpaper(space)

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should save and return solid-color wallpaper`() = runTest {

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs
        )

        val space = MockDataFactory.randomUuid()

        val givenWallpaper = Wallpaper.Color(
            code = MockDataFactory.randomString()
        )

        cache.setWallpaper(
            space = space,
            wallpaper = givenWallpaper
        )

        val actual = cache.getWallpaper(space)

        assertEquals(
            expected = givenWallpaper,
            actual = actual
        )
    }

    @Test
    fun `should save and return image wallpaper`() = runTest {

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs
        )

        val space = MockDataFactory.randomUuid()

        val givenWallpaper = Wallpaper.Image(
            hash = MockDataFactory.randomString()
        )

        cache.setWallpaper(
            space = space,
            wallpaper = givenWallpaper
        )

        val actual = cache.getWallpaper(space)

        assertEquals(
            expected = givenWallpaper,
            actual = actual
        )
    }

    @Test
    fun `should return default wallpaper`() = runTest {

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs
        )

        val space = MockDataFactory.randomUuid()

        val expected = Wallpaper.Default
        val actual = cache.getWallpaper(space)

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

}