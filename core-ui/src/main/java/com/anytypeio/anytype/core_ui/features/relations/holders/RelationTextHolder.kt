package com.anytypeio.anytype.core_ui.features.relations.holders

import android.text.InputType
import android.view.View
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.focusAndShowKeyboard
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.EditGridCellAction
import com.anytypeio.anytype.presentation.sets.RelationTextValueView
import kotlinx.android.synthetic.main.item_object_relation_text.view.*

class RelationTextHolder(view: View) : RelationBaseHolder(view) {

    fun bind(view: RelationTextValueView.Text) = with(itemView) {
        textInputField.setText(view.value)
        if (view.value.isNullOrEmpty()) {
            textInputField.focusAndShowKeyboard()
        }
        textInputField.setHint(R.string.dv_cell_description_hint)
        textInputField.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        textInputField.isSingleLine = false
        btnAction.gone()
    }
}

class RelationTextShortHolder(view: View) : RelationBaseHolder(view) {

    fun bind(view: RelationTextValueView.TextShort) = with(itemView) {
        textInputField.setText(view.value)
        if (view.value.isNullOrEmpty()) {
            textInputField.focusAndShowKeyboard()
        }
        textInputField.setHint(R.string.dv_cell_description_hint)
        textInputField.inputType = InputType.TYPE_CLASS_TEXT
        textInputField.isSingleLine = true
        btnAction.gone()
    }
}

class RelationPhoneHolder(view: View) : RelationBaseHolder(view) {

    fun bind(view: RelationTextValueView.Phone, actionClick: (EditGridCellAction) -> Unit) =
        with(itemView) {
            textInputField.setText(view.value)
            if (view.value.isNullOrEmpty()) {
                textInputField.focusAndShowKeyboard()
            } else {
                ivActionIcon.setImageResource(R.drawable.ic_cell_relation_call_with)
            }
            textInputField.setHint(R.string.hint_empty)
            textInputField.inputType = InputType.TYPE_CLASS_PHONE
            btnAction.visible()
            btnAction.setOnClickListener {
                actionClick(EditGridCellAction.Phone(textInputField.text.toString()))
            }
        }
}

class RelationEmailHolder(view: View) : RelationBaseHolder(view) {

    fun bind(view: RelationTextValueView.Email, actionClick: (EditGridCellAction) -> Unit) =
        with(itemView) {
            textInputField.setText(view.value)
            if (view.value.isNullOrEmpty()) {
                textInputField.focusAndShowKeyboard()
            } else {
                ivActionIcon.setImageResource(R.drawable.ic_cell_relation_go_to_mail_client)
            }
            textInputField.setHint(R.string.hint_empty)
            textInputField.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            btnAction.visible()
            btnAction.setOnClickListener {
                actionClick(EditGridCellAction.Email(textInputField.text.toString()))
            }
        }
}

class RelationUrlHolder(view: View) : RelationBaseHolder(view) {

    fun bind(view: RelationTextValueView.Url, actionClick: (EditGridCellAction) -> Unit) =
        with(itemView) {
            textInputField.setText(view.value)
            if (view.value.isNullOrEmpty()) {
                textInputField.focusAndShowKeyboard()
            } else {
                ivActionIcon.setImageResource(R.drawable.ic_cell_relation_go_to_link)
            }
            textInputField.setHint(R.string.hint_empty)
            textInputField.inputType = InputType.TYPE_TEXT_VARIATION_URI
            btnAction.visible()
            btnAction.setOnClickListener {
                actionClick(EditGridCellAction.Url(textInputField.text.toString()))
            }
        }
}

class RelationNumberHolder(view: View) : RelationBaseHolder(view) {

    fun bind(view: RelationTextValueView.Number) = with(itemView) {
        textInputField.setText(view.value)
        if (view.value.isNullOrEmpty()) {
            textInputField.focusAndShowKeyboard()
        }
        textInputField.setHint(R.string.dv_cell_number_hint)
        textInputField.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        btnAction.gone()
    }
}