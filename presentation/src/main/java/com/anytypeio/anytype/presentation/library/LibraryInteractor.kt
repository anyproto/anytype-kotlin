package com.anytypeio.anytype.presentation.library

import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.library.LibrarySearchParams
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.navigation.LibraryView
import com.anytypeio.anytype.presentation.objects.toLibraryViews
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface LibraryInteractor {

    fun subscribe(searchParams: LibrarySearchParams): Flow<List<LibraryView>>

    class Impl @Inject constructor(
        private val repo: BlockRepository,
        private val urlBuilder: UrlBuilder,
    ) : LibraryInteractor {
        override fun subscribe(searchParams: LibrarySearchParams): Flow<List<LibraryView>> =
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
                    ).results.toLibraryViews(urlBuilder = urlBuilder)

                    emit(initial)
                }
            }
    }
}