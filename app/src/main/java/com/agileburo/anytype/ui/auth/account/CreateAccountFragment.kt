package com.agileburo.anytype.ui.auth.account

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ext.hideKeyboard
import com.agileburo.anytype.core_utils.ext.invisible
import com.agileburo.anytype.core_utils.ext.parsePath
import com.agileburo.anytype.core_utils.ext.visible
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.auth.account.CreateAccountViewModel
import com.agileburo.anytype.presentation.auth.account.CreateAccountViewModelFactory
import com.agileburo.anytype.ui.base.NavigationFragment
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_create_account.*
import javax.inject.Inject


class CreateAccountFragment : NavigationFragment(R.layout.fragment_create_account) {

    @Inject
    lateinit var factory: CreateAccountViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(CreateAccountViewModel::class.java)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createProfileButton.setOnClickListener {
            vm.onCreateProfileClicked(nameInputField.text.toString())
        }
        profileIconPlaceholder.setOnClickListener { openGallery() }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.currentFocus?.hideKeyboard()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupNavigation()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == SELECT_IMAGE_CODE) {

            data?.data?.let { uri ->

                profileIcon.apply {
                    visible()
                    Glide
                        .with(profileIcon)
                        .load(uri)
                        .circleCrop()
                        .into(profileIcon)
                }

                profileIconPlaceholder.invisible()

                vm.onAvatarSet(uri.parsePath(requireContext()))
            }
        }
    }

    private fun setupNavigation() {
        vm.observeNavigation().observe(this, navObserver)
    }

    private fun openGallery() {
        Intent(
            Intent.ACTION_PICK,
            INTERNAL_CONTENT_URI
        ).let { intent ->
            startActivityForResult(intent, SELECT_IMAGE_CODE)
        }
    }

    override fun injectDependencies() {
        componentManager().createAccountComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createAccountComponent.release()
    }

    companion object {
        const val SELECT_IMAGE_CODE = 1
    }
}