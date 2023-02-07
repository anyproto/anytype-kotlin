package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for adding a new viewer to DV.
 */
class AddDataViewViewer(
    private val repo: BlockRepository
) : BaseUseCase<Payload, AddDataViewViewer.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.addDataViewViewer(
            ctx = params.ctx,
            target = params.target,
            name = params.name,
            type = params.type,
            source = params.source
        )
    }

    /**
     * @property [context] operation context (a.k.a object set id)
     * @property [target] DV block id
     * @property [name] viewer name
     * @property [type] viewer type
     */
    class Params(
        val ctx: Id,
        val target: Id,
        val name: String,
        val type: DVViewerType,
        val source: List<Id>
    )
}