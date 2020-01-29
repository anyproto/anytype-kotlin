package com.agileburo.anytype.domain.block.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.ext.rangeIntersection

/**
 * Remove all link marks from list with intersected ranges
 * with given range.
 */

class RemoveLinkMark : BaseUseCase<List<Block.Content.Text.Mark>, RemoveLinkMark.Params>() {

    override suspend fun run(params: Params): Either<Throwable, List<Block.Content.Text.Mark>> =
        try {
            val result = mutableListOf<Block.Content.Text.Mark>()
            params.marks.forEach {
                if (it.type != Block.Content.Text.Mark.Type.LINK) {
                    result.add(it)
                } else {
                    if (it.rangeIntersection(params.range) == 0) {
                        result.add(it)
                    }
                }
            }
            Either.Right(result.toList())
        } catch (t: Throwable) {
            Either.Left(t)
        }

    /**
     * @property marks Collection to remove link marks
     * @property range Given range to find intersections
     */
    class Params(
        val marks: List<Block.Content.Text.Mark>,
        val range: IntRange
    )
}