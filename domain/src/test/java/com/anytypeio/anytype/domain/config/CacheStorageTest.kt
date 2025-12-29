package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CacheStorageTest {

    private lateinit var storage: CacheStorage

    @Before
    fun setup() {
        storage = CacheStorage()
    }

    @Test
    fun `initial state should return null for getOrNull`() {
        assertNull(storage.getOrNull())
    }

    @Test
    fun `initial state should return null for getAccountId`() {
        assertNull(storage.getAccountId())
    }

    @Test
    fun `initial state should return null for provide`() {
        assertNull(storage.provide())
    }

    @Test
    fun `set should store config and return it via getOrNull`() {
        val config = StubConfig()
        val accountId = MockDataFactory.randomUuid()

        storage.set(config, accountId)

        assertEquals(config, storage.getOrNull())
    }

    @Test
    fun `set should store accountId and return it via getAccountId`() {
        val config = StubConfig()
        val accountId = MockDataFactory.randomUuid()

        storage.set(config, accountId)

        assertEquals(accountId, storage.getAccountId())
    }

    @Test
    fun `provide should return SpaceId from techSpace after set`() {
        val techSpace = MockDataFactory.randomUuid()
        val config = StubConfig(techSpace = techSpace)
        val accountId = MockDataFactory.randomUuid()

        storage.set(config, accountId)

        assertEquals(SpaceId(techSpace), storage.provide())
    }

    @Test
    fun `clear should reset getOrNull to null`() {
        val config = StubConfig()
        val accountId = MockDataFactory.randomUuid()

        storage.set(config, accountId)
        storage.clear()

        assertNull(storage.getOrNull())
    }

    @Test
    fun `clear should reset getAccountId to null`() {
        val config = StubConfig()
        val accountId = MockDataFactory.randomUuid()

        storage.set(config, accountId)
        storage.clear()

        assertNull(storage.getAccountId())
    }

    @Test
    fun `clear should reset provide to null`() {
        val config = StubConfig()
        val accountId = MockDataFactory.randomUuid()

        storage.set(config, accountId)
        storage.clear()

        assertNull(storage.provide())
    }

    @Test
    fun `set should override previous values`() {
        val config1 = StubConfig()
        val accountId1 = MockDataFactory.randomUuid()
        val config2 = StubConfig()
        val accountId2 = MockDataFactory.randomUuid()

        storage.set(config1, accountId1)
        storage.set(config2, accountId2)

        assertEquals(config2, storage.getOrNull())
        assertEquals(accountId2, storage.getAccountId())
    }
}
