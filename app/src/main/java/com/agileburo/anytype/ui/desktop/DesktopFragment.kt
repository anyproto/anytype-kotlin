package com.agileburo.anytype.ui.desktop

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.desktop.DesktopView
import com.agileburo.anytype.presentation.desktop.DesktopViewModel
import com.agileburo.anytype.presentation.desktop.DesktopViewModelFactory
import com.agileburo.anytype.presentation.profile.ProfileView
import com.agileburo.anytype.ui.base.ViewStateFragment
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_desktop.*
import java.io.ByteArrayInputStream
import javax.inject.Inject


class DesktopFragment : ViewStateFragment<ViewState<List<DesktopView>>>(R.layout.fragment_desktop) {

    private val profileObserver = Observer<ProfileView> { profile ->
        desktopTitle.text = getString(R.string.greet, profile.name)
    }

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(DesktopViewModel::class.java)
    }

    @Inject
    lateinit var factory: DesktopViewModelFactory

    private val desktopAdapter by lazy {
        DesktopAdapter(
            data = mutableListOf(),
            onDocumentClicked = { vm.onDocumentClicked() }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(this, this)
        vm.navigation.observe(this, navObserver)
        vm.profile.observe(this, profileObserver)
        vm.onViewCreated()

        vm.image.observe(this, Observer { blob ->

            val stream = ByteArrayInputStream(blob)

            Glide
                .with(this)
                .load(BitmapFactory.decodeStream(stream))
                .centerInside()
                .circleCrop()
                .into(profileImage)

            stream.close()
        })
    }

    override fun render(state: ViewState<List<DesktopView>>) {
        when (state) {
            is ViewState.Init -> {
                desktopRecycler.apply {
                    layoutManager = GridLayoutManager(context, 2)
                    adapter = desktopAdapter
                }
                fab.setOnClickListener {
                    findNavController().navigate(R.id.action_desktopScreen_to_databaseViewFragment)
                    //vm.onAddNewDocumentClicked()
                }
                profileImage.setOnClickListener { vm.onProfileClicked() }
            }
            is ViewState.Success -> {
                desktopAdapter.update(state.data)
            }
        }
    }

    override fun injectDependencies() {
        componentManager().desktopComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().desktopComponent.release()
    }
}