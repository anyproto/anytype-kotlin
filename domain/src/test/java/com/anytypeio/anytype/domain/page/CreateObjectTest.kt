package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.CreateObjectResult
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.util.dispatchers
import com.anytypeio.anytype.domain.workspace.SpaceManager
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

class CreateObjectTest {

    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var getDefaultPageType: GetDefaultPageType

    @Mock
    lateinit var getTemplates: GetTemplates

    @Mock
    lateinit var spaceManager: SpaceManager

    lateinit var createObject: CreateObject

    @Before
    fun setup() {
        createObject = CreateObject(
            repo = repo,
            getDefaultPageType = getDefaultPageType,
            spaceManager = spaceManager,
            dispatchers = dispatchers
        )
        stubSpaceManager()
    }

    @Test
    fun `when type is null and default type not null - should send proper params`() = runBlocking {

        //SETUP
        val type = null
        val appDefaultTypeKey = TypeKey(MockDataFactory.randomString())
        givenGetDefaultObjectType(type = appDefaultTypeKey)
        stubCreateObject()

        //TESTING
        val params = CreateObject.Param(
            type = type,
            internalFlags = listOf(
                InternalFlags.ShouldSelectType,
                InternalFlags.ShouldSelectTemplate,
                InternalFlags.ShouldEmptyDelete
            )
        )
        createObject.run(params)

        //ASSERT
        verifyNoInteractions(getTemplates)
        val commands = Command.CreateObject(
            prefilled = emptyMap(),
            template = null,
            internalFlags = listOf(
                InternalFlags.ShouldSelectType,
                InternalFlags.ShouldSelectTemplate,
                InternalFlags.ShouldEmptyDelete
            ),
            space = SpaceId(""),
            typeKey = appDefaultTypeKey
        )
        verifyBlocking(repo, times(1)) { createObject(commands) }
    }

    @Test
    fun `when type is null and default type is note - should send proper params`() =
        runBlocking {

            //SETUP
            val type = null
            val defaultType = MockDataFactory.randomString()
            givenGetDefaultObjectType(
                type = TypeKey(defaultType)
            )
            givenGetTemplates()
            stubCreateObject()

            //TESTING
            val params = CreateObject.Param(
                type = type,
                internalFlags = listOf(
                    InternalFlags.ShouldSelectType,
                    InternalFlags.ShouldSelectTemplate,
                    InternalFlags.ShouldEmptyDelete
                )
            )
            createObject.run(params)

            //ASSERT
            val commands = Command.CreateObject(
                prefilled = emptyMap(),
                template = null,
                internalFlags = listOf(
                    InternalFlags.ShouldSelectType,
                    InternalFlags.ShouldSelectTemplate,
                    InternalFlags.ShouldEmptyDelete
                ),
                space = SpaceId(""),
                typeKey = TypeKey(defaultType)
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
            givenGetDefaultObjectType(
                type = TypeKey(defaultType)
            )
            stubCreateObject()

            //TESTING
            val params = CreateObject.Param(
                type = type,
                internalFlags = listOf(
                    InternalFlags.ShouldSelectType,
                    InternalFlags.ShouldSelectTemplate,
                    InternalFlags.ShouldEmptyDelete
                )
            )
            createObject.run(params)

            //ASSERT
            val commands = Command.CreateObject(
                prefilled = emptyMap(),
                template = null,
                internalFlags = listOf(
                    InternalFlags.ShouldSelectType,
                    InternalFlags.ShouldSelectTemplate,
                    InternalFlags.ShouldEmptyDelete
                ),
                space = SpaceId(""),
                typeKey = TypeKey(defaultType)
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
            val params = CreateObject.Param(
                type = TypeKey(type),
                internalFlags = listOf(
                    InternalFlags.ShouldSelectType,
                    InternalFlags.ShouldSelectTemplate,
                    InternalFlags.ShouldEmptyDelete
                )
            )
            createObject.run(params)

            //ASSERT
            verifyNoInteractions(getDefaultPageType)
            val commands = Command.CreateObject(
                prefilled = emptyMap(),
                template = null,
                internalFlags = listOf(
                    InternalFlags.ShouldSelectType,
                    InternalFlags.ShouldSelectTemplate,
                    InternalFlags.ShouldEmptyDelete
                ),
                space = SpaceId(""),
                typeKey = TypeKey(type)
            )
            verifyBlocking(repo, times(1)) { createObject(commands) }
        }

    @Test
    fun `when type is custom with one template - should send proper params`() =
        runBlocking {

            //SETUP
            val type = MockDataFactory.randomString()
            val template = MockDataFactory.randomString()
            stubCreateObject()

            //TESTING
            val params = CreateObject.Param(
                type = TypeKey(type),
                internalFlags = listOf(
                    InternalFlags.ShouldSelectType,
                    InternalFlags.ShouldSelectTemplate,
                    InternalFlags.ShouldEmptyDelete
                )
            )
            createObject.run(params)

            //ASSERT
            verifyNoInteractions(getDefaultPageType)
            val commands = Command.CreateObject(
                prefilled = emptyMap(),
                template = null,
                internalFlags = listOf(
                    InternalFlags.ShouldSelectType,
                    InternalFlags.ShouldSelectTemplate,
                    InternalFlags.ShouldEmptyDelete
                ),
                space = SpaceId(""),
                typeKey = TypeKey(type)
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
            stubCreateObject()

            //TESTING
            val params = CreateObject.Param(
                type = TypeKey(type),
                internalFlags = listOf(
                    InternalFlags.ShouldSelectType,
                    InternalFlags.ShouldSelectTemplate,
                    InternalFlags.ShouldEmptyDelete
                ),
                template = templateTwo
            )
            createObject.run(params)

            //ASSERT
            verifyNoInteractions(getDefaultPageType)
            val commands = Command.CreateObject(
                prefilled = emptyMap(),
                template = templateTwo,
                internalFlags = listOf(
                    InternalFlags.ShouldSelectType,
                    InternalFlags.ShouldSelectTemplate,
                    InternalFlags.ShouldEmptyDelete
                ),
                space = SpaceId(""),
                typeKey = TypeKey(type)
            )
            verifyBlocking(repo, times(1)) { createObject(commands) }
        }

    private fun givenGetDefaultObjectType(
        type: TypeKey,
        name: String? = null,
        template: String? = null,
    ) {
        getDefaultPageType.stub {
            onBlocking { run(Unit) } doReturn GetDefaultPageType.Response(
                type = type,
                name = name,
                id = TypeId(MockDataFactory.randomString()),
                defaultTemplate = template
            )
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
                details = emptyMap()
            )
        }
    }

    fun stubSpaceManager() {
        spaceManager.stub {
            onBlocking { get() } doReturn ""
        }
    }
}