package com.anytypeio.anytype.presentation.relations.add

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.options.GetOptions
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationValueEvent
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.relations.RelationValueView.Option.Tag
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationProvider.Options
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber


abstract class BaseAddOptionsRelationViewModel(
    private val optionsProvider: AddOptionsRelationProvider,
    private val setObjectDetail: UpdateDetail,
    private val analytics: Analytics,
    private val dispatcher: Dispatcher<Payload>,
    protected val values: ObjectValueProvider,
    protected val relations: ObjectRelationProvider,
    protected val detailsProvider: ObjectDetailProvider,
    private val getOptions: GetOptions
) : BaseViewModel() {

    private val jobs = mutableListOf<Job>()

    private val query = MutableStateFlow("")
    private val allRelationOptions = MutableStateFlow(Options(emptyList(), emptyList()))
    protected val choosingRelationOptions = MutableStateFlow(emptyList<RelationValueView>())

    val ui = MutableStateFlow(listOf<RelationValueView>())
    val isAddButtonVisible = MutableStateFlow(true)
    val counter = MutableStateFlow(0)
    val isDismissed = MutableStateFlow(false)
    val isParentDismissed = MutableStateFlow(false)

    val isMultiple = MutableStateFlow(true)

    private val logger = Timber.tag("BaseAddOptionsRelation")

    init {
        viewModelScope.launch {
            allRelationOptions.collect { all ->
                choosingRelationOptions.value = all.notSelected
            }
        }
        viewModelScope.launch {
            choosingRelationOptions.combine(query) { choosing, query ->
                filterRelationsBy(query, allRelationOptions.value.all, choosing)
            }.collect {
                logger.i("Update ui: $it")
                ui.value = it
            }
        }
    }

    private fun filterRelationsBy(
        query: String,
        all: List<RelationValueView>,
        choosing: List<RelationValueView>
    ): List<RelationValueView> {
        return if (query.isEmpty())
            choosing
        else {
            val result = mutableListOf<RelationValueView>()
            val queriedOptions = choosing.filterIsInstance<RelationValueView.Option>()
                .filter { it.name.contains(query, true) }
            val searchedExistent = all.filterIsInstance<RelationValueView.Option>()
                .any {
                    it.name == query
                }
            if (!searchedExistent) {
                result.add(RelationValueView.Create(query))
            }
            result.addAll(queriedOptions)
            result.ifEmpty { listOf(RelationValueView.Empty) }
        }
    }

    fun onStart(
        ctx: Id,
        target: Id,
        relationKey: Key
    ) {
        jobs += viewModelScope.launch {
            combine(
                relations.observe(relationKey),
                values.subscribe(ctx = ctx, target = target)
            ) { relation, record ->
                if (relation.format == Relation.Format.STATUS) {
                    isAddButtonVisible.value = false
                    isMultiple.value = false
                }
                getOptions(GetOptions.Params(relationKey)).proceed(
                    success = { options ->
                        buildViews(
                            relation = relation,
                            record = record,
                            relationKey = relationKey,
                            options = options
                        )
                    },
                    failure = {
                        Timber.e(it, "Error while getting options by id")
                    }
                )
            }.collect()
        }
    }

    fun onStop() {
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
    }

    private fun buildViews(
        relation: ObjectWrapper.Relation,
        record: Map<String, Any?>,
        relationKey: Key,
        options: List<ObjectWrapper.Option>
    ) {
        allRelationOptions.value = optionsProvider.provideOptions(
            options = options,
            relation = relation,
            record = record,
            relationKey = relationKey
        )
    }

    fun onFilterInputChanged(input: String) {
        query.value = input
    }

    fun onTagClicked(tag: Tag) {
        val oldOptions = choosingRelationOptions.value
        val newOptions = oldOptions
            .map { view ->
                if (view is Tag && view.id == tag.id) {
                    view.copy(isSelected = !view.isSelected)
                } else {
                    view
                }
            }
        choosingRelationOptions.value = newOptions
        counter.value =
            newOptions.count { it is Tag && it.isSelected }
    }

    fun proceedWithAddingTagToObject(
        ctx: Id,
        target: Id,
        relationKey: Key,
        tags: List<Id>
    ) {
        Timber.d("Relations | Adding tag to object with id: $target")
        viewModelScope.launch {
            val obj = values.get(target = target, ctx = ctx)
            val result = mutableListOf<Id>()
            val value = obj[relationKey]
            if (value is List<*>) {
                result.addAll(value.typeOf())
            } else if (value is Id) {
                result.add(value)
            }
            result.addAll(tags)
            setObjectDetail(
                UpdateDetail.Params(
                    target = target,
                    key = relationKey,
                    value = result
                )
            ).process(
                failure = { Timber.e(it, "Error while adding tag to object") },
                success = {
                    dispatcher.send(it).also {
                        sendAnalyticsRelationValueEvent(analytics)
                        isDismissed.value = true
                    }
                }
            )
        }
    }

    fun proceedWithAddingStatusToObject(
        target: Id,
        relationKey: Key,
        status: Id
    ) {
        viewModelScope.launch {
            setObjectDetail(
                UpdateDetail.Params(
                    target = target,
                    key = relationKey,
                    value = listOf(status)
                )
            ).process(
                failure = { Timber.e(it, "Error while adding status to object") },
                success = {
                    dispatcher.send(it).also {
                        sendAnalyticsRelationValueEvent(analytics)
                        isParentDismissed.value = true
                    }
                }
            )
        }
    }
}

