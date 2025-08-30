package com.anytypeio.anytype.persistence

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.DEFAULT_RELATIVE_DATES
import com.anytypeio.anytype.core_models.GlobalSearchHistory
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.settings.VaultSettings
import com.anytypeio.anytype.device.providers.AppDefaultDateFormatProvider
import com.anytypeio.anytype.persistence.common.serializeWallpaperSettings
import com.anytypeio.anytype.persistence.model.WallpaperSetting as ModelWallpaperSetting
import com.anytypeio.anytype.persistence.repo.DefaultUserSettingsCache
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
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

    @Test
    fun `should return default vault settings with default params`() = runTest {

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
        )

        val dateFormat = "MM/dd/yyyy - ${RandomString.make()}"

        Mockito.`when`(dateFormatProvider.provide()).thenReturn(dateFormat)

        val account = Account(
            id = MockDataFactory.randomUuid(),
        )

        val vaultSettings = cache.getVaultSettings(account = account)

        val expected = VaultSettings(
            dateFormat = dateFormat,
            isRelativeDates = DEFAULT_RELATIVE_DATES
        )

        assertEquals(
            expected = expected,
            actual = vaultSettings
        )
    }

    @Test
    fun `should return updated vault date format settings`() = runTest {

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
        )

        val dateFormat = "MM/dd/yyyy - ${RandomString.make()}"
        val updatedDateFormat = "yyyy-MM-dd - ${RandomString.make()}"

        Mockito.`when`(dateFormatProvider.provide()).thenReturn(dateFormat)

        val account = Account(
            id = MockDataFactory.randomUuid(),
        )

        cache.setDateFormat(
            account = account,
            format = updatedDateFormat
        )

        val vaultSettings = cache.getVaultSettings(account = account)

        val expected = VaultSettings(
            dateFormat = updatedDateFormat,
            isRelativeDates = DEFAULT_RELATIVE_DATES
        )

        assertEquals(
            expected = expected,
            actual = vaultSettings
        )
    }

    @Test
    fun `should return updated vault relative dates settings`() = runTest {

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
        )

        val dateFormat = "MM/dd/yyyy - ${RandomString.make()}"

        Mockito.`when`(dateFormatProvider.provide()).thenReturn(dateFormat)

        val account = Account(
            id = MockDataFactory.randomUuid(),
        )

        cache.setRelativeDates(
            account = account,
            enabled = false
        )

        val vaultSettings = cache.getVaultSettings(account = account)

        val expected = VaultSettings(
            dateFormat = dateFormat,
            isRelativeDates = false
        )

        assertEquals(
            expected = expected,
            actual = vaultSettings
        )
    }

    @Test
    fun `should return updated vault relative dates settings and keep date format settings`() = runTest {

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
        )

        val dateFormat = "MM/dd/yyyy - ${RandomString.make()}"
        val updatedDateFormat = "yyyy-MM-dd - ${RandomString.make()}"

        Mockito.`when`(dateFormatProvider.provide()).thenReturn(dateFormat)

        val account = Account(
            id = MockDataFactory.randomUuid(),
        )

        cache.setDateFormat(
            account = account,
            format = updatedDateFormat
        )

        cache.setRelativeDates(
            account = account,
            enabled = false
        )

        val vaultSettings = cache.getVaultSettings(account = account)

        val expected = VaultSettings(
            dateFormat = updatedDateFormat,
            isRelativeDates = false
        )

        assertEquals(
            expected = expected,
            actual = vaultSettings
        )
    }

    @Test
    fun `should migrate single wallpaper from SharedPreferences to DataStore`() = runTest {
        
        // Clear preferences to start with clean state
        defaultPrefs.edit().clear().apply()
        
        val space1 = MockDataFactory.randomUuid()
        val wallpaper1 = Wallpaper.Gradient(code = "gradient123")
        
        // Simulate existing SharedPreferences data
        val wallpaperMap = mapOf(
            space1 to ModelWallpaperSetting(type = 2, value = "gradient123")
        )
        val serializedWallpapers = wallpaperMap.serializeWallpaperSettings()
        
        defaultPrefs.edit()
            .putString("prefs.user_settings.wallpaper_settings", serializedWallpapers)
            .apply()

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
        )

        // First access should trigger migration
        val actual = cache.getWallpaper(space1)

        assertEquals(
            expected = wallpaper1,
            actual = actual
        )

        // Verify SharedPreferences data is cleaned up
        val remainingData = defaultPrefs.getString("prefs.user_settings.wallpaper_settings", null)
        assertEquals(null, remainingData)
    }

    @Test
    fun `should migrate multiple wallpapers from SharedPreferences to DataStore`() = runTest {
        
        // Clear preferences to start with clean state
        defaultPrefs.edit().clear().apply()
        
        val space1 = MockDataFactory.randomUuid()
        val space2 = MockDataFactory.randomUuid()
        val space3 = MockDataFactory.randomUuid()
        
        val wallpaper1 = Wallpaper.Color(code = "color123")
        val wallpaper2 = Wallpaper.Gradient(code = "gradient456")
        val wallpaper3 = Wallpaper.Image(hash = "image789")
        
        // Simulate existing SharedPreferences data with multiple wallpapers
        val wallpaperMap = mapOf(
            space1 to ModelWallpaperSetting(type = 1, value = "color123"),
            space2 to ModelWallpaperSetting(type = 2, value = "gradient456"),
            space3 to ModelWallpaperSetting(type = 3, value = "image789")
        )
        val serializedWallpapers = wallpaperMap.serializeWallpaperSettings()
        
        defaultPrefs.edit()
            .putString("prefs.user_settings.wallpaper_settings", serializedWallpapers)
            .apply()

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
        )

        // Access all wallpapers should trigger migration
        val allWallpapers = cache.getWallpapers()
        
        assertEquals(3, allWallpapers.size)
        assertEquals(wallpaper1, allWallpapers[space1])
        assertEquals(wallpaper2, allWallpapers[space2])
        assertEquals(wallpaper3, allWallpapers[space3])

        // Verify individual access works
        assertEquals(wallpaper1, cache.getWallpaper(space1))
        assertEquals(wallpaper2, cache.getWallpaper(space2))
        assertEquals(wallpaper3, cache.getWallpaper(space3))

        // Verify SharedPreferences data is cleaned up
        val remainingData = defaultPrefs.getString("prefs.user_settings.wallpaper_settings", null)
        assertEquals(null, remainingData)
    }

    @Test
    fun `should not migrate when no SharedPreferences wallpaper data exists`() = runTest {
        
        // Clear preferences to start with clean state
        defaultPrefs.edit().clear().apply()

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
        )

        val space = MockDataFactory.randomUuid()
        
        // First access should not migrate anything
        val actual = cache.getWallpaper(space)

        assertEquals(Wallpaper.Default, actual)
        
        // Verify no SharedPreferences data was created
        val sharedPrefsData = defaultPrefs.getString("prefs.user_settings.wallpaper_settings", null)
        assertEquals(null, sharedPrefsData)
    }

    @Test
    fun `should not migrate when SharedPreferences wallpaper data is empty`() = runTest {
        
        // Clear preferences to start with clean state
        defaultPrefs.edit().clear().apply()
        
        // Set empty string in SharedPreferences
        defaultPrefs.edit()
            .putString("prefs.user_settings.wallpaper_settings", "")
            .apply()

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
        )

        val space = MockDataFactory.randomUuid()
        
        // First access should not migrate anything
        val actual = cache.getWallpaper(space)

        assertEquals(Wallpaper.Default, actual)
        
        // Verify empty SharedPreferences data still exists (not cleaned up when empty)
        val sharedPrefsData = defaultPrefs.getString("prefs.user_settings.wallpaper_settings", null)
        assertEquals("", sharedPrefsData)
    }

    @Test
    fun `should work correctly after migration is complete`() = runTest {
        
        // Clear preferences to start with clean state
        defaultPrefs.edit().clear().apply()
        
        val space1 = MockDataFactory.randomUuid()
        val space2 = MockDataFactory.randomUuid()
        val existingWallpaper = Wallpaper.Color(code = "existing123")
        
        // Simulate existing SharedPreferences data
        val wallpaperMap = mapOf(
            space1 to ModelWallpaperSetting(type = 1, value = "existing123")
        )
        val serializedWallpapers = wallpaperMap.serializeWallpaperSettings()
        
        defaultPrefs.edit()
            .putString("prefs.user_settings.wallpaper_settings", serializedWallpapers)
            .apply()

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
        )

        // First access triggers migration
        assertEquals(existingWallpaper, cache.getWallpaper(space1))
        
        // Now set new wallpaper after migration
        val newWallpaper = Wallpaper.Gradient(code = "new456")
        cache.setWallpaper(space2, newWallpaper)
        
        // Verify both old (migrated) and new wallpapers work
        assertEquals(existingWallpaper, cache.getWallpaper(space1))
        assertEquals(newWallpaper, cache.getWallpaper(space2))
        
        val allWallpapers = cache.getWallpapers()
        assertEquals(2, allWallpapers.size)
        assertEquals(existingWallpaper, allWallpapers[space1])
        assertEquals(newWallpaper, allWallpapers[space2])
    }

    @Test
    fun `should only migrate once even with multiple access`() = runTest {
        
        // Clear preferences to start with clean state
        defaultPrefs.edit().clear().apply()
        
        val space1 = MockDataFactory.randomUuid()
        val wallpaper1 = Wallpaper.Color(code = "color123")
        
        // Simulate existing SharedPreferences data
        val wallpaperMap = mapOf(
            space1 to ModelWallpaperSetting(type = 1, value = "color123")
        )
        val serializedWallpapers = wallpaperMap.serializeWallpaperSettings()
        
        defaultPrefs.edit()
            .putString("prefs.user_settings.wallpaper_settings", serializedWallpapers)
            .apply()

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
        )

        // Multiple accesses should all work but migration should only happen once
        assertEquals(wallpaper1, cache.getWallpaper(space1))
        assertEquals(wallpaper1, cache.getWallpaper(space1))
        assertEquals(1, cache.getWallpapers().size)
        assertEquals(wallpaper1, cache.getWallpaper(space1))
        
        // Verify SharedPreferences data is cleaned up after first migration
        val remainingData = defaultPrefs.getString("prefs.user_settings.wallpaper_settings", null)
        assertEquals(null, remainingData)
    }

    @Test
    fun `should preserve other space settings when setting wallpaper`() = runTest {
        
        // Clear preferences to start with clean state
        defaultPrefs.edit().clear().apply()

        val cache = DefaultUserSettingsCache(
            prefs = defaultPrefs,
            context = ApplicationProvider.getApplicationContext(),
            appDefaultDateFormatProvider = dateFormatProvider
        )

        val space1 = SpaceId(MockDataFactory.randomUuid())
        val space2 = SpaceId(MockDataFactory.randomUuid())
        
        val type1 = TypeId(MockDataFactory.randomUuid())
        val type2 = TypeId(MockDataFactory.randomUuid())
        
        val pinnedTypes1 = listOf(TypeId(MockDataFactory.randomUuid()), TypeId(MockDataFactory.randomUuid()))
        val pinnedTypes2 = listOf(TypeId(MockDataFactory.randomUuid()))
        
        val lastOpenedObject1 = MockDataFactory.randomUuid()
        val lastOpenedObject2 = MockDataFactory.randomUuid()
        
        val globalSearch1 = GlobalSearchHistory(
            query = "test query 1",
            relatedObject = MockDataFactory.randomUuid()
        )
        val globalSearch2 = GlobalSearchHistory(
            query = "test query 2", 
            relatedObject = MockDataFactory.randomUuid()
        )

        // Set up existing space settings for both spaces
        cache.setDefaultObjectType(space1, type1)
        cache.setDefaultObjectType(space2, type2)
        
        cache.setPinnedObjectTypes(space1, pinnedTypes1)
        cache.setPinnedObjectTypes(space2, pinnedTypes2)
        
        cache.setLastOpenedObject(lastOpenedObject1, space1)
        cache.setLastOpenedObject(lastOpenedObject2, space2)
        
        cache.setGlobalSearchHistory(globalSearch1, space1)
        cache.setGlobalSearchHistory(globalSearch2, space2)

        // Verify initial settings are correct
        assertEquals(type1, cache.getDefaultObjectType(space1))
        assertEquals(type2, cache.getDefaultObjectType(space2))
        assertEquals(pinnedTypes1, cache.getPinnedObjectTypes(space1).first())
        assertEquals(pinnedTypes2, cache.getPinnedObjectTypes(space2).first())
        assertEquals(lastOpenedObject1, cache.getLastOpenedObject(space1))
        assertEquals(lastOpenedObject2, cache.getLastOpenedObject(space2))
        assertEquals(globalSearch1, cache.getGlobalSearchHistory(space1))
        assertEquals(globalSearch2, cache.getGlobalSearchHistory(space2))
        
        // Initial wallpapers should be default
        assertEquals(Wallpaper.Default, cache.getWallpaper(space1.id))
        assertEquals(Wallpaper.Default, cache.getWallpaper(space2.id))

        // Now set wallpaper for space1 only
        val newWallpaper = Wallpaper.Gradient(code = "gradient123")
        cache.setWallpaper(space1.id, newWallpaper)

        // Verify wallpaper was set correctly
        assertEquals(newWallpaper, cache.getWallpaper(space1.id))
        assertEquals(Wallpaper.Default, cache.getWallpaper(space2.id)) // space2 wallpaper unchanged

        // Verify ALL other space1 settings remain unchanged after wallpaper set
        assertEquals(type1, cache.getDefaultObjectType(space1))
        assertEquals(pinnedTypes1, cache.getPinnedObjectTypes(space1).first())
        assertEquals(lastOpenedObject1, cache.getLastOpenedObject(space1))
        assertEquals(globalSearch1, cache.getGlobalSearchHistory(space1))

        // Verify ALL space2 settings remain unchanged
        assertEquals(type2, cache.getDefaultObjectType(space2))
        assertEquals(pinnedTypes2, cache.getPinnedObjectTypes(space2).first())
        assertEquals(lastOpenedObject2, cache.getLastOpenedObject(space2))
        assertEquals(globalSearch2, cache.getGlobalSearchHistory(space2))

        // Now set wallpaper for space2 as well
        val anotherWallpaper = Wallpaper.Color(code = "color456")
        cache.setWallpaper(space2.id, anotherWallpaper)

        // Verify both wallpapers are correct
        assertEquals(newWallpaper, cache.getWallpaper(space1.id))
        assertEquals(anotherWallpaper, cache.getWallpaper(space2.id))

        // Verify all other settings still remain unchanged for both spaces
        assertEquals(type1, cache.getDefaultObjectType(space1))
        assertEquals(type2, cache.getDefaultObjectType(space2))
        assertEquals(pinnedTypes1, cache.getPinnedObjectTypes(space1).first())
        assertEquals(pinnedTypes2, cache.getPinnedObjectTypes(space2).first())
        assertEquals(lastOpenedObject1, cache.getLastOpenedObject(space1))
        assertEquals(lastOpenedObject2, cache.getLastOpenedObject(space2))
        assertEquals(globalSearch1, cache.getGlobalSearchHistory(space1))
        assertEquals(globalSearch2, cache.getGlobalSearchHistory(space2))
        
        // Verify getWallpapers() returns both wallpapers correctly
        val allWallpapers = cache.getWallpapers()
        assertEquals(2, allWallpapers.size)
        assertEquals(newWallpaper, allWallpapers[space1.id])
        assertEquals(anotherWallpaper, allWallpapers[space2.id])
    }
}