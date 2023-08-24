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

    override suspend fun onEvent(event: ViewerEvent) {
        when (event) {
            is ViewerEvent.Delete -> onDelete(
                ctx = event.ctx,
                dv = event.dv,
                viewer = event.viewer
            )

            is ViewerEvent.AddNew -> onAddNew(
                ctx = event.ctx,
                dv = event.dv,
                name = event.name,
                type = event.type
            )

            is ViewerEvent.Duplicate -> onDuplicate(
                ctx = event.ctx,
                dv = event.dv,
                viewer = event.viewer
            )

            is ViewerEvent.Rename -> onRename(
                ctx = event.ctx,
                dv = event.dv,
                viewer = event.viewer
            )

            is ViewerEvent.UpdatePosition -> onUpdatePosition(
                ctx = event.ctx,
                dv = event.dv,
                viewer = event.viewer,
                position = event.position
            )

            is ViewerEvent.SetActive -> {
                session.currentViewerId.value = event.viewer
            }
        }
    }

    private suspend fun onAddNew(ctx: Id, dv: Id, name: String, type: DVViewerType) {
        val params = AddDataViewViewer.Params(
            ctx = ctx,
            target = dv,
            name = name,
            type = type
        )
        addDataViewViewer.async(params).fold(
            onFailure = { Timber.e(it, "Error while adding new viewer") },
            onSuccess = { dispatcher.send(it) }
        )
    }

    private suspend fun onUpdatePosition(ctx: Id, dv: Id, viewer: Id, position: Int) {
        val params = SetDataViewViewerPosition.Params(
            ctx = ctx,
            dv = dv,
            viewer = viewer,
            pos = position
        )
        setDataViewViewerPosition.async(params).fold(
            onFailure = { Timber.e(it, "Error while updating position") },
            onSuccess = { dispatcher.send(it) }
        )
    }

    private suspend fun onRename(ctx: Id, dv: Id, viewer: DVViewer) {
        val params = RenameDataViewViewer.Params(
            context = ctx,
            target = dv,
            viewer = viewer
        )
        renameDataViewViewer.async(params).fold(
            onFailure = { Timber.e(it, "Error while renaming view") },
            onSuccess = { dispatcher.send(it) }
        )
    }

    private suspend fun onDuplicate(ctx: Id, dv: Id, viewer: DVViewer) {
        val params = DuplicateDataViewViewer.Params(
            context = ctx,
            target = dv,
            viewer = viewer
        )
        duplicateDataViewViewer.async(params).fold(
            onFailure = { Timber.e(it, "Error while duplicating view") },
            onSuccess = { dispatcher.send(it) }
        )
    }

    private suspend fun onDelete(ctx: Id, dv: Id, viewer: Id) {
        val params = DeleteDataViewViewer.Params(
            ctx = ctx,
            dataview = dv,
            viewer = viewer
        )
        deleteDataViewViewer.async(params).fold(
            onFailure = { Timber.e(it, "Error while deleting view") },
            onSuccess = { dispatcher.send(it) }
        )
    }
}