package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyBlocking
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit

class CreateNewObjectTest {

    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var createPage: CreatePage

    @Mock
    lateinit var getDefaultEditorType: GetDefaultEditorType

    @Mock
    lateinit var getTemplates: GetTemplates

    @Test
    fun `should start creating page`() {
        givenCreatePage()
        givenGetDefaultObjectType()

        runBlocking { givenCreateNewObject().run(any()) }

        verifyBlocking(createPage, times(1)) { run(any()) }
    }

    @Test
    fun `should create new object with null template and isDraft true params`() {
        val type = MockDataFactory.randomString()

        givenCreatePage()
        givenGetTemplates()
        givenGetDefaultObjectType(type = type)

        runBlocking { givenCreateNewObject().run(Unit) }

        val params = CreatePage.Params(
            ctx = null,
            type = type,
            emoji = null,
            isDraft = true,
            template = null
        )

        verifyBlocking(createPage, times(1)) { run(params) }
    }

    @Test
    fun `should create new object with non nullable template and isDraft false params`() {

        val templateId = MockDataFactory.randomUuid()
        val type = MockDataFactory.randomString()
        val obj = ObjectWrapper.Basic(mapOf("id" to templateId))

        givenCreatePage()
        givenGetTemplates(objects = listOf(obj))
        givenGetDefaultObjectType(type = type)

        runBlocking { givenCreateNewObject().run(Unit) }

        val params = CreatePage.Params(
            ctx = null,
            type = type,
            emoji = null,
            isDraft = false,
            template = templateId
        )

        verifyBlocking(createPage, times(1)) { run(params) }
    }

    private fun givenCreatePage(id: String = "") {
        createPage.stub {
            onBlocking { run(any()) } doReturn id
        }
    }

    private fun givenGetDefaultObjectType(type: String? = null, name: String? = null) {
        getDefaultEditorType.stub {
            onBlocking { invoke(Unit) } doReturn flow {
                emit(
                    GetDefaultEditorType.Response(
                        type,
                        name
                    )
                )
            }
        }
    }

    private fun givenGetTemplates(objects: List<ObjectWrapper.Basic> = listOf()) {
        getTemplates.stub {
            onBlocking { run(any()) } doReturn objects
        }
    }

    private fun givenCreateNewObject() = CreateNewObject(
        getDefaultEditorType, getTemplates, createPage
    )
}