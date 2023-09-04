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
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.launch
import timber.log.Timber

class AddOptionsRelationViewModel(
    values: ObjectValueProvider,
    relations: ObjectRelationProvider,
    optionsProvider: AddOptionsRelationProvider,
    getOptions: GetOptions,
    private val createRelationOption: CreateRelationOption,
    private val updateDetail: UpdateDetail,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    private val detailProvider: ObjectDetailProvider,
    private val spaceManager: SpaceManager
) : BaseAddOptionsRelationViewModel(
    values = values,
    relations = relations,
    optionsProvider = optionsProvider,
    analytics = analytics,
    dispatcher = dispatcher,
    setObjectDetail = updateDetail,
    detailsProvider = detailProvider,
    getOptions = getOptions
) {

    fun onAddObjectStatusClicked(
        ctx: Id,
        relationKey: Key,
        status: RelationValueView.Option.Status
    ) = proceedWithAddingStatusToObject(
        target = ctx,
        relationKey = relationKey,
        status = status.id
    )

    fun onAddSelectedValuesToObjectClicked(
        ctx: Id,
        obj: Id,
        relationKey: Key,
    ) {
        val tags = choosingRelationOptions.value.mapNotNull { view ->
            if (view is RelationValueView.Option.Tag && view.isSelected)
                view.id
            else
                null
        }
        proceedWithAddingTagToObject(
            ctx =  ctx,
            target = obj,
            relationKey = relationKey,
            tags = tags
        )
    }

    fun onCreateObjectRelationOptionClicked(
        ctx: Id,
        relationKey: Key,
        name: String,
        obj: Id
    ) {
        viewModelScope.launch {
            createRelationOption(
                CreateRelationOption.Params(
                    space = spaceManager.get(),
                    relation = relationKey,
                    name = name,
                    color = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random().code
                )
            ).proceed(
                success = { option ->
                    when (val format = relations.get(relationKey).format) {
                        RelationFormat.TAG -> {
                            proceedWithAddingTagToObject(
                                ctx = ctx,
                                relationKey = relationKey,
                                target = obj,
                                tags = listOf(option.id)
                            )
                        }
                        RelationFormat.STATUS -> {
                            proceedWithAddingStatusToObject(
                                target = obj,
                                relationKey = relationKey,
                                status = option.id
                            )
                        }
                        else -> {
                            Timber.e("Trying to create an option for relation format: $format")
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
        private val createRelationOption: CreateRelationOption,
        private val updateDetail: UpdateDetail,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics,
        private val optionsProvider: AddOptionsRelationProvider,
        private val detailProvider: ObjectDetailProvider,
        private val getOptions: GetOptions,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddOptionsRelationViewModel(
                values = values,
                relations = relations,
                createRelationOption = createRelationOption,
                updateDetail = updateDetail,
                dispatcher = dispatcher,
                analytics = analytics,
                optionsProvider = optionsProvider,
                detailProvider = detailProvider,
                getOptions = getOptions,
                spaceManager = spaceManager
            ) as T
        }
    }
}