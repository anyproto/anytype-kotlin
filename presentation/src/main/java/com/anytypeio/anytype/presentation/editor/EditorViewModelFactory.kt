package com.anytypeio.anytype.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.interactor.RemoveLinkMark
import com.anytypeio.anytype.domain.block.interactor.UpdateLinkMarks
import com.anytypeio.anytype.domain.cover.RemoveDocCover
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.GetCompatibleObjectTypes
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
import com.anytypeio.anytype.domain.page.*
import com.anytypeio.anytype.domain.page.navigation.GetListPages
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.presentation.common.StateReducer
import com.anytypeio.anytype.presentation.editor.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.editor.editor.Orchestrator
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.util.Dispatcher

open class EditorViewModelFactory(
    private val openPage: OpenPage,
    private val closeObject: CloseBlock,
    private val createPage: CreatePage,
    private val createDocument: CreateDocument,
    private val createObject: CreateObject,
    private val createNewDocument: CreateNewDocument,
    private val setObjectIsArchived: SetObjectIsArchived,
    private val setDocCoverImage: SetDocCoverImage,
    private val removeDocCover: RemoveDocCover,
    private val interceptEvents: InterceptEvents,
    private val interceptThreadStatus: InterceptThreadStatus,
    private val updateLinkMarks: UpdateLinkMarks,
    private val removeLinkMark: RemoveLinkMark,
    private val documentEventReducer: StateReducer<List<Block>, Event>,
    private val urlBuilder: UrlBuilder,
    private val renderer: DefaultBlockViewRenderer,
    private val orchestrator: Orchestrator,
    private val getListPages: GetListPages,
    private val analytics: Analytics,
    private val dispatcher: Dispatcher<Payload>,
    private val detailModificationManager: DetailModificationManager,
    private val updateDetail: UpdateDetail,
    private val getCompatibleObjectTypes: GetCompatibleObjectTypes,
    private val objectTypesProvider: ObjectTypesProvider
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return EditorViewModel(
            openPage = openPage,
            closePage = closeObject,
            setObjectIsArchived = setObjectIsArchived,
            interceptEvents = interceptEvents,
            interceptThreadStatus = interceptThreadStatus,
            updateLinkMarks = updateLinkMarks,
            removeLinkMark = removeLinkMark,
            createPage = createPage,
            createObject = createObject,
            reducer = documentEventReducer,
            urlBuilder = urlBuilder,
            renderer = renderer,
            createDocument = createDocument,
            createNewDocument = createNewDocument,
            setDocCoverImage = setDocCoverImage,
            removeDocCover = removeDocCover,
            orchestrator = orchestrator,
            getListPages = getListPages,
            analytics = analytics,
            dispatcher = dispatcher,
            detailModificationManager = detailModificationManager,
            updateDetail = updateDetail,
            getCompatibleObjectTypes = getCompatibleObjectTypes,
            objectTypesProvider = objectTypesProvider
        ) as T
    }
}