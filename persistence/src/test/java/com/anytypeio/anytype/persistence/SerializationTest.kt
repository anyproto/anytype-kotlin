package com.anytypeio.anytype.persistence

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.persistence.common.serializeWallpaperSettings
import com.anytypeio.anytype.persistence.model.WallpaperSetting
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test

class SerializationTest {

    @Test
    fun test() {

        val id = MockDataFactory.randomUuid()
        val type = MockDataFactory.randomInt()
        val value = MockDataFactory.randomString()

        val givenMap = mapOf(
            id to WallpaperSetting(
                type = type,
                value = value
            )
        )

        val encoded = givenMap.serializeWallpaperSettings()

        val decoded = Json.decodeFromString<Map<Id, WallpaperSetting>>(encoded)

        assertEquals(
            expected = givenMap,
            actual = decoded
        )
    }

}