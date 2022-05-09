package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewRelationOption
import com.anytypeio.anytype.domain.dataview.interactor.AddStatusToDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.AddTagToDataViewRecord
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.launch
import timber.log.Timber

class RelationOptionValueDVAddViewModel(
    details: ObjectDetailProvider,
    types: ObjectTypesProvider,
    urlBuilder: UrlBuilder,
    values: ObjectValueProvider,
    relations: ObjectRelationProvider,
    private val addDataViewRelationOption: AddDataViewRelationOption,
    private val addTagToDataViewRecord: AddTagToDataViewRecord,
    private val addStatusToDataViewRecord: AddStatusToDataViewRecord,
    private val dispatcher: Dispatcher<Payload>,
) : AddObjectRelationValueViewModel(
    details = details,
    values = values,
    types = types,
    urlBuilder = urlBuilder,
    relations = relations,
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
                    color = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random().title
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
        status: RelationValueBaseViewModel.RelationValueView.Status
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
        val tags = views.value.mapNotNull { view ->
            if (view is RelationValueBaseViewModel.RelationValueView.Tag && view.isSelected == true)
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
        private val details: ObjectDetailProvider,
        private val relations: ObjectRelationProvider,
        private val types: ObjectTypesProvider,
        private val addDataViewRelationOption: AddDataViewRelationOption,
        private val addTagToDataViewRecord: AddTagToDataViewRecord,
        private val addStatusToDataViewRecord: AddStatusToDataViewRecord,
        private val urlBuilder: UrlBuilder,
        private val dispatcher: Dispatcher<Payload>,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationOptionValueDVAddViewModel(
                details = details,
                values = values,
                relations = relations,
                types = types,
                urlBuilder = urlBuilder,
                addDataViewRelationOption = addDataViewRelationOption,
                addTagToDataViewRecord = addTagToDataViewRecord,
                addStatusToDataViewRecord = addStatusToDataViewRecord,
                dispatcher = dispatcher,
            ) as T
        }
    }
}