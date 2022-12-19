package com.anytypeio.anytype.domain.page.navigation

import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.DocumentInfo
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import kotlin.test.assertEquals

class GetListPagesTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repository: BlockRepository
    lateinit var getListPages: GetListPages

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        getListPages = GetListPages(repository)
    }

    @Test
    fun `should filter results by archived pages`() {

        val obj1 = ObjectWrapper.Basic(mapOf("name" to "Alex"))
        val obj2 = ObjectWrapper.Basic(mapOf("name" to "Mike", "isArchived" to false))
        val obj3 = ObjectWrapper.Basic(mapOf("name" to "Leo", "isArchived" to true))

        repository.stub {
            onBlocking { getListPages() } doReturn listOf(
                DocumentInfo(
                    id = "123678",
                    obj = obj1,
                    snippet = "Snippet1",
                    hasInboundLinks = false,
                    smartBlockType = SmartBlockType.PAGE
                ),
                DocumentInfo(
                    id = "9876",
                    obj = obj2,
                    snippet = "Snippet2",
                    hasInboundLinks = false,
                    smartBlockType = SmartBlockType.PAGE
                ),
                DocumentInfo(
                    id = "934",
                    obj = obj3,
                    snippet = "Snippet3",
                    hasInboundLinks = false,
                    smartBlockType = SmartBlockType.PAGE
                )
            )
        }

        runBlocking {

            getListPages.invoke(Unit).proceed(
                failure = {},
                success = { response: GetListPages.Response ->
                    assertEquals(
                        expected = listOf(
                            DocumentInfo(
                                id = "123678",
                                obj = obj1,
                                snippet = "Snippet1",
                                hasInboundLinks = false,
                                smartBlockType = SmartBlockType.PAGE
                            ),
                            DocumentInfo(
                                id = "9876",
                                obj = obj2,
                                snippet = "Snippet2",
                                hasInboundLinks = false,
                                smartBlockType = SmartBlockType.PAGE
                            )
                        ),
                        actual = response.listPages
                    )
                })
        }
    }
}