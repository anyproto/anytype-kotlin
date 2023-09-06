package com.anytypeio.anytype.persistence

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
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

    @Test
    fun `should save new default object type for given space`() = runTest {

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs
        )

        val space = SpaceId(MockDataFactory.randomUuid())
        val type = TypeId(MockDataFactory.randomUuid())

        // Settings are empty before we save anything

        assertEquals(
            expected = null,
            actual = cache.getDefaultObjectType(space)
        )

        // Saving object type for given space

        cache.setDefaultObjectType(
            space = space,
            type = type
        )

        // Making sure default object type is saved

        assertEquals(
            expected = type,
            actual = cache.getDefaultObjectType(space)
        )
    }

    @Test
    fun `should save default object type for two given spaces`() = runTest {

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs
        )

        val space1 = SpaceId(MockDataFactory.randomUuid())
        val type1 = TypeId(MockDataFactory.randomUuid())

        val space2 = SpaceId(MockDataFactory.randomUuid())
        val type2 = TypeId(MockDataFactory.randomUuid())

        // Settings are empty before we save anything

        assertEquals(
            expected = null,
            actual = cache.getDefaultObjectType(space1)
        )

        // Saving the first object type for the first space

        cache.setDefaultObjectType(
            space = space1,
            type = type1
        )

        // Making sure the first object type is saved

        assertEquals(
            expected = type1,
            actual = cache.getDefaultObjectType(space1)
        )

        // Making sure the second object type is not saved yet

        assertEquals(
            expected = null,
            actual = cache.getDefaultObjectType(space2)
        )

        // Saving the second object type for the second space

        cache.setDefaultObjectType(
            space = space2,
            type = type2
        )

        // Making sure the second object type is saved

        assertEquals(
            expected = type2,
            actual = cache.getDefaultObjectType(space2)
        )
    }
}