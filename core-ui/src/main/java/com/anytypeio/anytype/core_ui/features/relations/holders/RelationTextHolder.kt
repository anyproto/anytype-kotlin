package com.anytypeio.anytype.core_ui.features.relations.holders

import android.text.InputType.*
import android.view.inputmethod.EditorInfo
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemObjectRelationTextBinding
import com.anytypeio.anytype.core_utils.ext.focusAndShowKeyboard
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.EditGridCellAction
import com.anytypeio.anytype.presentation.sets.RelationTextValueView

class RelationTextHolder(
    val binding: ItemObjectRelationTextBinding
) : RelationBaseHolder(binding.root) {

    init {
        with(binding.textInputField) {
            imeOptions = EditorInfo.IME_ACTION_DONE
            inputType = TYPE_TEXT_FLAG_MULTI_LINE
            setRawInputType(TYPE_CLASS_TEXT or TYPE_TEXT_FLAG_CAP_SENTENCES or TYPE_TEXT_FLAG_AUTO_CORRECT)
            setHorizontallyScrolling(false)
            maxLines = Integer.MAX_VALUE
        }
    }

    fun bind(view: RelationTextValueView.Text) = with(binding) {
        textInputField.setText(view.value)
        if (view.value.isNullOrEmpty()) {
            textInputField.focusAndShowKeyboard()
        }
        btnAction.gone()
    }
}

class RelationTextShortHolder(
    val binding: ItemObjectRelationTextBinding
) : RelationBaseHolder(binding.root) {

    fun bind(view: RelationTextValueView.TextShort) = with(binding) {
        textInputField.setText(view.value)
        if (view.value.isNullOrEmpty()) {
            textInputField.focusAndShowKeyboard()
        }
        textInputField.inputType = TYPE_CLASS_TEXT
        textInputField.isSingleLine = true
        btnAction.gone()
    }
}

class RelationPhoneHolder(
    val binding: ItemObjectRelationTextBinding
) : RelationBaseHolder(binding.root) {

    fun bind(
        view: RelationTextValueView.Phone,
        actionClick: (EditGridCellAction) -> Unit
    ) = with(binding) {
        textInputField.setText(view.value)
        if (view.value.isNullOrEmpty()) {
            textInputField.focusAndShowKeyboard()
        } else {
            ivActionIcon.setImageResource(R.drawable.ic_cell_relation_call_with)
        }
        textInputField.inputType = TYPE_CLASS_PHONE
        btnAction.visible()
        btnAction.setOnClickListener {
            actionClick(EditGridCellAction.Phone(textInputField.text.toString()))
        }
    }
}

class RelationEmailHolder(
    val binding: ItemObjectRelationTextBinding
) : RelationBaseHolder(binding.root) {

    fun bind(
        view: RelationTextValueView.Email,
        actionClick: (EditGridCellAction) -> Unit
    ) = with(binding) {
        textInputField.setText(view.value)
        if (view.value.isNullOrEmpty()) {
            textInputField.focusAndShowKeyboard()
        } else {
            ivActionIcon.setImageResource(R.drawable.ic_cell_relation_go_to_mail_client)
        }
        textInputField.inputType = TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        btnAction.visible()
        btnAction.setOnClickListener {
            actionClick(EditGridCellAction.Email(textInputField.text.toString()))
        }
    }
}

class RelationUrlHolder(
    val binding: ItemObjectRelationTextBinding
) : RelationBaseHolder(binding.root) {

    fun bind(
        view: RelationTextValueView.Url,
        actionClick: (EditGridCellAction) -> Unit
    ) = with(binding) {
        textInputField.setText(view.value)
        if (view.value.isNullOrEmpty()) {
            textInputField.focusAndShowKeyboard()
        } else {
            ivActionIcon.setImageResource(R.drawable.ic_cell_relation_go_to_link)
        }
        textInputField.inputType = TYPE_TEXT_VARIATION_URI
        btnAction.visible()
        btnAction.setOnClickListener {
            actionClick(EditGridCellAction.Url(textInputField.text.toString()))
        }
    }
}

class RelationNumberHolder(
    val binding: ItemObjectRelationTextBinding
) : RelationBaseHolder(binding.root) {

    init {
        with(binding.textInputField) {
            inputType = TYPE_CLASS_NUMBER or TYPE_NUMBER_FLAG_DECIMAL or TYPE_NUMBER_FLAG_SIGNED
        }
    }

    fun bind(view: RelationTextValueView.Number) = with(binding) {
        textInputField.setText(view.value)
        if (view.value.isNullOrEmpty()) {
            textInputField.focusAndShowKeyboard()
        }
        btnAction.gone()
    }
}