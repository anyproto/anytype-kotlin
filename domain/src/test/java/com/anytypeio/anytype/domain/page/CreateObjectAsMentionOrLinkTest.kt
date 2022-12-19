package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.CreateObjectResult
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions

class CreateObjectAsMentionOrLinkTest {

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

    lateinit var createObjectAsMentionOrLink: CreateObjectAsMentionOrLink

    @Before
    fun setup() {
        createObjectAsMentionOrLink =
            CreateObjectAsMentionOrLink(repo, getDefaultEditorType, getTemplates)
    }

    @Test
    fun `when type is null and default type is null - should send proper params`() = runBlocking {

        //SETUP
        val type = null
        val name = MockDataFactory.randomString()
        givenGetDefaultObjectType()
        stubCreateObject()

        //TESTING
        val params = CreateObjectAsMentionOrLink.Params(
            name = name,
            type = type
        )
        createObjectAsMentionOrLink.run(params)

        //ASSERT
        verifyNoInteractions(getTemplates)
        val commands = Command.CreateObject(
            prefilled = buildMap {
                put(Relations.NAME, name)
            },
            template = null,
            internalFlags = listOf()
        )
        verifyBlocking(repo, times(1)) { createObject(commands) }
    }

    @Test
    fun `when type is null and default type is not null without template - should send proper params`() =
        runBlocking {

            //SETUP
            val type = null
            val name = MockDataFactory.randomString()
            val typeDefault = MockDataFactory.randomString()
            val typeDefaultName = MockDataFactory.randomString()
            givenGetDefaultObjectType(type = typeDefault, name = typeDefaultName)
            givenGetTemplates()
            stubCreateObject()

            //TESTING
            val params = CreateObjectAsMentionOrLink.Params(
                name = name,
                type = type
            )
            createObjectAsMentionOrLink.run(params)

            //ASSERT
            val commands = Command.CreateObject(
                prefilled = buildMap {
                    put(Relations.TYPE, typeDefault)
                    put(Relations.NAME, name)
                },
                template = null,
                internalFlags = listOf()
            )
            verifyBlocking(repo, times(1)) { createObject(commands) }
        }

    @Test
    fun `when type is null and default type is not null with one template - should send proper params`() =
        runBlocking {

            //SETUP
            val type = null
            val name = MockDataFactory.randomString()
            val typeDefault = MockDataFactory.randomString()
            val typeDefaultName = MockDataFactory.randomString()
            val template = MockDataFactory.randomString()
            givenGetDefaultObjectType(type = typeDefault, name = typeDefaultName)
            givenGetTemplates(listOf(ObjectWrapper.Basic(buildMap {
                put(Relations.ID, template)
            })))
            stubCreateObject()

            //TESTING
            val params = CreateObjectAsMentionOrLink.Params(
                name = name,
                type = type
            )
            createObjectAsMentionOrLink.run(params)

            //ASSERT
            val commands = Command.CreateObject(
                prefilled = buildMap {
                    put(Relations.TYPE, typeDefault)
                    put(Relations.NAME, name)
                },
                template = template,
                internalFlags = listOf()
            )
            verifyBlocking(repo, times(1)) { createObject(commands) }
        }

    @Test
    fun `when type is null and default type is not null with two templates - should send proper params`() =
        runBlocking {

            //SETUP
            val type = null
            val name = MockDataFactory.randomString()
            val typeDefault = MockDataFactory.randomString()
            val typeDefaultName = MockDataFactory.randomString()
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
            givenGetDefaultObjectType(type = typeDefault, name = typeDefaultName)
            stubCreateObject()

            //TESTING
            val params = CreateObjectAsMentionOrLink.Params(
                name = name,
                type = type
            )
            createObjectAsMentionOrLink.run(params)

            //ASSERT
            val commands = Command.CreateObject(
                prefilled = buildMap {
                    put(Relations.TYPE, typeDefault)
                    put(Relations.NAME, name)
                },
                template = null,
                internalFlags = listOf()
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