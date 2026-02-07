package com.anytypeio.anytype.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.history.GetVersions
import com.anytypeio.anytype.domain.history.SetVersion
import com.anytypeio.anytype.domain.history.ShowVersion
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.history.VersionHistoryViewModel.VmParams
import com.anytypeio.anytype.presentation.sets.state.ObjectStateReducer
import javax.inject.Inject

class VersionHistoryVMFactory @Inject constructor(
    private val vmParams: VmParams,
    private val getVersions: GetVersions,
    private val objectSearch: SearchObjects,
    private val dateProvider: DateProvider,
    private val analytics: Analytics,
    private val urlBuilder: UrlBuilder,
    private val showVersion: ShowVersion,
    private val setVersion: SetVersion,
    private val renderer: DefaultBlockViewRenderer,
    private val setStateReducer: ObjectStateReducer,
    private val storeOfRelations: StoreOfRelations,
    private val storeOfObjectTypes: StoreOfObjectTypes
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VersionHistoryViewModel(
            vmParams = vmParams,
            getVersions = getVersions,
            objectSearch = objectSearch,
            dateProvider = dateProvider,
            analytics = analytics,
            urlBuilder = urlBuilder,
            showVersion = showVersion,
            renderer = renderer,
            setVersion = setVersion,
            setStateReducer = setStateReducer,
            storeOfRelations = storeOfRelations,
            storeOfObjectTypes = storeOfObjectTypes
        ) as T
    }
}