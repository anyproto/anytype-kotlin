package com.anytypeio.anytype.ui.oswidgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.ext.mapToObjectWrapperType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetIconCache
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetObjectShortcutEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import com.anytypeio.anytype.feature_os_widgets.ui.OsObjectShortcutWidgetUpdater
import com.anytypeio.anytype.feature_os_widgets.ui.config.ObjectItemView
import com.anytypeio.anytype.feature_os_widgets.ui.config.ObjectSelectionScreen
import com.anytypeio.anytype.feature_os_widgets.ui.config.SpaceSelectionScreen
import com.anytypeio.anytype.feature_os_widgets.ui.config.WidgetConfigTheme
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Configuration Activity for the Object Shortcut widget.
 * Two-step flow: Select Space -> Select Object (with search)
 */
class ObjectShortcutWidgetConfigActivity : AppCompatActivity() {

    @Inject
    lateinit var spaceViews: SpaceViewSubscriptionContainer

    @Inject
    lateinit var urlBuilder: UrlBuilder

    @Inject
    lateinit var searchObjects: SearchObjects

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var selectedSpace: ObjectWrapper.SpaceView? = null

    // UI State
    private var screenState by mutableStateOf<ScreenState>(ScreenState.SpaceSelection)
    private var objectItems by mutableStateOf<List<ObjectItemView>>(emptyList())
    private var isLoading by mutableStateOf(false)
    private var searchQuery by mutableStateOf("")

    private var searchJob: Job? = null
    private var typesMap: Map<Id, ObjectWrapper.Type> = emptyMap()

    sealed class ScreenState {
        data object SpaceSelection : ScreenState()
        data object ObjectSelection : ScreenState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        componentManager().objectShortcutWidgetConfigComponent.get().inject(this)

