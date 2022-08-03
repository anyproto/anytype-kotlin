package com.anytypeio.anytype.presentation.relations.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewRelationOption
import com.anytypeio.anytype.domain.dataview.interactor.AddStatusToDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.AddTagToDataViewRecord
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.launch
import timber.log.Timber

class AddOptionsRelationDVViewModel(
    values: ObjectValueProvider,
    relations: ObjectRelationProvider,
    optionsProvider: AddOptionsRelationProvider,
    private val addDataViewRelationOption: AddDataViewRelationOption,
    private val addTagToDataViewRecord: AddTagToDataViewRecord,
    private val addStatusToDataViewRecord: AddStatusToDataViewRecord,
    private val dispatcher: Dispatcher<Payload>,
) : BaseAddOptionsRelationViewModel(
    values = values,
    relations = relations,
    optionsProvider = optionsProvider
) {

    fun onCreateDataViewRelationOptionClicked(
        ctx: Id,
        dataview: Id,
        viewer: Id,
        relation: Id,
        target: Id,
        name: String
    ) {
        viewModelScope.launch {
            addDataViewRelationOption(
                AddDataViewRelationOption.Params(
                    ctx = ctx,
                    relation = relation,
                    dataview = dataview,
                    record = target,
                    name = name,
                    color = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random().code
                )
            ).proceed(
                success = { (payload, option) ->
                    dispatcher.send(payload)
                    if (option != null) {
                        when (relations.get(relation).format) {
                            Relation.Format.TAG -> {
                                proceedWithAddingTagToDataViewRecord(
                                    ctx = ctx,
                                    dataview = dataview,
                                    viewer = viewer,
                                    relation = relation,
                                    target = target,
                                    tags = listOf(option)
                                )
                            }
                            Relation.Format.STATUS -> {
                                proceedWithAddingStatusToDataViewRecord(
                                    ctx = ctx,
                                    dataview = dataview,
                                    viewer = viewer,
                                    relation = relation,
                                    obj = target,
                                    status = option
                                )
                            }
                            else -> Timber.e("Trying to create option for wrong relation.")
                        }
                    }
                },
                failure = { Timber.e(it, "Error while creating a new option") }
            )
        }
    }

    fun onAddObjectSetStatusClicked(
        ctx: Id,
        dataview: Id,
        viewer: Id,
        relation: Id,
        obj: Id,
        status: RelationValueView.Option.Status
    ) {
        proceedWithAddingStatusToDataViewRecord(ctx, dataview, viewer, relation, obj, status.id)
    }

    private fun proceedWithAddingStatusToDataViewRecord(
        ctx: Id,
        dataview: Id,
        viewer: Id,
        relation: Id,
        obj: Id,
        status: Id
    ) {
        viewModelScope.launch {
            addStatusToDataViewRecord(
                AddStatusToDataViewRecord.Params(
                    ctx = ctx,
                    dataview = dataview,
                    viewer = viewer,
                    relation = relation,
                    obj = obj,
                    status = status,
                    record = values.get(target = obj)
                )
            ).process(
                failure = { Timber.e(it, "Error while adding tag") },
                success = { isParentDismissed.value = true }
            )
        }
    }

    fun onAddSelectedValuesToDataViewClicked(
        ctx: Id,
        dataview: Id,
        target: Id,
        relation: Id,
        viewer: Id
    ) {
        val tags = choosingRelationOptions.value.mapNotNull { view ->
            if (view is RelationValueView.Option.Tag && view.isSelected)
                view.id
            else
                null
        }
        proceedWithAddingTagToDataViewRecord(
            target = target,
            ctx = ctx,
            dataview = dataview,
            relation = relation,
            viewer = viewer,
            tags = tags
        )
    }

    private fun proceedWithAddingTagToDataViewRecord(
        target: Id,
        ctx: Id,
        dataview: Id,
        relation: Id,
        viewer: Id,
        tags: List<Id>
    ) {
        viewModelScope.launch {
            val record = values.get(target = target)
            addTagToDataViewRecord(
                AddTagToDataViewRecord.Params(
                    ctx = ctx,
                    tags = tags,
                    record = record,
                    dataview = dataview,
                    relation = relation,
                    viewer = viewer,
                    target = target
                )
            ).process(
                failure = { Timber.e(it, "Error while adding tag") },
                success = { isDismissed.value = true }
            )
        }
    }

    class Factory(
        private val values: ObjectValueProvider,
        private val relations: ObjectRelationProvider,
        private val addDataViewRelationOption: AddDataViewRelationOption,
        private val addTagToDataViewRecord: AddTagToDataViewRecord,
        private val addStatusToDataViewRecord: AddStatusToDataViewRecord,
        private val dispatcher: Dispatcher<Payload>,
        private val optionsProvider: AddOptionsRelationProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddOptionsRelationDVViewModel(
                values = values,
                relations = relations,
                addDataViewRelationOption = addDataViewRelationOption,
                addTagToDataViewRecord = addTagToDataViewRecord,
                addStatusToDataViewRecord = addStatusToDataViewRecord,
                dispatcher = dispatcher,
                optionsProvider = optionsProvider
            ) as T
        }
    }
}