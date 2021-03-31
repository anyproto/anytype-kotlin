package com.anytypeio.anytype.ui.database.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.hide
import com.anytypeio.anytype.core_utils.ext.show
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.databaseview.modals.AddDisplayViewModel
import com.anytypeio.anytype.presentation.databaseview.modals.AddDisplayViewModelFactory
import com.anytypeio.anytype.presentation.databaseview.modals.AddViewState
import com.anytypeio.anytype.ui.base.NavigationBottomSheetFragment
import kotlinx.android.synthetic.main.modal_add_view.*
import javax.inject.Inject

@Deprecated("legacy")
class AddDisplayFragment : NavigationBottomSheetFragment() {

    @Inject
    lateinit var factory: AddDisplayViewModelFactory

    lateinit var listChosen: ImageView
    lateinit var tableChosen: ImageView
    lateinit var galleryChosen: ImageView
    lateinit var kanbanChosen: ImageView

    private val vm : AddDisplayViewModel by viewModels { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.modal_add_view, container, false)

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

    private fun render(state: AddViewState) {
        when (state) {

            is AddViewState.Init -> {
                item_list.setOnClickListener { vm.onListClick() }
                item_table.setOnClickListener { vm.onTableClick() }
                item_kanban.setOnClickListener { vm.onKanbanClick() }
                item_gallery.setOnClickListener { vm.onGalleryClick() }
                buttonAdd.setOnClickListener { vm.onAddClick(editName.text.toString()) }
                buttonCancel.setOnClickListener { vm.onCancelClick() }
                galleryChosen.visibility = View.VISIBLE
            }

            is AddViewState.Create -> {
                requireContext().toast("Create DisplayView : ${state.display}")
            }

            AddViewState.EmptyName -> {
                requireContext().toast("Please, enter name")
            }

            AddViewState.GalleryChosen -> {
                galleryChosen.show()
                listChosen.hide()
                tableChosen.hide()
                kanbanChosen.hide()
            }

            AddViewState.ListChosen -> {
                listChosen.show()
                tableChosen.hide()
                kanbanChosen.hide()
                galleryChosen.hide()
            }

            AddViewState.KanbanChosen -> {
                kanbanChosen.show()
                listChosen.hide()
                tableChosen.hide()
                galleryChosen.hide()
            }

            AddViewState.TableChosen -> {
                tableChosen.show()
                listChosen.hide()
                kanbanChosen.hide()
                galleryChosen.hide()
            }
        }
    }

    override fun injectDependencies() {
        componentManager().databaseViewComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().databaseViewComponent.release()
    }
}