        setContentView(
            ComposeView(this).apply {
                setContent {
                    WidgetConfigTheme {
                        when (screenState) {
                            ScreenState.SpaceSelection -> {
                                SpaceSelectionScreen(
                                    spaceViews = spaceViews,
                                    urlBuilder = urlBuilder,
                                    onSpaceSelected = { space ->
                                        selectedSpace = space
                                        screenState = ScreenState.ObjectSelection
                                        searchObjectsInSpace(
                                            spaceId = space.targetSpaceId.orEmpty(),
                                            query = "",
                                            fetchTypes = true
                                        )
                                    },
                                    onCancel = { finish() }
                                )
                            }
                            ScreenState.ObjectSelection -> {
                                ObjectSelectionScreen(
                                    spaceName = selectedSpace?.name.orEmpty(),
                                    objectItems = objectItems,
                                    isLoading = isLoading,
                                    searchQuery = searchQuery,
                                    onSearchQueryChanged = { query ->
                                        searchQuery = query
                                        searchObjectsInSpace(
                                            selectedSpace?.targetSpaceId.orEmpty(),
                                            query,
                                            fetchTypes = false
                                        )
                                    },
                                    onObjectSelected = { item ->
                                        completeConfiguration(item.obj)
                                    },
                                    onBack = {
                                        searchJob?.cancel()
                                        selectedSpace = null
                                        objectItems = emptyList()
                                        typesMap = emptyMap()
                                        searchQuery = ""
                                        screenState = ScreenState.SpaceSelection
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    private fun searchObjectsInSpace(spaceId: String, query: String, fetchTypes: Boolean) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            isLoading = true
            delay(300) // Debounce

            try {
                // Fetch types once when entering the space
                if (fetchTypes) {
                    typesMap = fetchObjectTypesForSpace(SpaceId(spaceId))
                }

                val filters = buildList {
                    // Exclude deleted
                    add(DVFilter(
                        relation = Relations.IS_DELETED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    ))
                    // Exclude archived
                    add(DVFilter(
                        relation = Relations.IS_ARCHIVED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    ))
                    // Exclude templates
                    add(DVFilter(
                        relation = Relations.TYPE_UNIQUE_KEY,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = ObjectTypeUniqueKeys.TEMPLATE
                    ))
                    // Include common layouts
                    add(DVFilter(
                        relation = Relations.LAYOUT,
                        condition = DVFilterCondition.IN,
                        value = listOf(
                            ObjectType.Layout.BASIC.code.toDouble(),
                            ObjectType.Layout.PROFILE.code.toDouble(),
                            ObjectType.Layout.TODO.code.toDouble(),
                            ObjectType.Layout.NOTE.code.toDouble(),
                            ObjectType.Layout.BOOKMARK.code.toDouble(),
                            ObjectType.Layout.SET.code.toDouble(),
                            ObjectType.Layout.COLLECTION.code.toDouble(),
                            ObjectType.Layout.IMAGE.code.toDouble(),
                            ObjectType.Layout.FILE.code.toDouble(),
                            ObjectType.Layout.PDF.code.toDouble(),
                            ObjectType.Layout.AUDIO.code.toDouble(),
                            ObjectType.Layout.VIDEO.code.toDouble(),
                            ObjectType.Layout.CHAT_DERIVED.code.toDouble()
                        )
                    ))
                }

                val sorts = listOf(
                    DVSort(
                        relationKey = Relations.LAST_OPENED_DATE,
                        type = DVSortType.DESC,
                        relationFormat = RelationFormat.DATE,
                        includeTime = true
                    )
                )

                val params = SearchObjects.Params(
                    space = SpaceId(spaceId),
                    filters = filters,
                    sorts = sorts,
                    fulltext = query,
                    keys = ObjectSearchConstants.defaultKeys,
                    limit = 100
                )

                val result = searchObjects(params)
                result.process(
                    failure = { error ->
                        Timber.e(error, "Error searching objects")
                        objectItems = emptyList()
                    },
                    success = { foundObjects ->
                        objectItems = foundObjects.map { obj ->
                            val typeId = obj.type.firstOrNull()
                            val objType = typeId?.let { typesMap[it] }
                            ObjectItemView(
                                obj = obj,
                                icon = obj.objectIcon(builder = urlBuilder, objType = objType),
                                typeName = objType?.name.orEmpty()
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error searching objects")
                objectItems = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Fetches all object types from the given space.
     */
    private suspend fun fetchObjectTypesForSpace(spaceId: SpaceId): Map<Id, ObjectWrapper.Type> {
        val filters = buildList {
            add(DVFilter(
                relation = Relations.IS_DELETED,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            ))
            add(DVFilter(
                relation = Relations.IS_ARCHIVED,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            ))
            add(DVFilter(
                relation = Relations.TYPE_UNIQUE_KEY,
                condition = DVFilterCondition.NOT_EQUAL,
                value = ObjectTypeUniqueKeys.TEMPLATE
            ))
            add(DVFilter(
                relation = Relations.LAYOUT,
                condition = DVFilterCondition.EQUAL,
                value = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
            ))
            add(DVFilter(
                relation = Relations.UNIQUE_KEY,
                condition = DVFilterCondition.NOT_EMPTY
            ))
        }

        val params = SearchObjects.Params(
            space = spaceId,
            filters = filters,
            sorts = emptyList(),
            keys = ObjectSearchConstants.defaultKeysObjectType,
            limit = 0
        )

        return try {
            val results = searchObjects(params).getOrNull() ?: emptyList()
            results.mapNotNull { obj ->
                obj.map.mapToObjectWrapperType()?.let { type ->
                    type.id to type
                }
            }.toMap()
        } catch (e: Exception) {
            Timber.e(e, "Error fetching object types for space")
            emptyMap()
        }
    }

    private fun completeConfiguration(obj: ObjectWrapper.Basic) {
        val space = selectedSpace ?: return
        lifecycleScope.launch {
            try {
                // Cache the icon image if available
                val iconCache = OsWidgetIconCache(applicationContext)
                val cachedIconPath = obj.iconImage?.takeIf { it.isNotEmpty() }?.let { iconHash ->
                    val iconUrl = urlBuilder.thumbnail(iconHash)
                    iconCache.cacheShortcutIcon(
                        url = iconUrl,
                        widgetId = appWidgetId,
                        prefix = OsWidgetIconCache.PREFIX_OBJECT
                    )
                }

                val config = OsWidgetObjectShortcutEntity(
                    appWidgetId = appWidgetId,
                    spaceId = space.targetSpaceId.orEmpty(),
                    spaceName = space.name.orEmpty(),
                    objectId = obj.id,
                    objectName = obj.name.orEmpty(),
                    objectIconEmoji = obj.iconEmoji,
                    objectIconImage = obj.iconImage,
                    objectIconName = obj.iconName,
                    objectIconOption = obj.iconOption?.toInt(),
                    objectLayout = obj.layout?.code,
                    cachedIconPath = cachedIconPath
                )

                OsWidgetsDataStore(applicationContext).saveObjectShortcutConfig(config)
                OsObjectShortcutWidgetUpdater.update(applicationContext, appWidgetId)

                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                )
                finish()
            } catch (e: Exception) {
                Timber.e(e, "Error saving widget config")
                Toast.makeText(this@ObjectShortcutWidgetConfigActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        componentManager().objectShortcutWidgetConfigComponent.release()
    }
}

