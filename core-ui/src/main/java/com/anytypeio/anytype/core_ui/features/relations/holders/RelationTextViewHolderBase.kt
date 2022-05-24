package com.anytypeio.anytype.core_ui.features.relations.holders

import android.text.InputType
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemObjectRelationTextBinding
import com.anytypeio.anytype.core_utils.ext.focusAndShowKeyboard
import com.anytypeio.anytype.presentation.sets.RelationTextValueView

abstract class RelationTextViewHolderBase<T : RelationTextValueView>(
    val binding: ItemObjectRelationTextBinding,
    inputType: Int,
    @StringRes
    hint: Int
) : RecyclerView.ViewHolder(binding.root) {

    protected val input: TextView = binding.textInputField
    protected val btnAction = binding.btnAction
    private val textInputField = binding.textInputField

    init {
        input.inputType = inputType
        input.setHint(hint)
    }

    @CallSuper
    open fun bind(item: T) {
        textInputField.setText(item.value)
        if (item.value.isNullOrEmpty() && item.isEditable) {
            textInputField.focusAndShowKeyboard()
        }
        if (!item.isEditable) {
            enableReadMode()
        }
    }

    private fun enableReadMode() {
        input.inputType = InputType.TYPE_NULL
        input.setHint(R.string.empty)
    }
}