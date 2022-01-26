package com.anytypeio.anytype.core_ui.features.relations.holders

import android.text.InputType.*
import android.view.View
import android.view.inputmethod.EditorInfo
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.focusAndShowKeyboard
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.EditGridCellAction
import com.anytypeio.anytype.presentation.sets.RelationTextValueView
import kotlinx.android.synthetic.main.item_object_relation_text.view.*

class RelationTextHolder(view: View) : RelationBaseHolder(view) {

    init {
        with(itemView.textInputField) {
            imeOptions = EditorInfo.IME_ACTION_DONE
            inputType = TYPE_TEXT_FLAG_MULTI_LINE
            setRawInputType(TYPE_CLASS_TEXT or TYPE_TEXT_FLAG_CAP_SENTENCES or TYPE_TEXT_FLAG_AUTO_CORRECT)
            setHorizontallyScrolling(false)
            maxLines = Integer.MAX_VALUE
        }
    }

    fun bind(view: RelationTextValueView.Text) = with(itemView) {
        textInputField.setText(view.value)
        if (view.value.isNullOrEmpty()) {
            textInputField.focusAndShowKeyboard()
        }
        btnAction.gone()
    }
}

class RelationTextShortHolder(view: View) : RelationBaseHolder(view) {

    fun bind(view: RelationTextValueView.TextShort) = with(itemView) {
        textInputField.setText(view.value)
        if (view.value.isNullOrEmpty()) {
            textInputField.focusAndShowKeyboard()
        }
        textInputField.inputType = TYPE_CLASS_TEXT
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
            textInputField.inputType = TYPE_CLASS_PHONE
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
            textInputField.inputType = TYPE_TEXT_VARIATION_EMAIL_ADDRESS
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
            textInputField.inputType = TYPE_TEXT_VARIATION_URI
            btnAction.visible()
            btnAction.setOnClickListener {
                actionClick(EditGridCellAction.Url(textInputField.text.toString()))
            }
        }
}

class RelationNumberHolder(view: View) : RelationBaseHolder(view) {

    init {
        with(itemView.textInputField) {
            inputType = TYPE_CLASS_NUMBER or TYPE_NUMBER_FLAG_DECIMAL or TYPE_NUMBER_FLAG_SIGNED
        }
    }

    fun bind(view: RelationTextValueView.Number) = with(itemView) {
        textInputField.setText(view.value)
        if (view.value.isNullOrEmpty()) {
            textInputField.focusAndShowKeyboard()
        }
        btnAction.gone()
    }
}