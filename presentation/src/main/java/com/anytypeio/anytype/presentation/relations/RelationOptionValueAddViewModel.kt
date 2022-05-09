package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.relations.AddObjectRelationOption
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationValueEvent
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.launch
import timber.log.Timber

class RelationOptionValueAddViewModel(
    details: ObjectDetailProvider,
    types: ObjectTypesProvider,
    urlBuilder: UrlBuilder,
    values: ObjectValueProvider,
    relations: ObjectRelationProvider,
    private val addObjectRelationOption: AddObjectRelationOption,
    private val updateDetail: UpdateDetail,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics
) : AddObjectRelationValueViewModel(
    details = details,
    values = values,
    types = types,
    urlBuilder = urlBuilder,
    relations = relations,
) {

    fun onAddObjectStatusClicked(
        ctx: Id,
        relation: Id,
        status: RelationValueBaseViewModel.RelationValueView.Status
    ) = proceedWithAddingStatusToObject(
        ctx = ctx,
        relation = relation,
        status = status.id
    )

    private fun proceedWithAddingStatusToObject(
        ctx: Id,
        relation: Id,
        status: Id
    ) {
        viewModelScope.launch {
            updateDetail(
                UpdateDetail.Params(
                    ctx = ctx,
                    key = relation,
                    value = listOf(status)
                )
            ).process(
                failure = { Timber.e(it, "Error while adding tag") },
                success = {
                    dispatcher.send(it).also {
                        sendAnalyticsRelationValueEvent(analytics)
                        isParentDismissed.value = true
                    }
                }
            )
        }
    }

    fun onAddSelectedValuesToObjectClicked(
        ctx: Id,
        obj: Id,
        relation: Id
    ) {
        val tags = views.value.mapNotNull { view ->
            if (view is RelationValueBaseViewModel.RelationValueView.Tag && view.isSelected == true)
                view.id
            else
                null
        }
        proceedWithAddingTagToObject(
            obj = obj,
            ctx = ctx,
            relation = relation,
            tags = tags
        )
    }

    private fun proceedWithAddingTagToObject(
        obj: Id,
        ctx: Id,
        relation: Id,
        tags: List<Id>
    ) {
        viewModelScope.launch {
            val obj = values.get(target = obj)
            val result = mutableListOf<Id>()
            val value = obj[relation]
            if (value is List<*>) {
                result.addAll(value.typeOf())
            } else if (value is Id) {
                result.add(value)
            }
            result.addAll(tags)
            updateDetail(
                UpdateDetail.Params(
                    ctx = ctx,
                    key = relation,
                    value = result
                )
            ).process(
                failure = { Timber.e(it, "Error while adding tag") },
                success = {
                    dispatcher.send(it).also {
                        sendAnalyticsRelationValueEvent(analytics)
                        isDismissed.value = true
                    }
                }
            )
        }
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
                    color = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random().title
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
                                    obj = obj,
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
        private val details: ObjectDetailProvider,
        private val relations: ObjectRelationProvider,
        private val types: ObjectTypesProvider,
        private val addObjectRelationOption: AddObjectRelationOption,
        private val updateDetail: UpdateDetail,
        private val urlBuilder: UrlBuilder,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationOptionValueAddViewModel(
                details = details,
                values = values,
                relations = relations,
                types = types,
                urlBuilder = urlBuilder,
                addObjectRelationOption = addObjectRelationOption,
                updateDetail = updateDetail,
                dispatcher = dispatcher,
                analytics = analytics
            ) as T
        }
    }
}