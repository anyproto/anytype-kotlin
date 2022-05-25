package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.AddNewRelationToDataView
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.SetActiveViewer
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.util.Dispatcher

class ObjectSetViewModelFactory(
    private val reducer: ObjectSetReducer,
    private val openObjectSet: OpenObjectSet,
    private val closeBlock: CloseBlock,
    private val setActiveViewer: SetActiveViewer,
    private val addDataViewRelation: AddNewRelationToDataView,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val updateDataViewRecord: UpdateDataViewRecord,
    private val createDataViewRecord: CreateDataViewRecord,
    private val downloadUnsplashImage: DownloadUnsplashImage,
    private val setDocCoverImage: SetDocCoverImage,
    private val updateText: UpdateText,
    private val interceptEvents: InterceptEvents,
    private val interceptThreadStatus: InterceptThreadStatus,
    private val dispatcher: Dispatcher<Payload>,
    private val delegator: Delegator<Action>,
    private val objectSetRecordCache: ObjectSetRecordCache,
    private val urlBuilder: UrlBuilder,
    private val session: ObjectSetSession,
    private val analytics: Analytics,
    private val getTemplates: GetTemplates
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ObjectSetViewModel(
            reducer = reducer,
            openObjectSet = openObjectSet,
            closeBlock = closeBlock,
            setActiveViewer = setActiveViewer,
            addDataViewRelation = addDataViewRelation,
            updateDataViewViewer = updateDataViewViewer,
            updateDataViewRecord = updateDataViewRecord,
            createDataViewRecord = createDataViewRecord,
            setDocCoverImage = setDocCoverImage,
            downloadUnsplashImage = downloadUnsplashImage,
            updateText = updateText,
            interceptEvents = interceptEvents,
            interceptThreadStatus = interceptThreadStatus,
            dispatcher = dispatcher,
            delegator = delegator,
            objectSetRecordCache = objectSetRecordCache,
            urlBuilder = urlBuilder,
            session = session,
            analytics = analytics,
            getTemplates = getTemplates
        ) as T
    }
}