package com.anytypeio.anytype.domain.launch

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.NO_VALUE
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject

class GetDefaultPageType @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val blockRepository: BlockRepository,
    private val spaceManager: SpaceManager,
    private val configStorage: ConfigStorage,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, GetDefaultPageType.Response>(dispatchers.io) {

    override suspend fun doWork(params: Unit): Response {
        val space = SpaceId(spaceManager.get())
        userSettingsRepository.getDefaultObjectType(space)?.let { type ->
            val item = searchObjectByIdAndSpaceId(
                id = type.id,
                space = space.id
            )
            if (item != null) {
                val key = item.uniqueKey?.let {
                    TypeKey(it)
                }
                return Response(
                    type = key,
                    name =item.name
                )
            } else {
                return searchNote()
            }
        } ?: run {
            return searchNote()
        }
    }

    private suspend fun searchNote(): Response {
        val items = blockRepository.searchObjects(
            limit = 1,
            fulltext = NO_VALUE,
            filters = buildList {
                add(
                    DVFilter(
                        relation = Relations.UNIQUE_KEY,
                        condition = DVFilterCondition.EQUAL,
                        value = ObjectTypeIds.NOTE
                    )
                )
                val space = spaceManager.get().ifEmpty {
                    // Fallback to default space.
                    configStorage.getOrNull()?.space
                }
                if (!space.isNullOrEmpty()) {
                    add(
                        DVFilter(
                            relation = Relations.SPACE_ID,
                            condition = DVFilterCondition.EQUAL,
                            value = space
                        )
                    )
                }
            },
            offset = 0,
            sorts = emptyList(),
            keys = listOf(
                Relations.ID,
                Relations.NAME,
                Relations.UNIQUE_KEY,
                Relations.SPACE_ID
            )
        )
        val note = if (items.isNotEmpty()) {
            ObjectWrapper.Type(items.first())
        } else {
            null
        }
        val key = note?.uniqueKey?.let {
            TypeKey(it)
        }
        return Response(
            type = key,
            name = note?.name
        )
    }

    private suspend fun searchObjectByIdAndSpaceId(
        id: String,
        space: Id
    ): ObjectWrapper.Type? {
        val items = blockRepository.searchObjects(
            limit = 1,
            fulltext = NO_VALUE,
            filters = buildList {
                addAll(filterObjectTypeLibrary(space))
                add(
                    DVFilter(
                        relation = Relations.ID,
                        condition = DVFilterCondition.EQUAL,
                        value = id
                    )
                )
            },
            offset = 0,
            sorts = emptyList(),
            keys = listOf(
                Relations.ID,
                Relations.NAME,
                Relations.UNIQUE_KEY,
                Relations.SPACE_ID
            )
        )
        return if (items.isNotEmpty()) {
            ObjectWrapper.Type(items.first())
        } else {
            null
        }
    }

    class Response(val type: TypeKey?, val name: String?)

    private fun filterObjectTypeLibrary(space: Id) = listOf(
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.EQUAL,
            value = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
        ),
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_DELETED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = space
        )
    )
}