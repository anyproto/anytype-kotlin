package com.agileburo.anytype.domain.page.navigation

import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.CoroutineTestRule
import com.agileburo.anytype.domain.common.MockDataFactory
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

class GetPageInfoWithLinksTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()
    lateinit var getPageInfoWithLinks: GetPageInfoWithLinks

    @Mock
    lateinit var repository: BlockRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        getPageInfoWithLinks = GetPageInfoWithLinks(repository)
    }

    @Test
    fun `should filter outbound and inbound links by archived pages`() {

        val pageId = MockDataFactory.randomUuid()

        repository.stub {
            onBlocking { getPageInfoWithLinks(pageId) } doReturn PageInfoWithLinks(
                id = pageId,
                documentInfo = DocumentInfo(
                    id = pageId,
                    fields = Block.Fields.empty(),
                    snippet = "Snip",
                    hasInboundLinks = true
                ),
                links = PageLinks(
                    inbound = listOf(
                        DocumentInfo(
                            id = "12",
                            fields = Block.Fields(mapOf("name" to "Alex")),
                            snippet = "Snippet12",
                            hasInboundLinks = false
                        ),
                        DocumentInfo(
                            id = "13",
                            fields = Block.Fields(mapOf("name" to "Mike", "isArchived" to false)),
                            snippet = "Snippet13",
                            hasInboundLinks = false
                        ),
                        DocumentInfo(
                            id = "14",
                            fields = Block.Fields(mapOf("name" to "Leo", "isArchived" to true)),
                            snippet = "Snippet14",
                            hasInboundLinks = false
                        )
                    ),
                    outbound = listOf(
                        DocumentInfo(
                            id = "15",
                            fields = Block.Fields(mapOf("name" to "Teo")),
                            snippet = "Snippet15",
                            hasInboundLinks = false
                        ),
                        DocumentInfo(
                            id = "16",
                            fields = Block.Fields(mapOf("name" to "Thom", "isArchived" to false)),
                            snippet = "Snippet16",
                            hasInboundLinks = false
                        ),
                        DocumentInfo(
                            id = "17",
                            fields = Block.Fields(mapOf("name" to "Andrey", "isArchived" to true)),
                            snippet = "Snippet17",
                            hasInboundLinks = false
                        )
                    )
                )
            )
        }

        runBlocking {

            getPageInfoWithLinks(GetPageInfoWithLinks.Params(pageId = pageId)).proceed(
                failure = {},
                success = { response ->
                    val outbound = response.pageInfoWithLinks.links.outbound
                    val inbound = response.pageInfoWithLinks.links.inbound
                    assertEquals(
                        expected = listOf(
                            DocumentInfo(
                                id = "15",
                                fields = Block.Fields(mapOf("name" to "Teo")),
                                snippet = "Snippet15",
                                hasInboundLinks = false
                            ),
                            DocumentInfo(
                                id = "16",
                                fields = Block.Fields(
                                    mapOf(
                                        "name" to "Thom",
                                        "isArchived" to false
                                    )
                                ),
                                snippet = "Snippet16",
                                hasInboundLinks = false
                            )
                        ),
                        actual = outbound
                    )
                    assertEquals(
                        expected = listOf(
                            DocumentInfo(
                                id = "12",
                                fields = Block.Fields(mapOf("name" to "Alex")),
                                snippet = "Snippet12",
                                hasInboundLinks = false
                            ),
                            DocumentInfo(
                                id = "13",
                                fields = Block.Fields(
                                    mapOf(
                                        "name" to "Mike",
                                        "isArchived" to false
                                    )
                                ),
                                snippet = "Snippet13",
                                hasInboundLinks = false
                            )
                        ),
                        actual = inbound
                    )
                }
            )
        }
    }
}