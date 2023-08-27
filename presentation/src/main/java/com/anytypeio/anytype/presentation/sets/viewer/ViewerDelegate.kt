package com.anytypeio.anytype.presentation.sets.viewer

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.DeleteDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.DuplicateDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.RenameDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.SetDataViewViewerPosition
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.util.Dispatcher
import javax.inject.Inject
import timber.log.Timber

interface ViewerDelegate {
    suspend fun onEvent(event: ViewerEvent)
}

sealed class ViewerEvent {
    data class Delete(val ctx: Id, val dv: Id, val viewer: Id) : ViewerEvent()
    data class Duplicate(val ctx: Id, val dv: Id, val viewer: DVViewer) : ViewerEvent()
    data class Rename(val ctx: Id, val dv: Id, val viewer: DVViewer) : ViewerEvent()
    data class AddNew(val ctx: Id, val dv: Id, val name: String, val type: DVViewerType) :
        ViewerEvent()

    data class UpdatePosition(val ctx: Id, val dv: Id, val viewer: Id, val position: Int) :
        ViewerEvent()

    data class SetActive(val viewer: Id) : ViewerEvent()
}

class DefaultViewerDelegate @Inject constructor(
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    private val deleteDataViewViewer: DeleteDataViewViewer,
    private val setDataViewViewerPosition: SetDataViewViewerPosition,
    private val duplicateDataViewViewer: DuplicateDataViewViewer,
    private val addDataViewViewer: AddDataViewViewer,
    private val renameDataViewViewer: RenameDataViewViewer
) : ViewerDelegate {

    override suspend fun onEvent(event: ViewerEvent) {}
}