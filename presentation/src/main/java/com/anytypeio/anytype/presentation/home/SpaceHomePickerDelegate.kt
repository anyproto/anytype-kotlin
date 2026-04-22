package com.anytypeio.anytype.presentation.home

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.spaces.ResolveSpaceHomepage
import com.anytypeio.anytype.domain.spaces.SetHomepage
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Shared state + behaviour for the Space Home picker used by DROID-4467.
 *
 * The picker is launched from two places that own different ViewModels:
 *  - the widgets-overlay Home widget's long-press "Change Home" action,
 *  - the Space Settings → Home row.
 *
 * Both hosts own one of these delegates and forward their ViewModel scope.
 * The delegate resolves current homepage object id, fetches eligible
 * candidates via SearchObjects, and writes homepage changes via SetHomepage.
 */
class SpaceHomePickerDelegate(
    private val space: SpaceId,
    private val setHomepage: SetHomepage,
    private val searchObjects: SearchObjects,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val urlBuilder: UrlBuilder,
    private val isOneToOneSpaceProvider: () -> Boolean = { false }
) {
    private val _state = MutableStateFlow<SpaceHomePickerState>(SpaceHomePickerState.Hidden)
    val state: StateFlow<SpaceHomePickerState> = _state.asStateFlow()

    private var loadJob: Job? = null

    fun show(scope: CoroutineScope, currentHomepageObjectId: Id?) {
        // DROID-4469: 1-on-1 channels always open on Chat and must not offer a
        // homepage picker. Guard here so every caller (Home widget context
        // menu, Space Settings Home row, post-creation picker trigger) is
        // protected by a single check.
        if (isOneToOneSpaceProvider()) {
            Timber.d("SpaceHomePickerDelegate.show() skipped: 1-on-1 space")
            return
        }
        _state.value = SpaceHomePickerState.Visible(
            query = "",
            candidates = emptyList(),
            currentHomepage = currentHomepageObjectId,
            isLoading = true
        )
        load(scope, query = "")
    }

    fun dismiss() {
        loadJob?.cancel()
        loadJob = null
        _state.value = SpaceHomePickerState.Hidden
    }

    fun onQueryChanged(scope: CoroutineScope, query: String) {
        val current = _state.value
        if (current is SpaceHomePickerState.Visible) {
            _state.value = current.copy(query = query, isLoading = true)
            load(scope, query, debounceMillis = SEARCH_DEBOUNCE_MS)
        }
    }

    fun onObjectSelected(scope: CoroutineScope, objectId: Id) {
        scope.launch {
            setHomepage.async(
                SetHomepage.Params(spaceId = space.id, homepage = objectId)
            ).onFailure { Timber.e(it, "Failed to set homepage to object") }
            _state.value = SpaceHomePickerState.Hidden
        }
    }

    fun onNoHomeSelected(scope: CoroutineScope) {
        scope.launch {
            setHomepage.async(
                SetHomepage.Params(spaceId = space.id, homepage = ResolveSpaceHomepage.HOMEPAGE_WIDGETS_VALUE)
            ).onFailure { Timber.e(it, "Failed to set homepage to widgets") }
            _state.value = SpaceHomePickerState.Hidden
        }
    }

    private fun load(scope: CoroutineScope, query: String, debounceMillis: Long = 0L) {
        loadJob?.cancel()
        loadJob = scope.launch {
            if (debounceMillis > 0) delay(debounceMillis)
            val params = SearchObjects.Params(
                space = space,
                filters = buildList {
                    addAll(
                        ObjectSearchConstants.filterSearchObjects(
                            excludeTypes = true,
                            isOneToOneSpace = isOneToOneSpaceProvider()
                        )
                    )
                    add(
                        DVFilter(
                            relation = Relations.LAYOUT,
                            condition = DVFilterCondition.IN,
                            value = HOMEPAGE_ELIGIBLE_LAYOUTS.map { it.code.toDouble() }
                        )
                    )
                },
                fulltext = query,
                keys = ObjectSearchConstants.defaultKeys,
                limit = LIMIT
            )
            searchObjects.invoke(params).proceed(
                failure = { e ->
                    Timber.e(e, "Failed to load space home picker candidates")
                    val current = _state.value
                    if (current is SpaceHomePickerState.Visible && current.query == query) {
                        _state.value = current.copy(isLoading = false)
                    }
                },
                success = { objects ->
                    val items = mutableListOf<SpaceHomePickerItem>()
                    for (obj in objects) {
                        if (!obj.notDeletedNorArchived) continue
                        val objType = storeOfObjectTypes.getTypeOfObject(obj)
                        items += SpaceHomePickerItem(
                            objectId = obj.id,
                            name = fieldParser.getObjectName(obj),
                            icon = obj.objectIcon(builder = urlBuilder, objType = objType),
                            type = objType?.name.orEmpty()
                        )
                    }
                    val current = _state.value
                    if (current is SpaceHomePickerState.Visible && current.query == query) {
                        _state.value = current.copy(
                            candidates = items,
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    companion object {
        private const val LIMIT = 100
        private const val SEARCH_DEBOUNCE_MS = 300L

        private val HOMEPAGE_ELIGIBLE_LAYOUTS: List<ObjectType.Layout> = listOf(
            ObjectType.Layout.BASIC,
            ObjectType.Layout.NOTE,
            ObjectType.Layout.PROFILE,
            ObjectType.Layout.TODO,
            ObjectType.Layout.COLLECTION,
            ObjectType.Layout.SET,
            ObjectType.Layout.CHAT_DERIVED
        )
    }
}
