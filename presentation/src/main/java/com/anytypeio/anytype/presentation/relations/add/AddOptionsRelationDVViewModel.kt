package com.anytypeio.anytype.presentation.relations.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.options.GetOptions
import com.anytypeio.anytype.domain.relations.CreateRelationOption
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.launch
import timber.log.Timber

class AddOptionsRelationDVViewModel(
    values: ObjectValueProvider,
    relations: ObjectRelationProvider,
    optionsProvider: AddOptionsRelationProvider,
    detailsProvider: ObjectDetailProvider,
    getOptions: GetOptions,
    private val createRelationOption: CreateRelationOption,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    private val setObjectDetail: UpdateDetail
) : BaseAddOptionsRelationViewModel(
    values = values,
    relations = relations,
    optionsProvider = optionsProvider,
    dispatcher = dispatcher,
    analytics = analytics,
    setObjectDetail = setObjectDetail,
    detailsProvider = detailsProvider,
    getOptions = getOptions
) {

    fun onCreateDataViewRelationOptionClicked(
        ctx: Id,
        relationKey: Key,
        target: Id,
        name: String
    ) {
        viewModelScope.launch {
            createRelationOption(
                CreateRelationOption.Params(
                    relation = relationKey,
                    name = name,
                    color = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random().code
                )
            ).proceed(
                success = { option ->
                    Timber.d("Created new option with id: ${option.id}")
                    when (relations.get(relation = relationKey).format) {
                        RelationFormat.TAG -> {
                            proceedWithAddingTagToObject(
                                ctx = ctx,
                                target = target,
                                relationKey = relationKey,
                                tags = listOf(option.id)
                            )
                        }
                        RelationFormat.STATUS -> {
                            proceedWithAddingStatusToObject(
                                target = target,
                                relationKey = relationKey,
                                status = option.id
                            )
                        }
                        else -> Timber.e("Trying to create option for wrong relation.")
                    }
                },
                failure = { Timber.e(it, "Error while creating a new option") }
            )
        }
    }

    fun onAddObjectSetStatusClicked(
        relationKey: Key,
        obj: Id,
        status: RelationValueView.Option.Status
    ) {
        proceedWithAddingStatusToObject(
            target = obj,
            relationKey = relationKey,
            status = status.id
        )
    }

    fun onAddSelectedValuesToDataViewClicked(
        ctx: Id,
        target: Id,
        relationKey: Key
    ) {
        val tags = choosingRelationOptions.value.mapNotNull { view ->
            if (view is RelationValueView.Option.Tag && view.isSelected)
                view.id
            else
                null
        }
        proceedWithAddingTagToObject(
            ctx = ctx,
            relationKey = relationKey,
            tags = tags,
            target = target
        )
    }

    class Factory(
        private val values: ObjectValueProvider,
        private val relations: ObjectRelationProvider,
        private val createRelationOption: CreateRelationOption,
        private val dispatcher: Dispatcher<Payload>,
        private val optionsProvider: AddOptionsRelationProvider,
        private val setObjectDetail: UpdateDetail,
        private val analytics: Analytics,
        private val detailsProvider: ObjectDetailProvider,
        private val getOptions: GetOptions
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddOptionsRelationDVViewModel(
                values = values,
                relations = relations,
                createRelationOption = createRelationOption,
                dispatcher = dispatcher,
                optionsProvider = optionsProvider,
                setObjectDetail = setObjectDetail,
                analytics = analytics,
                detailsProvider = detailsProvider,
                getOptions = getOptions
            ) as T
        }
    }
}