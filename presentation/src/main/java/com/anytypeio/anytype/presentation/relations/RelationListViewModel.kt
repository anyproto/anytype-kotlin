package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.objectRelationFeature
import com.anytypeio.anytype.analytics.base.EventsDictionary.objectRelationUnfeature
import com.anytypeio.anytype.analytics.base.EventsDictionary.relationsScreenShow
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_utils.diff.DefaultObjectDiffIdentifier
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.AddToFeaturedRelations
import com.anytypeio.anytype.domain.relations.DeleteRelationFromObject
import com.anytypeio.anytype.domain.relations.RemoveFromFeaturedRelations
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationDeleteEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationValueEvent
import com.anytypeio.anytype.presentation.objects.LockedStateProvider
import com.anytypeio.anytype.presentation.objects.getProperType
import com.anytypeio.anytype.presentation.relations.model.RelationOperationError
import com.anytypeio.anytype.presentation.relations.providers.RelationListProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class RelationListViewModel(
    private val relationListProvider: RelationListProvider,
    private val lockedStateProvider: LockedStateProvider,
    private val urlBuilder: UrlBuilder,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDetail: UpdateDetail,
    private val addToFeaturedRelations: AddToFeaturedRelations,
    private val removeFromFeaturedRelations: RemoveFromFeaturedRelations,
    private val deleteRelationFromObject: DeleteRelationFromObject,
    private val analytics: Analytics,
    private val storeOfRelations: StoreOfRelations
) : BaseViewModel() {

    val isEditMode = MutableStateFlow(false)

    private val jobs = mutableListOf<Job>()

    private val isInAddMode = MutableStateFlow(false)
    val commands = MutableSharedFlow<Command>(replay = 0)
    val views = MutableStateFlow<List<Model>>(emptyList())

    fun onStartListMode(ctx: Id) {
        isInAddMode.value = false
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = relationsScreenShow
        )
        jobs += viewModelScope.launch {
            combine(
                storeOfRelations.trackChanges(),
                relationListProvider.links,
                relationListProvider.details
            ) { _, relationLinks, details ->

                val objectDetails = details.details[ctx]?.map ?: emptyMap()
                val objectWrapper = ObjectWrapper.Basic(objectDetails)
                val objectType = objectWrapper.getProperType()
                val objectTypeWrapper =
                    ObjectWrapper.Type(details.details[objectType]?.map ?: emptyMap())

                val objectRelations = objectRelations(
                    systemRelations = listOf(),
                    relationLinks = relationLinks,
                    recommendedRelations = objectTypeWrapper.recommendedRelations,
                    storeOfRelations = storeOfRelations
                )

                objectRelations.views(
                    context = ctx,
                    details = details,
                    values = objectDetails,
                    urlBuilder = urlBuilder,
                    featured = objectWrapper.featuredRelations
                ).map { view ->
                    Model.Item(
                        view = view,
                        isRemovable = isPossibleToRemoveRelation(relationKey = view.key)
                    )
                }
            }.map { views ->
                val result = mutableListOf<Model>().apply {
                    val (isFeatured, other) = views.partition { it.view.featured }
                    if (isFeatured.isNotEmpty()) {
                        add(Model.Section.Featured)
                        addAll(isFeatured)
                    }
                    if (other.isNotEmpty()) {
                        add(Model.Section.Other)
                        addAll(other)
                    }
                }
                result
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

    fun onRelationClicked(ctx: Id, target: Id?, view: ObjectRelationView) {
        if (isInAddMode.value) {
            onRelationClickedAddMode(target = target, view = view)
        } else {
            onRelationClickedListMode(ctx = ctx, view = view)
        }
    }

    fun onCheckboxClicked(ctx: Id, view: ObjectRelationView) {
        val isLocked = resolveIsLockedState(ctx)
        if (isLocked) {
            sendToast(RelationOperationError.LOCKED_OBJECT_MODIFICATION_ERROR)
        } else {
            proceedWithUpdatingFeaturedRelations(view, ctx)
        }
    }

    private fun proceedWithUpdatingFeaturedRelations(
        view: ObjectRelationView,
        ctx: Id
    ) {
        val relationKey = view.key
        viewModelScope.launch {
            if (view.featured) {
                viewModelScope.launch {
                    removeFromFeaturedRelations(
                        RemoveFromFeaturedRelations.Params(
                            ctx = ctx,
                            relations = listOf(relationKey)
                        )
                    ).process(
                        failure = { Timber.e(it, "Error while removing from featured relations") },
                        success = {
                            dispatcher.send(it)
                            sendEvent(
                                analytics = analytics,
                                eventName = objectRelationUnfeature
                            )
                        }
                    )
                }
            } else {
                viewModelScope.launch {
                    addToFeaturedRelations(
                        AddToFeaturedRelations.Params(
                            ctx = ctx,
                            relations = listOf(relationKey)
                        )
                    ).process(
                        failure = { Timber.e(it, "Error while adding to featured relations") },
                        success = {
                            dispatcher.send(it)
                            sendEvent(
                                analytics = analytics,
                                eventName = objectRelationFeature
                            )
                        }
                    )
                }
            }
        }
    }

    fun onDeleteClicked(ctx: Id, view: ObjectRelationView) {
        viewModelScope.launch {
            deleteRelationFromObject(
                DeleteRelationFromObject.Params(
                    ctx = ctx,
                    relation = view.key
                )
            ).process(
                failure = { Timber.e(it, "Error while deleting relation") },
                success = {
                    dispatcher.send(it)
                    sendAnalyticsRelationDeleteEvent(analytics)
                }
            )
        }
    }

    fun onEditOrDoneClicked(isLocked: Boolean) {
        if (isLocked) {
            sendToast(RelationOperationError.LOCKED_OBJECT_MODIFICATION_ERROR)
        } else {
            isEditMode.value = !isEditMode.value
            views.value = views.value.map { view ->
                if (view is Model.Item) {
                    view.copy(isRemovable = isPossibleToRemoveRelation(relationKey = view.view.key))
                } else {
                    view
                }
            }
        }
    }

    private fun isPossibleToRemoveRelation(relationKey: Key): Boolean {
        return isEditMode.value && !Relations.systemRelationKeys.contains(relationKey)
    }

    private fun onRelationClickedAddMode(
        target: Id?,
        view: ObjectRelationView
    ) {
        checkNotNull(target)
        viewModelScope.launch {
            commands.emit(
                Command.SetRelationKey(
                    blockId = target,
                    key = view.key
                )
            )
        }
    }

    private fun onRelationClickedListMode(ctx: Id, view: ObjectRelationView) {
        viewModelScope.launch {
            val relation = storeOfRelations.getById(view.id)
            if (relation == null) {
                if (BuildConfig.DEBUG) {
                    _toasts.emit("$NOT_FOUND_IN_RELATION_STORE[${view.id}]")
                }
                Timber.w("Couldn't find relation in store by id:${view.id}")
                return@launch
            }
            if (relation.isReadonlyValue) {
                _toasts.emit(NOT_ALLOWED_FOR_RELATION)
                Timber.d("No interaction allowed with this relation")
                return@launch
            }
            when (relation.format) {
                RelationFormat.SHORT_TEXT,
                RelationFormat.LONG_TEXT,
                RelationFormat.NUMBER,
                RelationFormat.URL,
                RelationFormat.EMAIL,
                RelationFormat.PHONE -> {
                    commands.emit(
                        Command.EditTextRelationValue(
                            ctx = ctx,
                            relationId = relation.id,
                            relationKey = relation.key,
                            target = ctx,
                            isLocked = resolveIsLockedState(ctx)
                        )
                    )
                }
                RelationFormat.CHECKBOX -> {
                    proceedWithTogglingRelationCheckboxValue(view, ctx)
                }
                RelationFormat.DATE -> {
                    commands.emit(
                        Command.EditDateRelationValue(
                            ctx = ctx,
                            relationId = relation.id,
                            relationKey = relation.key,
                            target = ctx
                        )
                    )
                }
                RelationFormat.STATUS -> {
                    commands.emit(
                        Command.EditStatusRelationValue(
                            ctx = ctx,
                            relationId = relation.id,
                            relationKey = relation.key,
                            target = ctx,
                            targetObjectTypes = relation.relationFormatObjectTypes,
                            isLocked = resolveIsLockedState(ctx)
                        )
                    )
                }
                RelationFormat.TAG,
                RelationFormat.FILE,
                RelationFormat.OBJECT -> {
                    commands.emit(
                        Command.EditRelationValue(
                            ctx = ctx,
                            relationId = relation.id,
                            relationKey = relation.key,
                            target = ctx,
                            targetObjectTypes = relation.relationFormatObjectTypes,
                            isLocked = resolveIsLockedState(ctx)
                        )
                    )
                }
                RelationFormat.EMOJI,
                RelationFormat.RELATIONS,
                RelationFormat.UNDEFINED -> {
                    _toasts.emit(NOT_SUPPORTED_UPDATE_VALUE)
                    Timber.d("Update value of relation with format:[${relation.format}] is not supported")
                }
                else -> {}
            }
        }
    }

    private fun resolveIsLockedState(ctx: Id): Boolean = lockedStateProvider.isLocked(ctx)

    private fun proceedWithTogglingRelationCheckboxValue(view: ObjectRelationView, ctx: Id) {
        viewModelScope.launch {
            check(view is ObjectRelationView.Checkbox)
            updateDetail(
                UpdateDetail.Params(
                    target = ctx,
                    key = view.key,
                    value = !view.isChecked
                )
            ).process(
                success = {
                    dispatcher.send(it)
                    sendAnalyticsRelationValueEvent(analytics)
                },
                failure = { Timber.e(it, "Error while updating checkbox relation") }
            )
        }
    }

    private fun getRelations(ctx: Id) {
        viewModelScope.launch {
            val relations = relationListProvider.getLinks().mapNotNull { storeOfRelations.getByKey(it.key) }
            val details = relationListProvider.getDetails()
            val values = details.details[ctx]?.map ?: emptyMap()
            views.value = relations.views(
                    details = details,
                    values = values,
                    urlBuilder = urlBuilder
            ).map { Model.Item(it) }
        }
    }

    fun onRelationTextValueChanged(
        ctx: Id,
        value: Any?,
        relationKey: Key
    ) {
        viewModelScope.launch {
            updateDetail(
                UpdateDetail.Params(
                    target = ctx,
                    key = relationKey,
                    value = value
                )
            ).process(
                success = { payload ->
                    if (payload.events.isNotEmpty()) dispatcher.send(payload)
                    sendAnalyticsRelationValueEvent(analytics)
                },
                failure = { Timber.e(it, "Error while updating relation values") }
            )
        }
    }

    sealed class Model : DefaultObjectDiffIdentifier {
        sealed class Section : Model() {
            object Featured : Section() {
                override val identifier: String get() = "Section_Featured"
            }

            object Other : Section() {
                override val identifier: String get() = "Section_Other"
            }
        }

        data class Item(
            val view: ObjectRelationView,
            val isRemovable: Boolean = false
        ) : Model() {
            override val identifier: String get() = view.identifier
        }
    }

    sealed class Command {
        data class EditTextRelationValue(
            val ctx: Id,
            val relationId: Id,
            val relationKey: Key,
            val target: Id,
            val isLocked: Boolean = false
        ) : Command()

        data class EditDateRelationValue(
            val ctx: Id,
            val relationId: Id,
            val relationKey: Key,
            val target: Id
        ) : Command()

        data class EditRelationValue(
            val ctx: Id,
            val relationId: Id,
            val relationKey: Key,
            val target: Id,
            val targetObjectTypes: List<Id>,
            val isLocked: Boolean = false
        ) : Command()

        data class EditStatusRelationValue(
            val ctx: Id,
            val relationId: Id,
            val relationKey: Key,
            val target: Id,
            val targetObjectTypes: List<Id>,
            val isLocked: Boolean = false
        ) : Command()

        data class SetRelationKey(
            val blockId: Id,
            val key: Id
        ) : Command()
    }

    companion object {
        const val NOT_ALLOWED_FOR_RELATION = "Not allowed for this relation"
        const val NOT_FOUND_IN_RELATION_STORE = "Couldn't find in relation store by id:"
        const val NOT_SUPPORTED_UPDATE_VALUE = "Update value of this relation isn't supported"
    }
}