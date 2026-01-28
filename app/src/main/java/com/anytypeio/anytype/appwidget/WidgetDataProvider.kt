package com.anytypeio.anytype.appwidget

import com.anytypeio.anytype.core_models.Block.Content.DataView.Sort
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.mapToObjectWrapperType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber


class WidgetDataProvider @Inject constructor(
    private val spaceManager: SpaceManager,
    private val blockRepository: BlockRepository,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val userSettingsRepository: UserSettingsRepository,
    private val urlBuilder: UrlBuilder,
    private val userPermissionProvider: UserPermissionProvider,
    private val stringResourceProvider: StringResourceProvider,
    private val fieldParser: FieldParser
) {

    suspend fun getTasks(): List<TaskWidgetView> = withContext(Dispatchers.IO) {
        val cachedSpaceId = userSettingsRepository.getCurrentSpace() ?: return@withContext emptyList()

        val taskObjectType = storeOfObjectTypes.getAll().find { it.uniqueKey == ObjectTypeIds.TASK }
            ?: return@withContext emptyList()

        val tasks = try {
            blockRepository.searchObjects(
                space = cachedSpaceId,
                filters = listOf(
                    DVFilter(
                        relation = Relations.TYPE,
                        value = taskObjectType.id,
                        condition = DVFilterCondition.EQUAL
                    )
                ),
                limit = 50
            )
        } catch (e: Exception) {
            emptyList()
        }

        return@withContext tasks.map { map ->
            val wrapper = ObjectWrapper.Basic(map)
            wrapper.toTaskWidgetView()
        }

    }

}