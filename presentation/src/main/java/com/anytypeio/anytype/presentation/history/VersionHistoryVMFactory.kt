package com.anytypeio.anytype.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.history.GetVersions
import com.anytypeio.anytype.domain.history.SetVersion
import com.anytypeio.anytype.domain.history.ShowVersion
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.history.VersionHistoryViewModel.VmParams
import com.anytypeio.anytype.presentation.sets.state.ObjectStateReducer
import javax.inject.Inject

class VersionHistoryVMFactory @Inject constructor(
    private val vmParams: VmParams,
    private val getVersions: GetVersions,
    private val objectSearch: SearchObjects,
    private val dateProvider: DateProvider,
    private val localeProvider: LocaleProvider,
    private val analytics: Analytics,
    private val urlBuilder: UrlBuilder,
    private val showVersion: ShowVersion,
    private val setVersion: SetVersion,
    private val renderer: DefaultBlockViewRenderer,
    private val setStateReducer: ObjectStateReducer,
    private val coverImageHashProvider: CoverImageHashProvider,
    private val storeOfRelations: StoreOfRelations
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VersionHistoryViewModel(
            vmParams = vmParams,
            getVersions = getVersions,
            objectSearch = objectSearch,
            dateProvider = dateProvider,
            localeProvider = localeProvider,
            analytics = analytics,
            urlBuilder = urlBuilder,
            showVersion = showVersion,
            renderer = renderer,
            setVersion = setVersion,
            setStateReducer = setStateReducer,
            coverImageHashProvider = coverImageHashProvider,
            storeOfRelations = storeOfRelations
        ) as T
    }
}