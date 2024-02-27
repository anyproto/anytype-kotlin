package com.anytypeio.anytype.domain.launch

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.NO_VALUE
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.mapToObjectWrapperType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject

class GetDefaultObjectType @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val blockRepository: BlockRepository,
    private val spaceManager: SpaceManager,
    private val configStorage: ConfigStorage,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, GetDefaultObjectType.Response>(dispatchers.io) {

    override suspend fun doWork(params: Unit): Response {
        val space = SpaceId(spaceManager.get())
        val defaultType = userSettingsRepository.getDefaultObjectType(space)
        if (defaultType != null) {
            val item = searchObjectByIdAndSpaceId(
                type = defaultType,
                space = space
            )
            return if (item != null) {
                val key = TypeKey(item.uniqueKey)
                val id = TypeId(item.id)
                Response(
                    type = key,
                    name = item.name,
                    id = id,
                    defaultTemplate = item.defaultTemplateId
                )
            } else {
                fetchDefaultType()
            }
        } else {
            return fetchDefaultType()
        }
    }

    private suspend fun fetchDefaultType(): Response {
        val structs = blockRepository.searchObjects(
            limit = 1,
            fulltext = NO_VALUE,
            filters = buildList {
                add(
                    DVFilter(
                        relation = Relations.UNIQUE_KEY,
                        condition = DVFilterCondition.EQUAL,
                        value = ObjectTypeUniqueKeys.NOTE
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
                Relations.SPACE_ID,
                Relations.DEFAULT_TEMPLATE_ID
            )
        )
        val objType = structs.firstOrNull()?.mapToObjectWrapperType()
        if (objType != null) {
            val key = TypeKey(objType.uniqueKey)
            val id = TypeId(objType.id)
            return Response(
                type = key,
                name = objType.name,
                id = id,
                defaultTemplate = objType.defaultTemplateId
            )
        } else {
            throw IllegalStateException("Default type not found")
        }
    }

    private suspend fun searchObjectByIdAndSpaceId(
        type: TypeId,
        space: SpaceId
    ): ObjectWrapper.Type? {
        val structs = blockRepository.searchObjects(
            limit = 1,
            fulltext = NO_VALUE,
            filters = buildList {
                addAll(filterObjectTypeLibrary(space))
                add(
                    DVFilter(
                        relation = Relations.ID,
                        condition = DVFilterCondition.EQUAL,
                        value = type.id
                    )
                )
            },
            offset = 0,
            sorts = emptyList(),
            keys = listOf(
                Relations.ID,
                Relations.NAME,
                Relations.UNIQUE_KEY,
                Relations.SPACE_ID,
                Relations.DEFAULT_TEMPLATE_ID
            )
        )
        return structs.firstOrNull()?.mapToObjectWrapperType()
    }

    private fun filterObjectTypeLibrary(space: SpaceId) = listOf(
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
            value = space.id
        ),
        DVFilter(
            relation = Relations.RESTRICTIONS,
            condition = DVFilterCondition.NOT_IN,
            value = listOf(ObjectRestriction.CREATE_OBJECT_OF_THIS_TYPE.code.toDouble())
        )
    )

    /**
     * TODO provide space to params
     */
    data class Response(
        val id: TypeId,
        val type: TypeKey,
        val name: String?,
        val defaultTemplate: Id?
    )
}