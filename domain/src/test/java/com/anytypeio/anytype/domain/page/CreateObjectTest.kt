package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.CreateObjectResult
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyBlocking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.verifyNoInteractions

class CreateObjectTest {

    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var getDefaultEditorType: GetDefaultEditorType

    @Mock
    lateinit var getTemplates: GetTemplates

    lateinit var createObject: CreateObject

    @Before
    fun setup() {
        createObject = CreateObject(repo, getDefaultEditorType, getTemplates)
    }

    @Test
    fun `when type is null and default type is null - should send proper params`() = runBlocking {

        //SETUP
        val type = null
        givenGetDefaultObjectType()
        stubCreateObject()

        //TESTING
        val params = CreateObject.Param(type)
        createObject.run(params)

        //ASSERT
        verifyNoInteractions(getTemplates)
        val commands = Command.CreateObject(
            prefilled = emptyMap(),
            template = null,
            internalFlags = listOf(InternalFlags.ShouldSelectType, InternalFlags.ShouldEmptyDelete)
        )
        verifyBlocking(repo, times(1)) { createObject(commands) }
    }

    @Test
    fun `when type is null and default type is note without template - should send proper params`() =
        runBlocking {

            //SETUP
            val type = null
            val defaultType = MockDataFactory.randomString()
            val defaultTypeName = MockDataFactory.randomString()
            givenGetDefaultObjectType(defaultType, defaultTypeName)
            givenGetTemplates()
            stubCreateObject()

            //TESTING
            val params = CreateObject.Param(type)
            createObject.run(params)

            //ASSERT
            verifyBlocking(getTemplates, times(1)) { run(GetTemplates.Params(defaultType)) }
            val commands = Command.CreateObject(
                prefilled = buildMap { put(Relations.TYPE, defaultType) },
                template = null,
                internalFlags = listOf(
                    InternalFlags.ShouldSelectType,
                    InternalFlags.ShouldEmptyDelete
                )
            )
            verifyBlocking(repo, times(1)) { createObject(commands) }
        }

    @Test
    fun `when type is null and default type is book with template - should send proper params`() =
        runBlocking {

            //SETUP
            val type = null
            val defaultType = MockDataFactory.randomString()
            val defaultTypeName = MockDataFactory.randomString()
            val templateBook = MockDataFactory.randomString()
            givenGetDefaultObjectType(defaultType, defaultTypeName)
            givenGetTemplates(listOf(ObjectWrapper.Basic(buildMap {
                put(Relations.ID, templateBook)
            })))
            stubCreateObject()

            //TESTING
            val params = CreateObject.Param(type)
            createObject.run(params)

            //ASSERT
            verifyBlocking(getTemplates, times(1)) { run(GetTemplates.Params(defaultType)) }
            val commands = Command.CreateObject(
                prefilled = buildMap { put(Relations.TYPE, defaultType) },
                template = templateBook,
                internalFlags = listOf()
            )
            verifyBlocking(repo, times(1)) { createObject(commands) }
        }

    @Test
    fun `when type is custom without template - should send proper params`() =
        runBlocking {

            //SETUP
            val type = MockDataFactory.randomString()
            givenGetTemplates()
            stubCreateObject()

            //TESTING
            val params = CreateObject.Param(type)
            createObject.run(params)

            //ASSERT
            verifyNoInteractions(getDefaultEditorType)
            verifyBlocking(getTemplates, times(1)) { run(GetTemplates.Params(type)) }
            val commands = Command.CreateObject(
                prefilled = buildMap { put(Relations.TYPE, type) },
                template = null,
                internalFlags = listOf(
                    InternalFlags.ShouldSelectType,
                    InternalFlags.ShouldEmptyDelete
                )
            )
            verifyBlocking(repo, times(1)) { createObject(commands) }
        }

    @Test
    fun `when type is custom with one template - should send proper params`() =
        runBlocking {

            //SETUP
            val type = MockDataFactory.randomString()
            val template = MockDataFactory.randomString()
            givenGetTemplates(listOf(ObjectWrapper.Basic(buildMap {
                put(Relations.ID, template)
            })))
            stubCreateObject()

            //TESTING
            val params = CreateObject.Param(type)
            createObject.run(params)

            //ASSERT
            verifyNoInteractions(getDefaultEditorType)
            verifyBlocking(getTemplates, times(1)) { run(GetTemplates.Params(type)) }
            val commands = Command.CreateObject(
                prefilled = buildMap { put(Relations.TYPE, type) },
                template = template,
                internalFlags = listOf()
            )
            verifyBlocking(repo, times(1)) { createObject(commands) }
        }

    @Test
    fun `when type is custom with two templates - should send proper params`() =
        runBlocking {

            //SETUP
            val type = MockDataFactory.randomString()
            val templateOne = MockDataFactory.randomString()
            val templateTwo = MockDataFactory.randomString()
            givenGetTemplates(
                listOf(
                    ObjectWrapper.Basic(buildMap {
                        put(Relations.ID, templateOne)
                    }),
                    ObjectWrapper.Basic(buildMap {
                        put(Relations.ID, templateTwo)
                    })
                )
            )
            stubCreateObject()

            //TESTING
            val params = CreateObject.Param(type)
            createObject.run(params)

            //ASSERT
            verifyNoInteractions(getDefaultEditorType)
            verifyBlocking(getTemplates, times(1)) { run(GetTemplates.Params(type)) }
            val commands = Command.CreateObject(
                prefilled = buildMap { put(Relations.TYPE, type) },
                template = null,
                internalFlags = listOf(
                    InternalFlags.ShouldSelectType,
                    InternalFlags.ShouldEmptyDelete
                )
            )
            verifyBlocking(repo, times(1)) { createObject(commands) }
        }

    private fun givenGetDefaultObjectType(type: String? = null, name: String? = null) {
        getDefaultEditorType.stub {
            onBlocking { run(Unit) } doReturn GetDefaultEditorType.Response(type, name)
        }
    }

    private fun givenGetTemplates(objects: List<ObjectWrapper.Basic> = listOf()) {
        getTemplates.stub {
            onBlocking { run(any()) } doReturn objects
        }
    }

    private fun stubCreateObject() {
        repo.stub {
            onBlocking { createObject(any()) } doReturn CreateObjectResult(
                id = "",
                event = Payload(context = "", events = listOf()),
                details = null
            )
        }
    }
}