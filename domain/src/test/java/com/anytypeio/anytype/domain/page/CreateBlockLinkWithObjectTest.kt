package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.CreateBlockLinkWithObjectResult
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.block.repo.BlockRepository
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

class CreateBlockLinkWithObjectTest {

    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var getTemplates: GetTemplates

    lateinit var createBlockLinkWithObject: CreateBlockLinkWithObject

    @Before
    fun setup() {
        createBlockLinkWithObject = CreateBlockLinkWithObject(repo, getTemplates)
    }

    @Test
    fun `when type without template - should send proper params`() = runBlocking {

        //SETUP
        val context = MockDataFactory.randomString()
        val target = MockDataFactory.randomString()
        val position = Position.LEFT
        val type = MockDataFactory.randomString()
        val name = MockDataFactory.randomString()
        stubCreateBlockLinkWithObject()
        givenGetTemplates(listOf())

        //TESTING
        val params = CreateBlockLinkWithObject.Params(
            context = context,
            target = target,
            position = position,
            type = type
        )
        createBlockLinkWithObject.run(params)

        //ASSERT
        val commands = Command.CreateBlockLinkWithObject(
            context = context,
            target = target,
            position = position,
            prefilled = buildMap {
                put(Relations.TYPE, type)
            },
            template = null,
            internalFlags = listOf()
        )
        verifyBlocking(repo, times(1)) { createBlockLinkWithObject(commands) }
    }

    @Test
    fun `when type with one template - should send proper params`() = runBlocking {

        //SETUP
        val context = MockDataFactory.randomString()
        val target = MockDataFactory.randomString()
        val position = Position.LEFT
        val type = MockDataFactory.randomString()
        val template = MockDataFactory.randomString()
        stubCreateBlockLinkWithObject()
        givenGetTemplates(listOf(ObjectWrapper.Basic(buildMap {
            put(Relations.ID, template)
        })))

        //TESTING
        val params = CreateBlockLinkWithObject.Params(
            context = context,
            target = target,
            position = position,
            type = type
        )
        createBlockLinkWithObject.run(params)

        //ASSERT
        val commands = Command.CreateBlockLinkWithObject(
            context = context,
            target = target,
            position = position,
            prefilled = buildMap {
                put(Relations.TYPE, type)
            },
            template = template,
            internalFlags = listOf()
        )
        verifyBlocking(repo, times(1)) { createBlockLinkWithObject(commands) }
    }

    @Test
    fun `when type with two templates - should send proper params`() = runBlocking {

        //SETUP
        val context = MockDataFactory.randomString()
        val target = MockDataFactory.randomString()
        val position = Position.LEFT
        val type = MockDataFactory.randomString()
        val templateOne = MockDataFactory.randomString()
        val templateTwo = MockDataFactory.randomString()
        stubCreateBlockLinkWithObject()
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

        //TESTING
        val params = CreateBlockLinkWithObject.Params(
            context = context,
            target = target,
            position = position,
            type = type
        )
        createBlockLinkWithObject.run(params)

        //ASSERT
        val commands = Command.CreateBlockLinkWithObject(
            context = context,
            target = target,
            position = position,
            prefilled = buildMap {
                put(Relations.TYPE, type)
            },
            template = null,
            internalFlags = listOf()
        )
        verifyBlocking(repo, times(1)) { createBlockLinkWithObject(commands) }
    }

    private fun givenGetTemplates(objects: List<ObjectWrapper.Basic> = listOf()) {
        getTemplates.stub {
            onBlocking { run(any()) } doReturn objects
        }
    }

    private fun stubCreateBlockLinkWithObject() {
        repo.stub {
            onBlocking { createBlockLinkWithObject(any()) } doReturn CreateBlockLinkWithObjectResult(
                blockId = "",
                objectId = "",
                event = Payload(context = "", events = listOf())
            )
        }
    }
}