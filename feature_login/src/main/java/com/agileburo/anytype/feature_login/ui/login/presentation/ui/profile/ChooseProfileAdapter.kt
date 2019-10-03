package com.agileburo.anytype.feature_login.ui.login.presentation.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.feature_login.R
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile.ChooseProfileView
import kotlinx.android.synthetic.main.item_choose_profile_profile.view.*

class ChooseProfileAdapter(
    private val views: MutableList<ChooseProfileView>,
    private val onAddNewProfileClicked: () -> Unit,
    private val onProfileClicked: (ChooseProfileView.ProfileView) -> Unit
) : RecyclerView.Adapter<ChooseProfileAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            PROFILE -> {
                ViewHolder.ProfileHolder(
                    view = inflater.inflate(R.layout.item_choose_profile_profile, parent, false)
                )
            }
            ADD_NEW_PROFILE -> {
                ViewHolder.AddNewProfileViewHolder(
                    view = inflater.inflate(R.layout.item_choose_profile_add, parent, false)
                )
            }
            else -> throw IllegalStateException("Unknown view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int = views[position].getViewType()

    override fun getItemCount(): Int = views.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.ProfileHolder -> {
                holder.bind(
                    model = views[position] as ChooseProfileView.ProfileView,
                    onProfileClicked = onProfileClicked
                )
            }
            is ViewHolder.AddNewProfileViewHolder -> {
                holder.bind(
                    onAddNewProfileClicked = onAddNewProfileClicked
                )
            }
        }
    }

    fun update(update: List<ChooseProfileView>) {
        views.apply {
            clear()
            addAll(update)
        }
        notifyDataSetChanged()
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class ProfileHolder(view: View) : ViewHolder(view) {

            private val name = itemView.name

            fun bind(
                model: ChooseProfileView.ProfileView,
                onProfileClicked: (ChooseProfileView.ProfileView) -> Unit
            ) {
                name.text = model.name
                itemView.setOnClickListener { onProfileClicked(model) }
            }
        }

        class AddNewProfileViewHolder(view: View) : ViewHolder(view) {

            fun bind(
                onAddNewProfileClicked: () -> Unit
            ) {
                itemView.setOnClickListener { onAddNewProfileClicked() }
            }
        }
    }

    companion object {
        const val PROFILE = 0
        const val ADD_NEW_PROFILE = 1
    }

}