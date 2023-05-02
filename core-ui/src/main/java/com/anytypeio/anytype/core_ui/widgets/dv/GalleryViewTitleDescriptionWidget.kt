package com.anytypeio.anytype.core_ui.widgets.dv

import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetGalleryViewTitleDescriptionBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.relations.model.DefaultObjectRelationValueView
import com.anytypeio.anytype.presentation.sets.model.Viewer

class GalleryViewTitleDescriptionWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    val binding = WidgetGalleryViewTitleDescriptionBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    fun setupTitle(title: SpannableString) {
        binding.tvTitle.text = title
    }

    fun setupDescription(item: Viewer.GalleryView.Item, space: View?) {
        val desc = item.relations.firstOrNull { it.relationKey == Relations.DESCRIPTION }
        if (desc != null && desc is DefaultObjectRelationValueView.Text) {
            binding.tvDescription.visible()
            binding.tvDescription.text = desc.text
            space?.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = resources.getDimension(R.dimen.default_dv_gallery_title_desc_height).toInt()
            }
        } else {
            space?.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = resources.getDimension(R.dimen.default_dv_gallery_title_height).toInt()
            }
            binding.tvDescription.gone()
        }
    }
}