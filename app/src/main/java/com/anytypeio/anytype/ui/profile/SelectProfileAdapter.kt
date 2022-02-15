package com.anytypeio.anytype.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_utils.ui.ViewType
import com.anytypeio.anytype.databinding.ItemSelectProfileAddProfileBinding
import com.anytypeio.anytype.databinding.ItemSelectProfileProfileBinding

class SelectProfileAdapter(
    private val models: MutableList<Model>,
    private val onAddProfileClicked: () -> Unit,
    private val onProfileClicked: (Model.Profile) -> Unit
) : RecyclerView.Adapter<SelectProfileAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return LayoutInflater.from(parent.context).let { inflater ->
            when (viewType) {
                PROFILE_HOLDER -> {
                    ViewHolder.ProfileViewHolder(
                        ItemSelectProfileProfileBinding.inflate(
                            inflater, parent, false
                        )
                    )
                }
                ADD_PROFILE_HOLDER -> {
                    ViewHolder.AddProfileViewHolder(
                        ItemSelectProfileAddProfileBinding.inflate(
                            inflater, parent, false
                        )
                    )
                }
                else -> throw IllegalStateException("Unexpected type: $viewType")
            }
        }
    }

    override fun getItemViewType(position: Int) = models[position].getViewType()
    override fun getItemCount(): Int = models.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.ProfileViewHolder -> holder.bind(
                model = models[position],
                onClick = onProfileClicked
            )
            is ViewHolder.AddProfileViewHolder -> holder.bind(
                onClick = onAddProfileClicked
            )
        }
    }

    fun update(update: List<Model>) {
        models.apply {
            clear()
            addAll(update)
        }
        notifyDataSetChanged()
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class ProfileViewHolder(val binding: ItemSelectProfileProfileBinding) :
            ViewHolder(binding.root) {

            fun bind(
                model: Model,
                onClick: (Model.Profile) -> Unit
            ) {
                check(model is Model.Profile)
                binding.apply {
                    root.isSelected = model.active
                    name.text = model.name
                    status.text = model.status
                    root.setOnClickListener { onClick(model) }
                }
            }
        }

        class AddProfileViewHolder(val binding: ItemSelectProfileAddProfileBinding) :
            ViewHolder(binding.root) {
            fun bind(onClick: () -> Unit) {
                itemView.setOnClickListener { onClick() }
            }
        }
    }

    sealed class Model : ViewType {
        data class Profile(
            val id: String,
            val name: String,
            val status: String,
            val active: Boolean = false
        ) : Model() {
            override fun getViewType() = PROFILE_HOLDER
        }

        object AddProfile : Model() {
            override fun getViewType() = ADD_PROFILE_HOLDER
        }
    }

    companion object {
        const val PROFILE_HOLDER = 0
        const val ADD_PROFILE_HOLDER = 1
    }
}