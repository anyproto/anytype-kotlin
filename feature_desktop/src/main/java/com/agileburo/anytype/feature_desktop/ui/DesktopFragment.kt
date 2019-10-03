package com.agileburo.anytype.feature_desktop.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.agileburo.anytype.core_utils.disposedBy
import com.agileburo.anytype.feature_desktop.R
import com.agileburo.anytype.feature_desktop.mvvm.DesktopViewModel
import com.agileburo.anytype.feature_desktop.navigation.DesktopNavigationProvider
import kotlinx.android.synthetic.main.fragment_desktop.*

class DesktopFragment : BaseFragment() {

    private val vm by lazy {
        ViewModelProviders.of(this).get(DesktopViewModel::class.java)
    }

    private val desktopAdapter by lazy {
        DesktopAdapter(
            data = mutableListOf(),
            onAddNewDocumentClicked = { vm.onAddNewDocumentClicked() },
            onDocumentClicked = {
                (requireActivity() as? DesktopNavigationProvider)
                    ?.provideDesktopNavigation()
                    ?.openDocument("")
            }
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_desktop, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subscriptions.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        desktopRecycler.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = desktopAdapter
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.observeDesktop().subscribe(desktopAdapter::update).disposedBy(subscriptions)
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}
}