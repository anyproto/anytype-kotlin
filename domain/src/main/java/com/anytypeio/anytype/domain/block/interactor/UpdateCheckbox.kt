package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Payload

/**
 * Use-case for updating checkbox state.
 */
open class UpdateCheckbox(
    private val repo: BlockRepository
) : BaseUseCase<Payload, UpdateCheckbox.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.updateCheckbox(
            command = Command.UpdateCheckbox(
                context = params.context,
                target = params.target,
                isChecked = params.isChecked
            )
        )
    }

    /**
     * @property context context id
     * @property target checkbox block id
     * @property isChecked new checked/unchecked state for this target
     */
    data class Params(
        val context: String,
        val target: String,
        val isChecked: Boolean
    )
}