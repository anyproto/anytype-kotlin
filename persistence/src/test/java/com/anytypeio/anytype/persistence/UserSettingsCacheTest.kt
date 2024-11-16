package com.anytypeio.anytype.persistence

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_models.GlobalSearchHistory
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.device.providers.AppDefaultDateFormatProvider
import com.anytypeio.anytype.device.providers.AppDefaultDateFormatProviderImpl
import com.anytypeio.anytype.persistence.repo.DefaultUserSettingsCache
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class UserSettingsCacheTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val defaultPrefs = RuntimeEnvironment.getApplication().getSharedPreferences(
        "Default prefs",
        Context.MODE_PRIVATE
    )

    @Mock
    lateinit var dateFormatProvider: AppDefaultDateFormatProvider

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Mockito.`when`(dateFormatProvider.provide()).thenReturn("dd/MM/yyyy")
    }

    @Test
    fun `should save and return default wallpaper`() = runTest {

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
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
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
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
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
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
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
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
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
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
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
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
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
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
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
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

    @Test
    fun `should save global search for given space and then clear it`() = runTest {

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
        )

        val space = SpaceId(MockDataFactory.randomUuid())
        val globalSearch = GlobalSearchHistory(
            query = MockDataFactory.randomString(),
            relatedObject = MockDataFactory.randomUuid()
        )

        // Settings are empty before we save anything

        assertEquals(
            expected = null,
            actual = cache.getDefaultObjectType(space)
        )

        // Saving global search for given space

        cache.setGlobalSearchHistory(
            globalSearchHistory = globalSearch,
            space = space
        )

        // Making sure global search is saved

        assertEquals(
            expected = globalSearch,
            actual = cache.getGlobalSearchHistory(space)
        )

        cache.clearGlobalSearchHistory(space)

        assertEquals(
            expected = null,
            actual = cache.getGlobalSearchHistory(space)
        )
    }
}