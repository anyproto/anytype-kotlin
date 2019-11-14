package com.agileburo.anytype.ui.table

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.domain.database.model.DatabaseView
import com.agileburo.anytype.presentation.databaseview.DatabaseViewModel
import com.agileburo.anytype.presentation.databaseview.DatabaseViewModelFactory
import com.agileburo.anytype.ui.base.ViewStateFragment
import timber.log.Timber
import javax.inject.Inject

const val TEST_ID = "1"

class DatabaseViewFragment : ViewStateFragment<ViewState<DatabaseView>>(R.layout.fragment_table) {

    @Inject
    lateinit var factory: DatabaseViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(DatabaseViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(this, this)
        vm.getDatabaseView(id = TEST_ID)
    }

    override fun render(state: ViewState<DatabaseView>) {
        when (state) {
            is ViewState.Success -> { Timber.d("Get database : ${state.data.content.view}")}
        }
    }

    override fun injectDependencies() {
        componentManager().databaseViewComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().databaseViewComponent.release()
    }
}