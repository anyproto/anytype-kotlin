package com.anytypeio.anytype.core_utils.ext

import android.content.Context
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow
import timber.log.Timber

fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}

fun <T> MutableList<T>.mapInPlace(
    mutator: (T) -> T,
) {
    for (i in this.indices) {
        this[i] = mutator(this[i])
    }
}

inline fun <reified T> MutableList<T>.shift(srcIndex: Int, dstIndex: Int) =
    if (srcIndex < dstIndex) shiftUp(srcIndex, dstIndex) else shiftDown(srcIndex, dstIndex)


inline fun <reified T> MutableList<T>.shiftUp(srcIndex: Int, dstIndex: Int) =
    MutableList(size) { i ->
        when {
            i < srcIndex -> this[i]
            i < dstIndex -> this[i + 1]
            i == dstIndex -> this[srcIndex]
            else -> this[i]
        }
    }

inline fun <reified T> MutableList<T>.shiftDown(srcIndex: Int, dstIndex: Int) =
    MutableList(size) { i ->
        when {
            i < dstIndex -> this[i]
            i == dstIndex -> this[srcIndex]
            i <= srcIndex -> this[i - 1]
            else -> this[i]
        }
    }

inline fun <reified T> List<*>.typeOf(): List<T> {
    val retlist = mutableListOf<T>()
    this.forEach {
        if (it is T) {
            retlist.add(it)
        }
    }
    return retlist
}

/**
 * A function for replacing a value of immutable list.
 * @param replacement a replacement operation (takes an old element, returns a new element)
 * @param target checks whether an element should be replaced or not.
 * @return an updated list
 */
inline fun <T> List<T>.replace(replacement: (T) -> T, target: (T) -> Boolean): List<T> {
    return map { if (target(it)) replacement(it) else it }
}

fun Context.toast(
    msg: CharSequence,
    duration: Int = Toast.LENGTH_SHORT
) = Toast.makeText(
    this,
    msg,
    duration
).show()

fun Fragment.toast(
    msg: CharSequence,
    duration: Int = Toast.LENGTH_SHORT
) = activity?.let {
    Toast.makeText(it, msg, duration).show()
}

fun Fragment.toast(@StringRes msgId: Int) = requireActivity().toast(requireActivity().getString(msgId))

fun Fragment.dismissInnerDialog(tag: String) {
    val fragment = childFragmentManager.findFragmentByTag(tag)
    (fragment as? DialogFragment)?.dismiss()
}

fun String.isEndLineClick(range: IntRange): Boolean = range.first == length && range.last == length

inline fun <reified T> Fragment.withParent(action: T.() -> Unit) {
    try {
        check(parentFragment is T) { "Parent is not ${T::class.java}. Please specify correct type" }
        (parentFragment as T).action()
    } catch (e: Exception) {
        Timber.e(e, "Error while executing an action with parent fragment")
    }
}

fun MatchResult?.parseMatchedInt(index: Int): Int? = this?.groups?.get(index)?.value?.toIntOrNull()

/**
 * Adds items after the index meeting a [predicateIndex]
 */
fun <T> MutableList<T>.addAfterIndexInLine(
    predicateIndex: (T) -> Boolean,
    items: List<T>
) {
    val newIndex = indexOfFirst(predicateIndex)
    if (newIndex in 0 until size) {
        addAll(newIndex + 1, items)
    } else {
        addAll(size, items)
    }
}

/**
 * Moves all items meeting a [predicateMove] after the index meeting a [predicateIndex]
 * Special case: if predicateIndex doesn't match (newIndex == -1), inserts at the beginning
 */
fun <T> MutableList<T>.moveAfterIndexInLine(
    predicateIndex: (T) -> Boolean,
    predicateMove: (T) -> Boolean
) {
    val split = partition(predicateMove)
    clear()
    addAll(split.second)
    val newIndex = indexOfFirst(predicateIndex)
    if (newIndex in 0 until size) {
        addAll(newIndex + 1, split.first)
    } else {
        // afterId not found (empty or invalid) - insert at beginning
        addAll(0, split.first)
    }
}

fun <T> MutableList<T>.moveOnTop(
    predicate: (T) -> Boolean
) {
    val (first, last) = partition(predicate)
    clear()
    addAll(first + last)
}

inline fun <T, R> Iterable<T>.allUniqueBy(transform: (T) -> R): Boolean {
    val hashset = hashSetOf<R>()
    return this.all { hashset.add(transform(it)) }
}

fun Long.readableFileSize(): String {
    if (this <= 0) return "Zero KB"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(this / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
}

fun Throwable.msg(default: String = "Unknown error") = message ?: default

inline fun <T> runSafely(actionDesc: String, block: () -> T) {
    try {
        block()
    } catch (e: Exception) {
        Timber.e(e, "Error during $actionDesc")
    }
}