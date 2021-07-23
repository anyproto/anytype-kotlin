package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_utils.diff.DefaultObjectDiffIdentifier
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.dataview.interactor.ObjectRelationList
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.relations.AddToFeaturedRelations
import com.anytypeio.anytype.domain.relations.RemoveFromFeaturedRelations
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.page.Editor
import com.anytypeio.anytype.presentation.page.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class RelationListViewModel(
    private val stores: Editor.Storage,
    private val urlBuilder: UrlBuilder,
    private val objectRelationList: ObjectRelationList,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDetail: UpdateDetail,
    private val detailModificationManager: DetailModificationManager,
    private val addToFeaturedRelations: AddToFeaturedRelations,
    private val removeFromFeaturedRelations: RemoveFromFeaturedRelations
) : BaseViewModel() {

    private val jobs = mutableListOf<Job>()

    private val isInAddMode = MutableStateFlow(false)
    val commands = MutableSharedFlow<Command>(replay = 0)
    val views = MutableStateFlow<List<Model>>(emptyList())

    fun onStartListMode(ctx: Id) {
        isInAddMode.value = false
        jobs += viewModelScope.launch {
            stores.relations.stream().combine(stores.details.stream()) { relations, details ->
                val detail = details.details[ctx]
                val values = detail?.map ?: emptyMap()
                val featured = detail?.featuredRelations ?: emptyList()
                relations.views(details, values, urlBuilder, featured).map { Model.Item(it) }
            }.map { views ->
                val result = mutableListOf<Model>().apply {
                    val (isFeatured, other) = views.partition { it.view.isFeatured }
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

    fun onRelationClicked(ctx: Id, target: Id?, view: DocumentRelationView) {
        if (isInAddMode.value) {
            onRelationClickedAddMode(target = target, view = view)
        } else {
            onRelationClickedListMode(ctx = ctx, view = view)
        }
    }

    fun onCheckboxClicked(ctx: Id, view: DocumentRelationView) {
        viewModelScope.launch {
            val details = stores.details.current().details[ctx]
            val current = details?.featuredRelations ?: emptyList()
            if (view.isFeatured) {
                viewModelScope.launch {
                    removeFromFeaturedRelations(
                        RemoveFromFeaturedRelations.Params(
                            ctx = ctx,
                            relations = listOf(view.relationId)
                        )
                    ).process(
                        failure = { Timber.e(it, "Error while removing from featured relations") },
                        success = { dispatcher.send(it) }
                    )
                }
            } else {
                if (current.size < MAX_FEATURED_RELATION_COUNT) {
                    viewModelScope.launch {
                        addToFeaturedRelations(
                            AddToFeaturedRelations.Params(
                                ctx = ctx,
                                relations = listOf(view.relationId)
                            )
                        ).process(
                            failure = { Timber.e(it, "Error while adding to featured relations") },
                            success = { dispatcher.send(it) }
                        )
                    }
                } else {
                    _toasts.emit(MAX_FEATURED_RELATION_COUNT_ERROR)
                }
            }
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
            if (relation.isReadOnly) {
                _toasts.emit(NOT_ALLOWED_FOR_RELATION)
                Timber.d("No interaction allowed with this relation")
                return@launch
            }
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
                    views.value = list.views(details, values, urlBuilder).map { Model.Item(it) }
                }
            )
        }
    }

    fun onRelationTextValueChanged(
        ctx: Id,
        value: Any?,
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

    sealed class Model : DefaultObjectDiffIdentifier {
        sealed class Section : Model() {
            object Featured : Section() {
                override val identifier: String get() = "Section_Featured"
            }

            object Other : Section() {
                override val identifier: String get() = "Section_Other"
            }

            sealed class SlashWidget : Section() {
                object Subheader : SlashWidget() {
                    override val identifier: String
                        get() = "Slash_Subheader"
                }
                object SubheaderWithBack : SlashWidget() {
                    override val identifier: String
                        get() = "Slash_Subheader_Back"

                }

            }
        }

        data class Item(val view: DocumentRelationView) : Model() {
            override val identifier: String get() = view.identifier
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

    companion object {
        const val MAX_FEATURED_RELATION_COUNT = 5
        const val MAX_FEATURED_RELATION_COUNT_ERROR = "Currently you cannot create more than $MAX_FEATURED_RELATION_COUNT featured relations"
        const val NOT_ALLOWED_FOR_RELATION = "Not allowed for this relation"
    }
}