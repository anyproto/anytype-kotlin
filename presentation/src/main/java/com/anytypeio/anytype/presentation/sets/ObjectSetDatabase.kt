package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer.Index
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class ObjectSetDatabase(
    val store: ObjectStore
) {
    private val _index = MutableStateFlow(Index())
    val index: StateFlow<Index> = _index

    fun update(update: Index) {
        _index.value = update
    }

    fun observe(target: Id) : Flow<ObjectWrapper.Basic> = index
        .onEach { Timber.d("SET-DB: Index changed —>\n$it") }
        .mapNotNull { store.get(target) }
}