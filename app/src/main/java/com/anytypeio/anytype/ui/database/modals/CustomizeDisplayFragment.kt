package com.anytypeio.anytype.ui.database.modals

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.show
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.di.feature.CustomizeDisplayViewModule
import com.anytypeio.anytype.domain.database.model.ViewType
import com.anytypeio.anytype.presentation.databaseview.modals.CustomizeDisplayViewModel
import com.anytypeio.anytype.presentation.databaseview.modals.CustomizeDisplayViewModelFactory
import com.anytypeio.anytype.presentation.databaseview.modals.CustomizeDisplayViewState
import com.anytypeio.anytype.ui.base.NavigationFragment
import com.anytypeio.anytype.ui.database.modals.ModalsNavFragment.Companion.ARGS_DB_ID
import kotlinx.android.synthetic.main.modal_customize.*
import javax.inject.Inject

@Deprecated("legacy")
class CustomizeDisplayFragment : NavigationFragment(R.layout.modal_customize) {

    companion object {
        fun newInstance(id: String): CustomizeDisplayFragment =
            CustomizeDisplayFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGS_DB_ID, id)
                }
            }
    }

    @Inject
    lateinit var factory: CustomizeDisplayViewModelFactory
    lateinit var filterCount: TextView
    lateinit var groupCount: TextView

    private val vm : CustomizeDisplayViewModel by viewModels { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        filterCount = view.findViewById(R.id.filterCount)
        groupCount = view.findViewById(R.id.groupCount)
        vm.state.observe(viewLifecycleOwner, Observer { render(it) })
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.onViewCreated()
    }

    private fun render(state: CustomizeDisplayViewState) {
        when (state) {
            is CustomizeDisplayViewState.Init -> {
                when (state.type) {
                    ViewType.GRID -> {
                        item_table.show()
                    }
                    ViewType.BOARD -> {
                        item_kanban.show()
                    }
                    ViewType.GALLERY -> {
                        item_gallery.show()
                    }
                    ViewType.LIST -> {
                        item_list.show()
                    }
                }
                item_table.setOnClickListener { vm.onViewTypeClick() }
                item_kanban.setOnClickListener { vm.onViewTypeClick() }
                item_list.setOnClickListener { vm.onViewTypeClick() }
                item_gallery.setOnClickListener { vm.onViewTypeClick() }
                item_properties.setOnClickListener { vm.onPropertiesClick() }
                filterCount.text = state.filters.size.toString()
                groupCount.text = state.groups.size.toString()
            }
            is CustomizeDisplayViewState.NavigateToSwitchScreen -> {
                (parentFragment as ModalNavigation).showSwitchScreen()
            }
            is CustomizeDisplayViewState.NavigateToPropertiesScreen -> {
                (parentFragment as ModalNavigation).showDetailsScreen()
            }
        }
    }

    override fun injectDependencies() {
        componentManager().mainComponent
            .customizeDisplayViewComponentBuilder()
            .customizeModule(CustomizeDisplayViewModule(id = arguments?.getString(ARGS_DB_ID) as String))
            .build()
            .inject(this)
    }

    override fun releaseDependencies() {}
}