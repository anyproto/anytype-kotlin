package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetMainBottomToolbarBinding
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.bumptech.glide.Glide

class MainBottomToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    val binding = WidgetMainBottomToolbarBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        isBaselineAligned = false
        orientation = HORIZONTAL
    }

    fun searchClicks() = binding.btnSearch.clicks()
    fun addDocClicks() = binding.btnAddDoc.clicks()
    fun homeClicks() = binding.btnHome.clicks()
    fun backClicks() = binding.btnBack.clicks()
    fun profileClicks() = binding.btnProfile.clicks()

    fun bind(icon: ProfileIconView) {
        when(icon) {
            is ProfileIconView.Image -> {
                binding.tvProfileInitial.text = ""
                Glide
                    .with(this)
                    .load(icon.url)
                    .fitCenter()
                    .circleCrop()
                    .placeholder(R.drawable.ic_nav_profile_icon_placeholder_circle)
                    .into(binding.ivProfile)
            }
            is ProfileIconView.Placeholder -> {
                val name = icon.name
                val nameFirstChar = if (name.isNullOrEmpty()) {
                    resources.getString(R.string.account_default_name)
                } else {
                    name.first().toString()
                }
                binding.tvProfileInitial.text = nameFirstChar
            }
            else -> {
                binding.ivProfile.setImageResource(R.drawable.ic_nav_profile_icon_placeholder_circle)
                binding.tvProfileInitial.text = ""
            }
        }
    }
}