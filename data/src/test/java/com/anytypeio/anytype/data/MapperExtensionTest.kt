package com.anytypeio.anytype.data

import com.anytypeio.anytype.data.auth.mapper.toDomain
import com.anytypeio.anytype.data.auth.mapper.toEntity
import com.anytypeio.anytype.data.auth.model.AccountEntity
import com.anytypeio.anytype.data.auth.model.WalletEntity
import com.anytypeio.anytype.domain.auth.model.Account
import org.junit.Test
import kotlin.test.assertTrue

class MapperExtensionTest {

    @Test
    fun `should map correctly wallet from data to domain layer`() {

        val wallet = WalletEntity(
            mnemonic = MockDataFactory.randomString()
        )

        val result = wallet.toDomain()

        assertTrue { result.mnemonic == wallet.mnemonic }
    }

    @Test
    fun `should map correctly account from domain to data layer`() {

        val account = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            color = MockDataFactory.randomString(),
            avatar = MockDataFactory.randomString()
        )

        account.toEntity().let { result ->
            assertTrue { result.id == account.id }
            assertTrue { result.name == account.name }
            assertTrue { result.color == account.color }
        }
    }

    @Test
    fun `should map correctly account from data to domain layer`() {

        val account = AccountEntity(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            color = MockDataFactory.randomString()
        )

        account.toDomain().let { result ->
            assertTrue { result.id == account.id }
            assertTrue { result.name == account.name }
            assertTrue { result.avatar == null }
            assertTrue { result.color == account.color }
        }
    }
}