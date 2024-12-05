package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.CreateBlockLinkWithObjectResult
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.util.dispatchers
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

class CreateBlockLinkWithObjectTest {

    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    lateinit var createBlockLinkWithObject: CreateBlockLinkWithObject

    @Before
    fun setup() {
        createBlockLinkWithObject = CreateBlockLinkWithObject(repo, dispatchers)
    }

    @Test
    fun `when type without template - should send proper params`() = runBlocking {

        //SETUP
        val context = MockDataFactory.randomString()
        val target = MockDataFactory.randomString()
        val spaceId = MockDataFactory.randomUuid()
        val position = Position.LEFT
        val typeId = MockDataFactory.randomString()
        val type = MockDataFactory.randomString()
        stubCreateBlockLinkWithObject()

        //TESTING
        val params = CreateBlockLinkWithObject.Params(
            context = context,
            target = target,
            position = position,
            typeKey = TypeKey(type),
            typeId = TypeId(typeId),
            space = spaceId,
            template = null
        )
        createBlockLinkWithObject.run(params)

        //ASSERT
        val commands = Command.CreateBlockLinkWithObject(
            context = context,
            target = target,
            position = position,
            prefilled = emptyMap(),
            template = null,
            internalFlags = listOf(InternalFlags.ShouldSelectTemplate),
            type = TypeKey(type),
            space = spaceId
        )
        verifyBlocking(repo, times(1)) { createBlockLinkWithObject(commands) }
    }

    @Test
    fun `when type with one template - should send proper params`() = runBlocking {

        //SETUP
        val context = MockDataFactory.randomString()
        val target = MockDataFactory.randomString()
        val position = Position.LEFT
        val spaceId = MockDataFactory.randomUuid()
        val type = MockDataFactory.randomString()
        val typeId = MockDataFactory.randomString()
        val template = MockDataFactory.randomString()
        stubCreateBlockLinkWithObject()

        //TESTING
        val params = CreateBlockLinkWithObject.Params(
            context = context,
            target = target,
            position = position,
            typeKey = TypeKey(type),
            typeId = TypeId(typeId),
            space = spaceId,
            template = template
        )
        createBlockLinkWithObject.run(params)

        //ASSERT
        val commands = Command.CreateBlockLinkWithObject(
            context = context,
            target = target,
            position = position,
            prefilled = emptyMap(),
            template = template,
            internalFlags = listOf(InternalFlags.ShouldSelectTemplate),
            type = TypeKey(type),
            space = spaceId
        )
        verifyBlocking(repo, times(1)) { createBlockLinkWithObject(commands) }
    }

    @Test
    fun `when type with two templates - should send proper params`() = runBlocking {

        //SETUP
        val context = MockDataFactory.randomString()
        val target = MockDataFactory.randomString()
        val position = Position.LEFT
        val spaceId = MockDataFactory.randomUuid()
        val type = MockDataFactory.randomString()
        val typeId = MockDataFactory.randomString()
        val templateOne = MockDataFactory.randomString()
        stubCreateBlockLinkWithObject()

        //TESTING
        val params = CreateBlockLinkWithObject.Params(
            context = context,
            target = target,
            position = position,
            typeKey = TypeKey(type),
            typeId = TypeId(typeId),
            space = spaceId,
            template = templateOne
        )
        createBlockLinkWithObject.run(params)

        //ASSERT
        val commands = Command.CreateBlockLinkWithObject(
            context = context,
            target = target,
            position = position,
            template = templateOne,
            internalFlags = listOf(InternalFlags.ShouldSelectTemplate),
            type = TypeKey(type),
            prefilled = emptyMap(),
            space = spaceId
        )
        verifyBlocking(repo, times(1)) { createBlockLinkWithObject(commands) }
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