package com.anytypeio.anytype.core_ui.features.relations.holders

import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_CLASS_PHONE
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
import android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
import android.text.InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
import android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
import android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
import android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
import android.text.InputType.TYPE_TEXT_VARIATION_URI
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemObjectRelationTextBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.EditGridCellAction
import com.anytypeio.anytype.presentation.sets.RelationTextValueView

class RelationTextHolder(
    binding: ItemObjectRelationTextBinding,
    @StringRes
    hint: Int,
) : RelationTextViewHolderBase<RelationTextValueView.Text>(
    binding,
    TYPE_TEXT_FLAG_MULTI_LINE,
    hint
) {

    init {
        with(input) {
            imeOptions = EditorInfo.IME_ACTION_DONE
            setRawInputType(TYPE_CLASS_TEXT or TYPE_TEXT_FLAG_CAP_SENTENCES or TYPE_TEXT_FLAG_AUTO_CORRECT)
            setHorizontallyScrolling(false)
            maxLines = Integer.MAX_VALUE
        }
        btnAction.gone()
    }
}

class RelationTextShortHolder(
    binding: ItemObjectRelationTextBinding,
    @StringRes
    hint: Int,
) : RelationTextViewHolderBase<RelationTextValueView.TextShort>(
    binding, TYPE_CLASS_TEXT,
    hint
) {

    init {
        input.isSingleLine = true
        binding.btnAction.gone()
    }
}

class RelationPhoneHolder(
    binding: ItemObjectRelationTextBinding,
    private val actionClick: (EditGridCellAction) -> Unit,
    @StringRes
    hint: Int,
) : RelationTextViewHolderBase<RelationTextValueView.Phone>(
    binding, TYPE_CLASS_PHONE,
    hint
) {

    init {
        btnAction.visible()
        binding.ivActionIcon.setImageResource(R.drawable.ic_cell_relation_call_with)
    }

    override fun bind(
        item: RelationTextValueView.Phone,
    ) {
        super.bind(item)
        binding.btnAction.setOnClickListener {
            actionClick(EditGridCellAction.Phone(input.text.toString()))
        }
    }
}

class RelationEmailHolder(
    binding: ItemObjectRelationTextBinding,
    private val actionClick: (EditGridCellAction) -> Unit,
    @StringRes
    hint: Int,
) : RelationTextViewHolderBase<RelationTextValueView.Email>(
    binding,
    TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
    hint
) {
    init {
        btnAction.visible()
        binding.ivActionIcon.setImageResource(R.drawable.ic_cell_relation_go_to_mail_client)
    }

    override fun bind(
        item: RelationTextValueView.Email,
    ) {
        super.bind(item)
        btnAction.setOnClickListener {
            actionClick(EditGridCellAction.Email(input.text.toString()))
        }
    }
}

class RelationUrlHolder(
    binding: ItemObjectRelationTextBinding,
    private val actionClick: (EditGridCellAction) -> Unit,
    @StringRes
    hint: Int,
) : RelationTextViewHolderBase<RelationTextValueView.Url>(
    binding, TYPE_TEXT_VARIATION_URI,
    hint
) {

    init {
        btnAction.visible()
        binding.ivActionIcon.setImageResource(R.drawable.ic_cell_relation_go_to_link)
    }

    override fun bind(
        item: RelationTextValueView.Url,
    ) {
        super.bind(item)
        btnAction.setOnClickListener {
            actionClick(EditGridCellAction.Url(input.text.toString()))
        }
    }
}

class RelationNumberHolder(
    binding: ItemObjectRelationTextBinding,
    @StringRes
    hint: Int,
) : RelationTextViewHolderBase<RelationTextValueView.Number>(
    binding,
    TYPE_CLASS_NUMBER or TYPE_NUMBER_FLAG_DECIMAL or TYPE_NUMBER_FLAG_SIGNED,
    hint
) {
    init {
        btnAction.gone()
    }
}