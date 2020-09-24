package com.anytypeio.anytype.domain.database.interactor

import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.common.CoroutineTestRule
import com.anytypeio.anytype.domain.database.model.ContentDatabaseView
import com.anytypeio.anytype.domain.database.model.DatabaseView
import com.anytypeio.anytype.domain.database.model.Detail
import com.anytypeio.anytype.domain.database.repo.DatabaseRepository
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

class HideDetailTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var databaseRepo: DatabaseRepository
    lateinit var hideDetail: HideDetail

    private val db = DatabaseView(
        content = ContentDatabaseView(
            databaseId = "213",
            displays = mutableListOf(),
            details = listOf(
                Detail.Date(id = "423", show = true, name = "Birthday"),
                Detail.Email(id = "544", show = true, name = "Cowokers email"),
                Detail.Text(id = "888", show = true, name = "Surnames")
            ),
            data = listOf()
        )
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        hideDetail = HideDetail(databaseRepo)
    }

    @Test
    fun `should hide detail with id`() = runBlocking {
        val databaseId = "213"
        val detailId = "423"

        databaseRepo.stub {
            onBlocking { getDatabase(databaseId) } doReturn db
        }

        val result =
            hideDetail.run(HideDetail.Params(databaseId = databaseId, detailId = detailId))

        assertEquals(Either.Right(Unit), result)
    }
}