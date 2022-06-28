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
import com.anytypeio.anytype.domain.block.interactor.sets.CreateObjectSet
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.GetCompatibleObjectTypes
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateDocument
import com.anytypeio.anytype.domain.page.CreateNewDocument
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.page.OpenPage
import com.anytypeio.anytype.domain.sets.FindObjectSetForType
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.common.StateReducer
import com.anytypeio.anytype.domain.page.CreateNewObject
import com.anytypeio.anytype.presentation.editor.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.editor.editor.Orchestrator
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.editor.template.EditorTemplateDelegate
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.Dispatcher

open class EditorViewModelFactory(
    private val openPage: OpenPage,
    private val closeObject: CloseBlock,
    private val createDocument: CreateDocument,
    private val createObjectSet: CreateObjectSet,
    private val createObject: CreateObject,
    private val createNewDocument: CreateNewDocument,
    private val setObjectIsArchived: SetObjectIsArchived,
    private val interceptEvents: InterceptEvents,
    private val interceptThreadStatus: InterceptThreadStatus,
    private val updateLinkMarks: UpdateLinkMarks,
    private val removeLinkMark: RemoveLinkMark,
    private val documentEventReducer: StateReducer<List<Block>, Event>,
    private val urlBuilder: UrlBuilder,
    private val renderer: DefaultBlockViewRenderer,
    private val orchestrator: Orchestrator,
    private val analytics: Analytics,
    private val dispatcher: Dispatcher<Payload>,
    private val delegator: Delegator<Action>,
    private val detailModificationManager: DetailModificationManager,
    private val updateDetail: UpdateDetail,
    private val getCompatibleObjectTypes: GetCompatibleObjectTypes,
    private val objectTypesProvider: ObjectTypesProvider,
    private val searchObjects: SearchObjects,
    private val getDefaultEditorType: GetDefaultEditorType,
    private val findObjectSetForType: FindObjectSetForType,
    private val copyFileToCacheDirectory: CopyFileToCacheDirectory,
    private val downloadUnsplashImage: DownloadUnsplashImage,
    private val setDocCoverImage: SetDocCoverImage,
    private val setDocImageIcon: SetDocumentImageIcon,
    private val editorTemplateDelegate: EditorTemplateDelegate,
    private val createNewObject: CreateNewObject
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditorViewModel(
            openPage = openPage,
            closePage = closeObject,
            setObjectIsArchived = setObjectIsArchived,
            interceptEvents = interceptEvents,
            interceptThreadStatus = interceptThreadStatus,
            updateLinkMarks = updateLinkMarks,
            removeLinkMark = removeLinkMark,
            createObject = createObject,
            reducer = documentEventReducer,
            urlBuilder = urlBuilder,
            renderer = renderer,
            createDocument = createDocument,
            createNewDocument = createNewDocument,
            orchestrator = orchestrator,
            analytics = analytics,
            dispatcher = dispatcher,
            delegator = delegator,
            detailModificationManager = detailModificationManager,
            updateDetail = updateDetail,
            getCompatibleObjectTypes = getCompatibleObjectTypes,
            objectTypesProvider = objectTypesProvider,
            searchObjects = searchObjects,
            getDefaultEditorType = getDefaultEditorType,
            findObjectSetForType = findObjectSetForType,
            createObjectSet = createObjectSet,
            copyFileToCache = copyFileToCacheDirectory,
            downloadUnsplashImage = downloadUnsplashImage,
            setDocCoverImage = setDocCoverImage,
            setDocImageIcon = setDocImageIcon,
            templateDelegate = editorTemplateDelegate,
            createNewObject = createNewObject
        ) as T
    }
}