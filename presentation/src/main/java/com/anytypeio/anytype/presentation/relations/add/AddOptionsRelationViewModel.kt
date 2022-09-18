package com.anytypeio.anytype.presentation.relations.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.relations.AddObjectRelationOption
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationValueEvent
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.launch
import timber.log.Timber

class AddOptionsRelationViewModel(
    values: ObjectValueProvider,
    relations: ObjectRelationProvider,
    optionsProvider: AddOptionsRelationProvider,
    private val addObjectRelationOption: AddObjectRelationOption,
    private val updateDetail: UpdateDetail,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics
) : BaseAddOptionsRelationViewModel(
    values = values,
    relations = relations,
    optionsProvider = optionsProvider,
    analytics = analytics,
    dispatcher = dispatcher,
    setObjectDetail = updateDetail
) {

    fun onAddObjectStatusClicked(
        ctx: Id,
        relation: Id,
        status: RelationValueView.Option.Status
    ) = proceedWithAddingStatusToObject(
        ctx = ctx,
        relation = relation,
        status = status.id
    )

    fun onAddSelectedValuesToObjectClicked(
        ctx: Id,
        obj: Id,
        relation: Id
    ) {
        val tags = choosingRelationOptions.value.mapNotNull { view ->
            if (view is RelationValueView.Option.Tag && view.isSelected)
                view.id
            else
                null
        }
        proceedWithAddingTagToObject(
            target = obj,
            ctx = ctx,
            relation = relation,
            tags = tags
        )
    }

    fun onCreateObjectRelationOptionClicked(
        ctx: Id,
        relation: Id,
        name: String,
        obj: Id
    ) {
        viewModelScope.launch {
            addObjectRelationOption(
                AddObjectRelationOption.Params(
                    ctx = ctx,
                    relation = relation,
                    name = name,
                    color = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random().code
                )
            ).proceed(
                success = { (payload, option) ->
                    dispatcher.send(payload)
                    if (option != null) {
                        when (val format = relations.get(relation).format) {
                            Relation.Format.TAG -> {
                                proceedWithAddingTagToObject(
                                    ctx = ctx,
                                    relation = relation,
                                    target = obj,
                                    tags = listOf(option)
                                )
                            }
                            Relation.Format.STATUS -> {
                                proceedWithAddingStatusToObject(
                                    ctx = ctx,
                                    relation = relation,
                                    status = option
                                )
                            }
                            else -> {
                                Timber.e("Trying to create an option for relation format: $format")
                            }
                        }
                    }
                },
                failure = { Timber.e(it, "Error while creating a new option for object") }
            )
        }
    }

    class Factory(
        private val values: ObjectValueProvider,
        private val relations: ObjectRelationProvider,
        private val addObjectRelationOption: AddObjectRelationOption,
        private val updateDetail: UpdateDetail,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics,
        private val optionsProvider: AddOptionsRelationProvider,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddOptionsRelationViewModel(
                values = values,
                relations = relations,
                addObjectRelationOption = addObjectRelationOption,
                updateDetail = updateDetail,
                dispatcher = dispatcher,
                analytics = analytics,
                optionsProvider = optionsProvider
            ) as T
        }
    }
}