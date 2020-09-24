package com.anytypeio.anytype.ui.database.modals

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.anytypeio.anytype.ui.database.modals.ModalsNavFragment.Companion.ARGS_DB_ID
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.hide
import com.anytypeio.anytype.core_utils.ext.show
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.di.feature.SwitchDisplayViewModule
import com.anytypeio.anytype.domain.database.model.ViewType
import com.anytypeio.anytype.presentation.databaseview.modals.SwitchDisplayViewState
import com.anytypeio.anytype.presentation.databaseview.modals.SwitchDisplayViewViewModel
import com.anytypeio.anytype.presentation.databaseview.modals.SwitchDisplayViewViewModelFactory
import com.anytypeio.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.modal_add_view.item_gallery
import kotlinx.android.synthetic.main.modal_add_view.item_kanban
import kotlinx.android.synthetic.main.modal_add_view.item_list
import kotlinx.android.synthetic.main.modal_add_view.item_table
import kotlinx.android.synthetic.main.modal_switch_view.*
import javax.inject.Inject

class SwitchDisplayFragment : NavigationFragment(R.layout.modal_switch_view) {

    companion object {
        fun newInstance(id: String): SwitchDisplayFragment =
            SwitchDisplayFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGS_DB_ID, id)
                }
            }
    }

    @Inject
    lateinit var factory: SwitchDisplayViewViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(SwitchDisplayViewViewModel::class.java)
    }

    lateinit var listChosen: ImageView
    lateinit var tableChosen: ImageView
    lateinit var galleryChosen: ImageView
    lateinit var kanbanChosen: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listChosen = view.findViewById(R.id.listChosen)
        galleryChosen = view.findViewById(R.id.galleryChosen)
        kanbanChosen = view.findViewById(R.id.kanbanChosen)
        tableChosen = view.findViewById(R.id.tableChosen)
        vm.state.observe(viewLifecycleOwner, Observer { render(it) })
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.onViewCreated()
    }

    private fun render(state: SwitchDisplayViewState) {
        when (state) {

            is SwitchDisplayViewState.Init -> {
                when (state.type) {
                    ViewType.GRID -> tableChosen.show()
                    ViewType.BOARD -> kanbanChosen.show()
                    ViewType.GALLERY -> galleryChosen.show()
                    ViewType.LIST -> listChosen.show()
                }
                iconBack.setOnClickListener { vm.onCancelClick() }
                item_list.setOnClickListener { vm.onListClick() }
                item_table.setOnClickListener { vm.onTableClick() }
                item_kanban.setOnClickListener { vm.onKanbanClick() }
                item_gallery.setOnClickListener { vm.onGalleryClick() }
            }

            SwitchDisplayViewState.GalleryChosen -> {
                galleryChosen.show()
                listChosen.hide()
                tableChosen.hide()
                kanbanChosen.hide()
            }

            SwitchDisplayViewState.ListChosen -> {
                listChosen.show()
                tableChosen.hide()
                kanbanChosen.hide()
                galleryChosen.hide()
            }

            SwitchDisplayViewState.KanbanChosen -> {
                kanbanChosen.show()
                listChosen.hide()
                tableChosen.hide()
                galleryChosen.hide()
            }

            SwitchDisplayViewState.TableChosen -> {
                tableChosen.show()
                listChosen.hide()
                kanbanChosen.hide()
                galleryChosen.hide()
            }
            is SwitchDisplayViewState.NavigateToCustomize -> {
                (parentFragment as ModalNavigation).showCustomizeScreen()
            }
        }
    }

    override fun injectDependencies() {
        componentManager()
            .mainComponent
            .switchDisplayViewComponentBuilder()
            .switchModule(SwitchDisplayViewModule(id = arguments?.getString(ARGS_DB_ID) as String))
            .build()
            .inject(this)
    }

    override fun releaseDependencies() {
        //todo add release subcomponent
    }
}