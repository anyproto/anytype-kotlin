package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.dataview.interactor.ObjectRelationList
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.page.Editor
import com.anytypeio.anytype.presentation.page.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectRelationListViewModel(
    private val stores: Editor.Storage,
    private val urlBuilder: UrlBuilder,
    private val objectRelationList: ObjectRelationList,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDetail: UpdateDetail,
    private val detailModificationManager: DetailModificationManager
) : ViewModel() {

    private val jobs = mutableListOf<Job>()

    private val isInAddMode = MutableStateFlow(false)
    val commands = MutableSharedFlow<Command>(replay = 0)
    val views = MutableStateFlow<List<DocumentRelationView>>(emptyList())

    fun onStartListMode(ctx: Id) {
        isInAddMode.value = false
        jobs += viewModelScope.launch {
            stores.relations.stream().combine(stores.details.stream()) { relations, details ->
                val values = details.details[ctx]?.map ?: emptyMap()
                relations.views(details, values, urlBuilder)
            }.collect { views.value = it }
        }
    }

    fun onStartAddMode(ctx: Id) {
        isInAddMode.value = true
        getRelations(ctx)
    }

    fun onStop() {
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
    }

    fun onRelationClicked(ctx: Id, target: Id?, view: DocumentRelationView) {
        if (isInAddMode.value) {
            onRelationClickedAddMode(target = target, view = view)
        } else {
            onRelationClickedListMode(ctx = ctx, view = view)
        }
    }

    private fun onRelationClickedAddMode(
        target: Id?,
        view: DocumentRelationView
    ) {
        checkNotNull(target)
        viewModelScope.launch {
            commands.emit(
                Command.SetRelationKey(
                    blockId = target,
                    key = view.relationId
                )
            )
        }
    }

    private fun onRelationClickedListMode(ctx: Id, view: DocumentRelationView) {
        viewModelScope.launch {
            val relation = stores.relations.current().first { it.key == view.relationId }
            when (relation.format) {
                Relation.Format.SHORT_TEXT,
                Relation.Format.LONG_TEXT,
                Relation.Format.NUMBER,
                Relation.Format.URL,
                Relation.Format.EMAIL,
                Relation.Format.PHONE -> {
                    commands.emit(
                        Command.EditTextRelationValue(
                            ctx = ctx,
                            relation = view.relationId,
                            target = ctx
                        )
                    )
                }
                Relation.Format.CHECKBOX -> {
                    proceedWithTogglingRelationCheckboxValue(view, ctx)
                }
                Relation.Format.DATE -> {
                    commands.emit(
                        Command.EditDateRelationValue(
                            ctx = ctx,
                            relation = view.relationId,
                            target = ctx
                        )
                    )
                }
                Relation.Format.STATUS,
                Relation.Format.TAG,
                Relation.Format.FILE,
                Relation.Format.OBJECT -> {
                    commands.emit(
                        Command.EditRelationValue(
                            ctx = ctx,
                            relation = view.relationId,
                            target = ctx
                        )
                    )
                }
            }
        }
    }

    private fun proceedWithTogglingRelationCheckboxValue(view: DocumentRelationView, ctx: Id) {
        viewModelScope.launch {
            check(view is DocumentRelationView.Checkbox)
            updateDetail(
                UpdateDetail.Params(
                    ctx = ctx,
                    key = view.relationId,
                    value = !view.isChecked
                )
            ).process(
                success = { dispatcher.send(it) },
                failure = { Timber.e(it, "Error while updating checkbox relation") }
            )
        }
    }

    private fun getRelations(ctx: Id) {
        viewModelScope.launch {
            objectRelationList.invoke(ObjectRelationList.Params(ctx = ctx)).process(
                failure = { throwable -> Timber.e("Error while getting object relation list $throwable") },
                success = { list: List<Relation> ->
                    val details = stores.details.current()
                    val values = details.details[ctx]?.map ?: emptyMap()
                    views.value = list.views(details, values, urlBuilder)
                }
            )
        }
    }

    fun onRelationTextValueChanged(
        ctx: Id,
        value: Any?,
        objectId: Id,
        relationId: Id
    ) {
        viewModelScope.launch {
            updateDetail(
                UpdateDetail.Params(
                    ctx = ctx,
                    key = relationId,
                    value = value
                )
            ).process(
                success = { payload ->
                    if (payload.events.isNotEmpty()) dispatcher.send(payload)
                    detailModificationManager.updateRelationValue(
                        target = ctx,
                        key = relationId,
                        value = value
                    )
                },
                failure = { Timber.e(it, "Error while updating relation values") }
            )
        }
    }

    sealed class Command {
        data class EditTextRelationValue(
            val ctx: Id,
            val relation: Id,
            val target: Id
        ) : Command()

        data class EditDateRelationValue(
            val ctx: Id,
            val relation: Id,
            val target: Id
        ) : Command()

        data class EditRelationValue(
            val ctx: Id,
            val relation: Id,
            val target: Id
        ) : Command()

        data class SetRelationKey(
            val blockId: Id,
            val key: Id
        ) : Command()
    }
}