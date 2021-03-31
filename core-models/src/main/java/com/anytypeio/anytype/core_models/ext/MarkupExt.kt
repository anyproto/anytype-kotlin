package com.anytypeio.anytype.core_models.ext

import com.anytypeio.anytype.core_models.Block.Content.Text.Mark
import com.anytypeio.anytype.core_models.misc.Overlap


typealias Marks = List<Mark>

/**
 * Base ext. function for adding a mark to a mark list.
 * @param mark marks that we need to process
 * @return a new list of [Mark]
 */
fun Marks.addMark(mark: Mark): Marks {
    return if (isEmpty())
        listOf(mark)
    else
        toggle(mark)
}

fun Marks.sortByType(): Marks {
    return this.sortedBy { it.type.ordinal }
}

/**
 * Ext. function for adding/toggling mark.
 * It handles different cases of [Overlap] between the mark that is being added
 * and marks of the same type already present in given markup.
 * @param target mark that we need to process.
 * @return a new list of [Mark]
 */
fun Marks.toggle(target: Mark): Marks {

    val del = mutableListOf<Mark>()

    val result = mutableListOf<Mark>()

    var targetConsumed = false

    forEach { mark ->
        if (mark.type != target.type)
            result.add(mark)
        else {

            targetConsumed = true

            when (target.range.overlap(mark.range)) {
                Overlap.EQUAL -> {
                    if (target.type == Mark.Type.TEXT_COLOR || target.type == Mark.Type.BACKGROUND_COLOR)
                        result.add(target)
                    else
                        del.add(target)
                }
                Overlap.OUTER -> {
                    result.add(target)
                }
                Overlap.INNER_LEFT -> {
                    if (target.type == Mark.Type.TEXT_COLOR || target.type == Mark.Type.BACKGROUND_COLOR) {
                        if (mark.param == target.param)
                            result.add(mark)
                        else {
                            result.add(target)
                            result.add(
                                mark.copy(
                                    range = target.range.last..mark.range.last
                                )
                            )
                        }
                    } else {
                        result.add(
                            mark.copy(
                                range = target.range.last..mark.range.last
                            )
                        )
                        del.add(target)
                    }
                }
                Overlap.INNER_RIGHT -> {
                    if (target.type == Mark.Type.TEXT_COLOR || target.type == Mark.Type.BACKGROUND_COLOR) {
                        if (target.param == mark.param)
                            result.add(mark)
                        else {
                            result.add(
                                mark.copy(
                                    range = mark.range.first..target.range.first
                                )
                            )
                            result.add(target)
                        }
                    } else {
                        result.add(
                            mark.copy(
                                range = mark.range.first..target.range.first
                            )
                        )
                        del.add(target)
                    }
                }
                Overlap.INNER -> {
                    if (target.type == Mark.Type.TEXT_COLOR || target.type == Mark.Type.BACKGROUND_COLOR) {
                        if (target.param == mark.param)
                            result.add(mark)
                        else {
                            result.add(
                                mark.copy(
                                    range = mark.range.first..target.range.first
                                )
                            )
                            result.add(target)
                            result.add(
                                mark.copy(
                                    range = target.range.last..mark.range.last
                                )
                            )
                        }
                    } else {
                        result.add(
                            mark.copy(
                                range = mark.range.first..target.range.first
                            )
                        )
                        result.add(
                            mark.copy(
                                range = target.range.last..mark.range.last
                            )
                        )
                        del.add(target)
                    }
                }
                Overlap.LEFT -> {
                    if (target.type == Mark.Type.TEXT_COLOR || target.type == Mark.Type.BACKGROUND_COLOR) {
                        if (target.param == mark.param)
                            result.add(
                                mark.copy(
                                    range = target.range.first..mark.range.last
                                )
                            )
                        else {
                            result.add(target)
                            result.add(
                                mark.copy(
                                    range = target.range.last..mark.range.last
                                )
                            )
                        }
                    } else
                        result.add(
                            mark.copy(
                                range = target.range.first..mark.range.last
                            )
                        )
                }
                Overlap.RIGHT -> {
                    if (target.type == Mark.Type.TEXT_COLOR || target.type == Mark.Type.BACKGROUND_COLOR) {
                        if (target.param == mark.param)
                            result.add(
                                mark.copy(
                                    range = mark.range.first..target.range.last
                                )
                            )
                        else {
                            result.add(
                                mark.copy(
                                    range = mark.range.first..target.range.first
                                )
                            )
                            result.add(target)
                        }
                    } else
                        result.add(
                            mark.copy(
                                range = mark.range.first..target.range.last
                            )
                        )
                }
                Overlap.AFTER -> {
                    result.add(mark)
                    result.add(target)
                }
                Overlap.BEFORE -> {
                    result.add(target)
                    result.add(mark)
                }
            }
        }
    }

    if (!targetConsumed) result.add(target)

    return result - del
}

/**
 * Sorts a list of [Mark] according to their type and range.
 * @return a sorted list of [Mark]
 */
fun Marks.sorted(): Marks = sortedWith(
    compareBy<Mark> { it.type.ordinal }.thenBy { it.range.first }.thenBy { it.range.last }
)

/**
 * Normalizes a [sorted] list of [Mark].
 * Unsorted list could lead to inconsistent states.
 * @return a normalized version of the given list.
 */
fun Marks.normalize(): Marks {

    // TODO handle dead markup (i.e. cases where text is missing while markup is present).

    if (size == 1) return this

    check(this == sorted()) { "In order to be normalized, marks should be sorted" }

    val result = mutableListOf<Mark>()

    for (i in indices) {

        val mark = get(i)
        val prev = getOrNull(i - 1)

        if (prev != null) {
            if (prev.type == mark.type) {
                if (prev.range.last > mark.range.first) {
                    result.add(prev.copy(range = prev.range.first..mark.range.last))
                } else {
                    result.add(prev)
                    if (i == lastIndex) result.add(mark)
                }
            } else {
                result.add(prev)
                if (i == lastIndex) result.add(mark)
            }
        }
    }

    return result

}

/**
 * Determines [Overlap] case for two ranges by comparing their values.
 */
fun IntRange.overlap(target: IntRange): Overlap = when {
    first == target.first && last == target.last -> Overlap.EQUAL
    last < target.first -> Overlap.BEFORE
    first > target.last -> Overlap.AFTER
    first <= target.first && last >= target.last -> Overlap.OUTER
    first > target.first && last < target.last -> Overlap.INNER
    first == target.first && last < target.last -> Overlap.INNER_LEFT
    first > target.first && last == target.last -> Overlap.INNER_RIGHT
    first < target.first && last >= target.first -> Overlap.LEFT
    else -> Overlap.RIGHT
}

/**
 * Recalculate marks ranges
 *
 * @param from start position for shifting markup ranges
 * @param length defines the number of positions to shift
 * @return
 */

fun List<Mark>.shift(from: Int, length: Int): List<Mark> {
    val updated = arrayListOf<Mark>()
    this.map { mark ->
        var newFrom = mark.range.first
        var newTo = mark.range.last
        if ((newFrom <= from) && (newTo > from)) {
            newTo += length
        } else {
            if (newFrom >= from) {
                newFrom += length
                newTo += length
            }
        }
        updated.add(
            mark.copy(
                range = IntRange(newFrom, newTo)
            )
        )
    }
    return updated
}