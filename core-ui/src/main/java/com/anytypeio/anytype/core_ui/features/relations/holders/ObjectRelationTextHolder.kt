package com.anytypeio.anytype.core_ui.features.relations.holders

import android.text.InputType
import android.view.View
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.EditGridCellAction
import com.anytypeio.anytype.presentation.sets.ObjectRelationTextValueView
import kotlinx.android.synthetic.main.item_object_relation_text.view.*

class ObjectRelationTextHolder(view: View) : ObjectRelationBaseHolder(view) {

    fun bind(view: ObjectRelationTextValueView.Text) = with(itemView) {
        textInputField.setText(view.value)
        textInputField.setHint(R.string.dv_cell_description_hint)
        textInputField.inputType = InputType.TYPE_CLASS_TEXT
        btnAction.gone()
    }
}

class ObjectRelationPhoneHolder(view: View) : ObjectRelationBaseHolder(view) {

    fun bind(view: ObjectRelationTextValueView.Phone, actionClick: (EditGridCellAction) -> Unit) =
        with(itemView) {
            textInputField.setText(view.value)
            textInputField.setHint(R.string.hint_empty)
            textInputField.inputType = InputType.TYPE_CLASS_PHONE
            btnAction.visible()
            btnAction.setOnClickListener {
                actionClick(EditGridCellAction.Phone(textInputField.text.toString()))
            }
            ivActionIcon.setImageResource(R.drawable.ic_cell_relation_call_with)
        }
}

class ObjectRelationEmailHolder(view: View) : ObjectRelationBaseHolder(view) {

    fun bind(view: ObjectRelationTextValueView.Email, actionClick: (EditGridCellAction) -> Unit) =
        with(itemView) {
            textInputField.setText(view.value)
            textInputField.setHint(R.string.hint_empty)
            textInputField.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            btnAction.visible()
            btnAction.setOnClickListener {
                actionClick(EditGridCellAction.Email(textInputField.text.toString()))
            }
            ivActionIcon.setImageResource(R.drawable.ic_cell_relation_go_to_mail_client)
        }
}

class ObjectRelationUrlHolder(view: View) : ObjectRelationBaseHolder(view) {

    fun bind(view: ObjectRelationTextValueView.Url, actionClick: (EditGridCellAction) -> Unit) =
        with(itemView) {
            textInputField.setText(view.value)
            textInputField.setHint(R.string.hint_empty)
            textInputField.inputType = InputType.TYPE_TEXT_VARIATION_URI
            btnAction.visible()
            btnAction.setOnClickListener {
                actionClick(EditGridCellAction.Url(textInputField.text.toString()))
            }
            ivActionIcon.setImageResource(R.drawable.ic_cell_relation_go_to_link)
        }
}

class ObjectRelationNumberHolder(view: View) : ObjectRelationBaseHolder(view) {

    fun bind(view: ObjectRelationTextValueView.Number) = with(itemView) {
        textInputField.setText(view.value)
        textInputField.setHint(R.string.dv_cell_number_hint)
        textInputField.inputType = InputType.TYPE_CLASS_NUMBER
        btnAction.gone()
    }
}