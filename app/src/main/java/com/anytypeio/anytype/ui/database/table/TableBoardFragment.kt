package com.anytypeio.anytype.ui.database.table

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.databaseview.TableBoardViewModel
import com.anytypeio.anytype.presentation.databaseview.TableBoardViewModelFactory
import com.anytypeio.anytype.presentation.databaseview.models.Table
import com.anytypeio.anytype.ui.base.ViewStateFragment
import com.anytypeio.anytype.ui.database.table.adapter.TableAdapter
import kotlinx.android.synthetic.main.fragment_table.*
import javax.inject.Inject

const val TEST_ID = "1"

@Deprecated("legacy")
class DatabaseViewFragment : ViewStateFragment<ViewState<Table>>(R.layout.fragment_table) {

    @Inject
    lateinit var factory: TableBoardViewModelFactory

    private val vm : TableBoardViewModel by viewModels { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
        vm.onViewCreated()
    }

    override fun render(state: ViewState<Table>) {
        when (state) {
            is ViewState.Init -> {
                tableView.setAdapter(
                    TableAdapter(
                        requireContext()
                    )
                )
                vm.getDatabaseView(id = TEST_ID)
            }

            is ViewState.Success -> {
                tableView.adapter?.setColumnHeaderItems(state.data.column)
                tableView.adapter?.setCellItems(state.data.cell)
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