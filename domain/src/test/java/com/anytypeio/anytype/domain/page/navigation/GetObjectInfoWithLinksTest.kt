package com.anytypeio.anytype.domain.page.navigation

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.MockDataFactory
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class GetObjectInfoWithLinksTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()
    lateinit var getObjectInfoWithLinks: GetObjectInfoWithLinks

    @Mock
    lateinit var repository: BlockRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        getObjectInfoWithLinks = GetObjectInfoWithLinks(repository)
    }

    @Test
    fun `should filter outbound and inbound links by archived pages`() {

        val pageId = MockDataFactory.randomUuid()

        val obj1 = ObjectWrapper.Basic(mapOf("name" to "Alex"))
        val obj2 = ObjectWrapper.Basic(mapOf("name" to "Mike", "isArchived" to false))
        val obj3 = ObjectWrapper.Basic(mapOf("name" to "Leo", "isArchived" to true))
        val obj4 = ObjectWrapper.Basic(mapOf("name" to "Teo"))
        val obj5 = ObjectWrapper.Basic(mapOf("name" to "Thom", "isArchived" to false))
        val obj6 = ObjectWrapper.Basic(mapOf("name" to "Andrey", "isArchived" to true))

        repository.stub {
            onBlocking { getObjectInfoWithLinks(pageId) } doReturn ObjectInfoWithLinks(
                id = pageId,
                documentInfo = DocumentInfo(
                    id = pageId,
                    snippet = "Snip",
                    hasInboundLinks = true,
                    smartBlockType = SmartBlockType.PAGE,
                    obj = ObjectWrapper.Basic(emptyMap()),
                ),
                links = ObjectLinks(
                    inbound = listOf(
                        DocumentInfo(
                            id = "12",
                            obj = obj1,
                            snippet = "Snippet12",
                            hasInboundLinks = false,
                            smartBlockType = SmartBlockType.PAGE
                        ),
                        DocumentInfo(
                            id = "13",
                            obj = obj2,
                            snippet = "Snippet13",
                            hasInboundLinks = false,
                            smartBlockType = SmartBlockType.PAGE
                        ),
                        DocumentInfo(
                            id = "14",
                            obj = obj3,
                            snippet = "Snippet14",
                            hasInboundLinks = false,
                            smartBlockType = SmartBlockType.PAGE
                        )
                    ),
                    outbound = listOf(
                        DocumentInfo(
                            id = "15",
                            obj = obj4,
                            snippet = "Snippet15",
                            hasInboundLinks = false,
                            smartBlockType = SmartBlockType.PAGE
                        ),
                        DocumentInfo(
                            id = "16",
                            obj = obj5,
                            snippet = "Snippet16",
                            hasInboundLinks = false,
                            smartBlockType = SmartBlockType.PAGE
                        ),
                        DocumentInfo(
                            id = "17",
                            obj = obj6,
                            snippet = "Snippet17",
                            hasInboundLinks = false,
                            smartBlockType = SmartBlockType.PAGE
                        )
                    )
                )
            )
        }

        runBlocking {

            getObjectInfoWithLinks(GetObjectInfoWithLinks.Params(pageId = pageId)).proceed(
                failure = {},
                success = { response ->
                    val outbound = response.pageInfoWithLinks.links.outbound
                    val inbound = response.pageInfoWithLinks.links.inbound
                    assertEquals(
                        expected = listOf(
                            DocumentInfo(
                                id = "15",
                                obj = obj4,
                                snippet = "Snippet15",
                                hasInboundLinks = false,
                                smartBlockType = SmartBlockType.PAGE
                            ),
                            DocumentInfo(
                                id = "16",
                                obj = obj5,
                                snippet = "Snippet16",
                                hasInboundLinks = false,
                                smartBlockType = SmartBlockType.PAGE
                            )
                        ),
                        actual = outbound
                    )
                    assertEquals(
                        expected = listOf(
                            DocumentInfo(
                                id = "12",
                                obj = obj1,
                                snippet = "Snippet12",
                                hasInboundLinks = false,
                                smartBlockType = SmartBlockType.PAGE
                            ),
                            DocumentInfo(
                                id = "13",
                                obj = obj2,
                                snippet = "Snippet13",
                                hasInboundLinks = false,
                                smartBlockType = SmartBlockType.PAGE
                            )
                        ),
                        actual = inbound
                    )
                }
            )
        }
    }
}