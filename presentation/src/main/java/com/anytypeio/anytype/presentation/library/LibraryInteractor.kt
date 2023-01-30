package com.anytypeio.anytype.presentation.library

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.library.LibrarySearchParams
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface LibraryInteractor {
    
    fun subscribe(searchParams: LibrarySearchParams): Flow<List<ObjectWrapper>>

    class Impl @Inject constructor(
        private val repo: BlockRepository
    ) : LibraryInteractor {
        override fun subscribe(searchParams: LibrarySearchParams): Flow<List<ObjectWrapper>> =
            flow {
                with(searchParams) {
                    val initial = repo.searchObjectsWithSubscription(
                        subscription = subscription,
                        sorts = sorts,
                        filters = filters,
                        offset = offset,
                        limit = limit,
                        keys = keys,
                        afterId = null,
                        beforeId = null,
                        source = source,
                        ignoreWorkspace = null,
                        noDepSubscription = null
                    ).results
                    emit(initial)
                }
            }
    }
}