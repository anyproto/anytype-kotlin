package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.NO_VALUE
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class CreatePrefilledNote @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreatePrefilledNote.Params, Id>(dispatchers.io) {

    override suspend fun doWork(params: Params): Id {
        val obj = repo.createObject(
            Command.CreateObject(
                typeKey = TypeKey(ObjectTypeUniqueKeys.NOTE),
                space = SpaceId(params.space),
                template = null,
                internalFlags = emptyList(),
                prefilled = params.details
            )
        )
        repo.create(
            command = Command.Create(
                context = obj.id,
                prototype = Block.Prototype.Text(
                    style = Block.Content.Text.Style.P,
                    text = params.text
                ),
                position = Position.NONE,
                target = NO_VALUE
            )
        )
        return obj.id
    }

    data class Params(
        val space: Id,
        val text: String,
        val details: Struct
    )
}