package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations

/**
 * Use-case for updating data view's viewer.
 */
class UpdateDataViewViewer(
    private val repo: BlockRepository
) : BaseUseCase<Payload, UpdateDataViewViewer.Params>() {

    override suspend fun run(params: Params) = safe {
        when (params) {
            is Params.Filter.Add -> {
                val command = Command.AddFilter(
                    ctx = params.ctx,
                    dv = params.dv,
                    view = params.view,
                    relationKey = params.relationKey,
                    relationFormat = params.relationFormat,
                    operator = params.operator,
                    condition = params.condition,
                    quickOption = params.quickOption,
                    value = params.value
                )
                repo.addDataViewFilter(command = command)
            }
            is Params.Filter.Remove -> {
                val command = Command.RemoveFilter(
                    ctx = params.ctx,
                    dv = params.dv,
                    view = params.view,
                    ids = params.ids
                )
                repo.removeDataViewFilter(command)
            }
            is Params.Filter.Replace -> {
                val command = Command.ReplaceFilter(
                    ctx = params.ctx,
                    dv = params.dv,
                    view = params.view,
                    id = params.filter.id,
                    filter = params.filter
                )
                repo.replaceDataViewFilter(command)
            }
            is Params.Sort.Add -> {
                val command = Command.AddSort(
                    ctx = params.ctx,
                    dv = params.dv,
                    view = params.view,
                    relationKey = params.sort.relationKey,
                    type = params.sort.type,
                    includeTime = sortIncludeTimeKeys.contains(params.sort.relationKey)
                )
                repo.addDataViewSort(command)
            }
            is Params.Sort.Remove -> {
                val command = Command.RemoveSort(
                    ctx = params.ctx,
                    dv = params.dv,
                    view = params.view,
                    ids = params.ids
                )
                repo.removeDataViewSort(command)
            }
            is Params.Sort.Replace -> {
                val sort = if (sortIncludeTimeKeys.contains(params.sort.relationKey)) {
                    params.sort.copy(includeTime = true)
                } else {
                    params.sort
                }
                val command = Command.ReplaceSort(
                    ctx = params.ctx,
                    dv = params.dv,
                    view = params.view,
                    sort = sort
                )
                repo.replaceDataViewSort(command)
            }
            is Params.ViewerRelation.Add -> {
                val command = Command.AddRelation(
                    ctx = params.ctx,
                    dv = params.dv,
                    view = params.view,
                    relation = params.relation
                )
                repo.addDataViewViewRelation(command)
            }
            is Params.ViewerRelation.Remove -> {
                val command = Command.DeleteRelation(
                    ctx = params.ctx,
                    dv = params.dv,
                    view = params.view,
                    keys = params.keys
                )
                repo.removeDataViewViewRelation(command)
            }
            is Params.ViewerRelation.Replace -> {
                val command = Command.UpdateRelation(
                    ctx = params.ctx,
                    dv = params.dv,
                    view = params.view,
                    relation = params.relation
                )
                repo.replaceDataViewViewRelation(command)
            }
            is Params.ViewerRelation.Sort -> {
                val command = Command.SortRelations(
                    ctx = params.ctx,
                    dv = params.dv,
                    view = params.view,
                    keys = params.keys
                )
                repo.sortDataViewViewRelation(command)
            }
            is Params.Fields -> {
                repo.updateDataViewViewer(
                    context = params.context,
                    target = params.target,
                    viewer = params.viewer
                )
            }
            is Params.Template -> {
                repo.updateDataViewViewer(
                    context = params.context,
                    target = params.target,
                    viewer = params.viewer
                )
            }
        }
    }

    sealed class Params {
        sealed class Filter : Params() {
            data class Add(
                val ctx: Id,
                val dv: Id,
                val view: Id,
                val relationKey: String,
                val relationFormat: RelationFormat? = null,
                val operator: Block.Content.DataView.Filter.Operator = Block.Content.DataView.Filter.Operator.AND,
                val condition: Block.Content.DataView.Filter.Condition,
                val quickOption: Block.Content.DataView.Filter.QuickOption = Block.Content.DataView.Filter.QuickOption.EXACT_DATE,
                val value: Any? = null
            ) : Filter()

            data class Replace(val ctx: Id, val dv: Id, val view: Id, val filter: DVFilter) :
                Filter()

            data class Remove(val ctx: Id, val dv: Id, val view: Id, val ids: List<Id>) : Filter()
        }

        sealed class Sort : Params() {

            data class Add(val ctx: Id, val dv: Id, val view: Id, val sort: DVSort) : Sort()
            data class Replace(
                val ctx: Id, val dv: Id, val view: Id, val sort: DVSort
            ) : Sort()

            data class Remove(val ctx: Id, val dv: Id, val view: Id, val ids: List<Id>) : Sort()
        }

        sealed class ViewerRelation : Params() {

            data class Add(val ctx: Id, val dv: Id, val view: Id, val relation: DVViewerRelation) :
                ViewerRelation()

            data class Replace(
                val ctx: Id,
                val dv: Id,
                val view: Id,
                val key: Key,
                val relation: DVViewerRelation
            ) : ViewerRelation()

            data class Remove(val ctx: Id, val dv: Id, val view: Id, val keys: List<Key>) :
                ViewerRelation()

            data class Sort(val ctx: Id, val dv: Id, val view: Id, val keys: List<Key>) :
                ViewerRelation()
        }

        data class Fields(
            val context: Id,
            val target: Id,
            val viewer: DVViewer
        ) : Params()

        data class Template(
            val context: Id,
            val target: Id,
            val viewer: DVViewer
        ) : Params()
    }

    private val sortIncludeTimeKeys = listOf(
        Relations.LAST_OPENED_DATE,
        Relations.LAST_MODIFIED_DATE,
        Relations.CREATED_DATE
    )
}