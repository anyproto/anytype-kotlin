package com.anytypeio.anytype.presentation.relations.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewRelationOption
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
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    private val setObjectDetail: UpdateDetail
) : BaseAddOptionsRelationViewModel(
    values = values,
    relations = relations,
    optionsProvider = optionsProvider,
    dispatcher = dispatcher,
    analytics = analytics,
    setObjectDetail = setObjectDetail
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
                                proceedWithAddingTagToObject(
                                    target = target,
                                    ctx = ctx,
                                    relation = relation,
                                    tags = listOf(option)
                                )
                            }
                            Relation.Format.STATUS -> {
                                proceedWithAddingStatusToObject(
                                    ctx = target,
                                    relation = relation,
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
        proceedWithAddingStatusToObject(
            ctx = obj,
            relation = relation,
            status = status.id
        )
    }

    fun onAddSelectedValuesToDataViewClicked(
        ctx: Id,
        target: Id,
        relation: Id
    ) {
        val tags = choosingRelationOptions.value.mapNotNull { view ->
            if (view is RelationValueView.Option.Tag && view.isSelected)
                view.id
            else
                null
        }
        proceedWithAddingTagToObject(
            ctx = ctx,
            relation = relation,
            tags = tags,
            target = target
        )
    }

    class Factory(
        private val values: ObjectValueProvider,
        private val relations: ObjectRelationProvider,
        private val addDataViewRelationOption: AddDataViewRelationOption,
        private val dispatcher: Dispatcher<Payload>,
        private val optionsProvider: AddOptionsRelationProvider,
        private val setObjectDetail: UpdateDetail,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddOptionsRelationDVViewModel(
                values = values,
                relations = relations,
                addDataViewRelationOption = addDataViewRelationOption,
                dispatcher = dispatcher,
                optionsProvider = optionsProvider,
                setObjectDetail = setObjectDetail,
                analytics = analytics
            ) as T
        }
    }
}