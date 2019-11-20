package com.agileburo.anytype.data

import com.agileburo.anytype.data.auth.mapper.toDomain
import com.agileburo.anytype.data.auth.mapper.toEntity
import com.agileburo.anytype.data.auth.model.AccountEntity
import com.agileburo.anytype.data.auth.model.ImageEntity
import com.agileburo.anytype.data.auth.model.WalletEntity
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.model.Image
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
    fun `should map correctly image size from data to domain layer`() {

        val small = ImageEntity.Size.SMALL
        val large = ImageEntity.Size.LARGE
        val thumb = ImageEntity.Size.THUMB

        assertTrue { small.toDomain() == Image.Size.SMALL }
        assertTrue { large.toDomain() == Image.Size.LARGE }
        assertTrue { thumb.toDomain() == Image.Size.THUMB }
    }

    @Test
    fun `should map correctly image size from domain to data layer`() {

        val small = Image.Size.SMALL
        val large = Image.Size.LARGE
        val thumb = Image.Size.THUMB

        assertTrue { small.toEntity() == ImageEntity.Size.SMALL }
        assertTrue { large.toEntity() == ImageEntity.Size.LARGE }
        assertTrue { thumb.toEntity() == ImageEntity.Size.THUMB }
    }

    @Test
    fun `should map correctly image from domain to data layer`() {

        val image = Image(
            id = MockDataFactory.randomString(),
            sizes = listOf(Image.Size.SMALL, Image.Size.LARGE)
        )

        image.toEntity().let { result ->
            assertTrue { result.id == image.id }
            assertTrue { result.sizes.size == image.sizes.size }
            assertTrue { result.sizes[0] == ImageEntity.Size.SMALL }
            assertTrue { result.sizes[1] == ImageEntity.Size.LARGE }
        }
    }

    @Test
    fun `should map correctly image from data to domain layer`() {

        val image = ImageEntity(
            id = MockDataFactory.randomString(),
            sizes = listOf(ImageEntity.Size.LARGE, ImageEntity.Size.THUMB)
        )

        image.toDomain().let { result ->
            assertTrue { result.id == image.id }
            assertTrue { result.sizes.size == image.sizes.size }
            assertTrue { result.sizes[0] == Image.Size.LARGE }
            assertTrue { result.sizes[1] == Image.Size.THUMB }
        }
    }

    @Test
    fun `should map correctly account from domain to data layer`() {

        val account = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = Image(
                id = MockDataFactory.randomUuid(),
                sizes = listOf(Image.Size.SMALL, Image.Size.LARGE)
            ),
            color = MockDataFactory.randomString()
        )

        account.toEntity().let { result ->
            assertTrue { result.id == account.id }
            assertTrue { result.name == account.name }
            assertTrue { result.avatar != null }
            assertTrue { result.color == account.color }

            result.avatar?.let { avatar ->
                assertTrue { avatar.sizes.size == 2 }
                assertTrue { avatar.sizes[0] == ImageEntity.Size.SMALL }
                assertTrue { avatar.sizes[1] == ImageEntity.Size.LARGE }
            } ?: throw IllegalStateException("Avatar should not be null")
        }
    }

    @Test
    fun `should map correctly account from data to domain layer`() {

        val account = AccountEntity(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = ImageEntity(
                id = MockDataFactory.randomUuid(),
                sizes = listOf(ImageEntity.Size.SMALL, ImageEntity.Size.LARGE)
            ),
            color = MockDataFactory.randomString()
        )

        account.toDomain().let { result ->
            assertTrue { result.id == account.id }
            assertTrue { result.name == account.name }
            assertTrue { result.avatar != null }
            assertTrue { result.color == account.color }

            result.avatar?.let { avatar ->
                assertTrue { avatar.sizes.size == 2 }
                assertTrue { avatar.sizes[0] == Image.Size.SMALL }
                assertTrue { avatar.sizes[1] == Image.Size.LARGE }
            } ?: throw IllegalStateException("Avatar should not be null")
        }
    }
}