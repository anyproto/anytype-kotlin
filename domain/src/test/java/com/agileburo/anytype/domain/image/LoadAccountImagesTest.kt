package com.agileburo.anytype.domain.image

import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.model.Image
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.common.MockDataFactory
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class LoadAccountImagesTest {

    @Mock
    lateinit var loader: ImageLoader

    lateinit var loadAccountImages: LoadAccountImages

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        loadAccountImages = LoadAccountImages(loader = loader)
    }

    @Test
    fun `should not load any image if an account does not have any avatar image`() = runBlocking {

        val account = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomUuid(),
            avatar = null
        )

        val blob = ByteArray(0)

        val params = LoadAccountImages.Params(
            accounts = listOf(account)
        )

        loader.stub {
            onBlocking {
                load(
                    id = any(),
                    size = any()
                )
            } doReturn blob
        }

        val result = loadAccountImages.run(params)

        verifyZeroInteractions(loader)

        val expected = Either.Right(mapOf(account to null))

        assertEquals(expected, result)
    }

    @Test
    fun `should load one image for one account`() = runBlocking {

        val account = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomUuid(),
            avatar = Image(
                id = MockDataFactory.randomUuid(),
                sizes = listOf(Image.Size.SMALL)
            )
        )

        val blob = ByteArray(0)

        val params = LoadAccountImages.Params(
            accounts = listOf(account)
        )

        loader.stub {
            onBlocking {
                load(
                    id = account.avatar!!.id,
                    size = Image.Size.SMALL
                )
            } doReturn blob
        }

        val result = loadAccountImages.run(params)

        verify(loader, times(1)).load(account.avatar!!.id, Image.Size.SMALL)

        val expected = Either.Right(mapOf(account to blob))

        assertEquals(expected, result)
    }

    @Test
    fun `should load an image for online one of two accounts`() = runBlocking {

        val firstAccount = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomUuid(),
            avatar = Image(
                id = MockDataFactory.randomUuid(),
                sizes = listOf(Image.Size.SMALL)
            )
        )

        val secondAccount = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomUuid(),
            avatar = Image(
                id = MockDataFactory.randomUuid(),
                sizes = listOf(Image.Size.SMALL)
            )
        )

        val blob = ByteArray(0)

        val params = LoadAccountImages.Params(
            accounts = listOf(firstAccount, secondAccount)
        )

        loader.stub {
            onBlocking {
                load(
                    id = secondAccount.avatar!!.id,
                    size = Image.Size.SMALL
                )
            } doReturn blob
        }

        val result = loadAccountImages.run(params)

        verify(loader, times(1)).load(firstAccount.avatar!!.id, Image.Size.SMALL)

        val expected = Either.Right(mapOf(firstAccount to null, secondAccount to blob))

        assertEquals(expected, result)
    }

    @Test
    fun `should not load an image for given account if there are no image sizes`() = runBlocking {

        val sizes = emptyList<Image.Size>()

        val account = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomUuid(),
            avatar = Image(
                id = MockDataFactory.randomUuid(),
                sizes = sizes
            )
        )

        val params = LoadAccountImages.Params(
            accounts = listOf(account)
        )

        val result = loadAccountImages.run(params)

        verifyZeroInteractions(loader)

        val expected = Either.Right(mapOf(account to null))

        assertEquals(expected, result)
    }

    @Test
    fun `if loading is failed, we get null instead of an image`() = runBlocking {

        val account = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomUuid(),
            avatar = Image(
                id = MockDataFactory.randomUuid(),
                sizes = listOf(Image.Size.SMALL)
            )
        )

        val params = LoadAccountImages.Params(
            accounts = listOf(account)
        )

        loader.stub {
            onBlocking {
                load(
                    id = account.avatar!!.id,
                    size = Image.Size.SMALL
                )
            } doThrow IllegalArgumentException("Error while loading image with id: ${account.avatar!!.id}")
        }

        val result = loadAccountImages.run(params)

        verify(loader, times(1)).load(account.avatar!!.id, Image.Size.SMALL)

        val expected = Either.Right(mapOf(account to null))

        assertEquals(expected, result)
    }

}