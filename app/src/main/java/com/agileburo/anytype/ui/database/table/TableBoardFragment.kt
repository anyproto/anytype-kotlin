package com.agileburo.anytype.ui.database.table

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.databaseview.TableBoardViewModel
import com.agileburo.anytype.presentation.databaseview.TableBoardViewModelFactory
import com.agileburo.anytype.presentation.databaseview.models.Table
import com.agileburo.anytype.ui.base.ViewStateFragment
import com.agileburo.anytype.ui.database.table.adapter.TableAdapter
import kotlinx.android.synthetic.main.fragment_table.*
import javax.inject.Inject

const val TEST_ID = "1"

class DatabaseViewFragment : ViewStateFragment<ViewState<Table>>(R.layout.fragment_table) {

    @Inject
    lateinit var factory: TableBoardViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(TableBoardViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
        vm.onViewCreated()
    }

    override fun render(state: ViewState<Table>) {
        when (state) {
            is ViewState.Init -> {
                tableView.adapter =
                    TableAdapter(
                        requireContext()
                    )
                vm.getDatabaseView(id = TEST_ID)
            }

            is ViewState.Success -> {
                tableView.adapter.setColumnHeaderItems(state.data.column)
                tableView.adapter.setCellItems(state.data.cell)
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