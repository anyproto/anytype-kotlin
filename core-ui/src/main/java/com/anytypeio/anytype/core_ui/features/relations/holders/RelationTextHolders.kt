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
import com.anytypeio.anytype.core_ui.databinding.ItemObjectRelationTextBinding
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
    }
}

class RelationPhoneHolder(
    binding: ItemObjectRelationTextBinding,
    @StringRes
    hint: Int,
) : RelationTextViewHolderBase<RelationTextValueView.Phone>(
    binding, TYPE_CLASS_PHONE,
    hint
)

class RelationEmailHolder(
    binding: ItemObjectRelationTextBinding,
    @StringRes
    hint: Int,
) : RelationTextViewHolderBase<RelationTextValueView.Email>(
    binding,
    TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
    hint
)

class RelationUrlHolder(
    binding: ItemObjectRelationTextBinding,
    @StringRes
    hint: Int,
) : RelationTextViewHolderBase<RelationTextValueView.Url>(
    binding, TYPE_TEXT_VARIATION_URI,
    hint
)

class RelationNumberHolder(
    binding: ItemObjectRelationTextBinding,
    @StringRes
    hint: Int,
) : RelationTextViewHolderBase<RelationTextValueView.Number>(
    binding,
    TYPE_CLASS_NUMBER or TYPE_NUMBER_FLAG_DECIMAL or TYPE_NUMBER_FLAG_SIGNED,
    hint
)