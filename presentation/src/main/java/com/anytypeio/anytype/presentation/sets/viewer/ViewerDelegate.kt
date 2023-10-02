package com.anytypeio.anytype.presentation.sets.viewer

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.DeleteDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.DuplicateDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.SetDataViewViewerPosition
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.util.Dispatcher
import javax.inject.Inject
import timber.log.Timber

interface ViewerDelegate {
    suspend fun onEvent(event: ViewerEvent)
}

sealed class ViewerEvent {
    data class Delete(val ctx: Id, val dv: Id, val viewer: Id, val onResult: () -> Unit) : ViewerEvent()
    data class Duplicate(val ctx: Id, val dv: Id, val viewer: DVViewer, val onResult: () -> Unit) : ViewerEvent()
    data class AddNew(val ctx: Id, val dv: Id, val viewer: DVViewer, val onResult: (String) -> Unit) :
        ViewerEvent()

    data class UpdatePosition(val ctx: Id, val dv: Id, val viewer: Id, val position: Int, val onResult: () -> Unit) :
        ViewerEvent()

    data class SetActive(val viewer: Id, val onResult: () -> Unit) : ViewerEvent()

    data class UpdateView(val ctx: Id, val dv: Id, val viewer: DVViewer, val onResult: () -> Unit) : ViewerEvent()
}

class DefaultViewerDelegate @Inject constructor(
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    private val deleteDataViewViewer: DeleteDataViewViewer,
    private val setDataViewViewerPosition: SetDataViewViewerPosition,
    private val duplicateDataViewViewer: DuplicateDataViewViewer,
    private val addDataViewViewer: AddDataViewViewer,
    private val updateDataViewViewer: UpdateDataViewViewer
) : ViewerDelegate {

    override suspend fun onEvent(event: ViewerEvent) {
        when (event) {
            is ViewerEvent.Delete -> onDelete(
                ctx = event.ctx,
                dv = event.dv,
                viewer = event.viewer,
                onResult = event.onResult
            )

            is ViewerEvent.AddNew -> onAddNew(
                ctx = event.ctx,
                dv = event.dv,
                viewer = event.viewer,
                onResult = event.onResult
            )

            is ViewerEvent.Duplicate -> onDuplicate(
                ctx = event.ctx,
                dv = event.dv,
                viewer = event.viewer,
                onResult = event.onResult
            )

            is ViewerEvent.UpdatePosition -> onUpdatePosition(
                ctx = event.ctx,
                dv = event.dv,
                viewer = event.viewer,
                position = event.position,
                onResult = event.onResult
            )

            is ViewerEvent.SetActive -> {
                session.currentViewerId.value = event.viewer
                event.onResult()
            }

            is ViewerEvent.UpdateView -> {
                onUpdateViewer(
                    ctx = event.ctx,
                    dv = event.dv,
                    viewer = event.viewer,
                    onResult = event.onResult
                )
            }
        }
    }

    private suspend fun onAddNew(ctx: Id, dv: Id, viewer: DVViewer, onResult: (String) -> Unit) {
        val params = DuplicateDataViewViewer.Params(
            context = ctx,
            target = dv,
            viewer = viewer
        )
        duplicateDataViewViewer.async(params).fold(
            onFailure = { Timber.e(it, "Error while adding new viewer") },
            onSuccess = { (id, payload) ->
                dispatcher.send(payload)
                onResult(id)
            }
        )
    }

    private suspend fun onUpdatePosition(ctx: Id, dv: Id, viewer: Id, position: Int, onResult: () -> Unit) {
        val params = SetDataViewViewerPosition.Params(
            ctx = ctx,
            dv = dv,
            viewer = viewer,
            pos = position
        )
        setDataViewViewerPosition.async(params).fold(
            onFailure = { Timber.e(it, "Error while updating position") },
            onSuccess = {
                dispatcher.send(it)
                onResult()
            }
        )
    }

    private suspend fun onDuplicate(ctx: Id, dv: Id, viewer: DVViewer, onResult: () -> Unit) {
        val params = DuplicateDataViewViewer.Params(
            context = ctx,
            target = dv,
            viewer = viewer
        )
        duplicateDataViewViewer.async(params).fold(
            onFailure = { Timber.e(it, "Error while duplicating view") },
            onSuccess = { (_, payload) ->
                dispatcher.send(payload)
                onResult()
            }
        )
    }

    private suspend fun onDelete(ctx: Id, dv: Id, viewer: Id, onResult: () -> Unit) {
        val params = DeleteDataViewViewer.Params(
            ctx = ctx,
            dataview = dv,
            viewer = viewer
        )
        deleteDataViewViewer.async(params).fold(
            onFailure = { Timber.e(it, "Error while deleting view") },
            onSuccess = {
                dispatcher.send(it)
                onResult()
            }
        )
    }

    private suspend fun onUpdateViewer(ctx: Id, dv: Id, viewer: DVViewer, onResult: () -> Unit) {
        val params = UpdateDataViewViewer.Params.UpdateView(
            context = ctx,
            target = dv,
            viewer = viewer
        )
        updateDataViewViewer.async(params).fold(
            onFailure = { Timber.e(it, "Error while updating view") },
            onSuccess = {
                dispatcher.send(it)
                onResult()
            }
        )
    }
}