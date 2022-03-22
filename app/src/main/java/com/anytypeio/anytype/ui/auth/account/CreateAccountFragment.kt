package com.anytypeio.anytype.ui.auth.account

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.extensions.toast
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.databinding.FragmentCreateAccountBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.auth.account.CreateAccountViewModel
import com.anytypeio.anytype.presentation.auth.account.CreateAccountViewModelFactory
import com.anytypeio.anytype.ui.base.NavigationFragment
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import javax.inject.Inject


class CreateAccountFragment : NavigationFragment<FragmentCreateAccountBinding>(R.layout.fragment_create_account) {

    @Inject
    lateinit var factory: CreateAccountViewModelFactory

    private val vm : CreateAccountViewModel by viewModels { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.createProfileButton.setOnClickListener {
            vm.onCreateProfileClicked(
                input = binding.nameInputField.text.toString(),
                invitationCode = getCode()
            )
        }
        binding.profileIconPlaceholder.setOnClickListener { proceedWithImagePick() }
        binding.backButton.setOnClickListener { vm.onBackButtonClicked() }
        setupNavigation()
        vm.error.observe(viewLifecycleOwner, Observer(this::showError))
    }

    private fun getCode() = requireArguments().getString(ARGS_CODE, EMPTY_CODE)

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.currentFocus?.hideKeyboard()
    }

    private fun showError(error: String) {
        requireActivity().toast(
            msg = error,
            gravity = Gravity.TOP,
            duration = Toast.LENGTH_SHORT
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        proceedWithGettingAvatarPathFromDevice(resultCode, requestCode, data)
    }

    private fun proceedWithGettingAvatarPathFromDevice(
        resultCode: Int,
        requestCode: Int,
        data: Intent?
    ) {
        try {
            if (resultCode == RESULT_OK && requestCode == SELECT_IMAGE_CODE) {
                data?.data?.let { uri ->

                    binding.profileIcon.apply {
                        visible()
                        Glide
                            .with(this)
                            .load(uri)
                            .circleCrop()
                            .into(this)
                    }

                    binding.profileIconPlaceholder.invisible()

                    vm.onAvatarSet(uri.parsePath(requireContext()))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while setting avatar")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_CODE)
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                openGallery()
    }

    private fun setupNavigation() {
        vm.observeNavigation().observe(viewLifecycleOwner, navObserver)
    }

    private fun openGallery() {
        getContent.launch(SELECT_IMAGE_CODE)
    }

    private fun proceedWithImagePick() {
        if (!hasExternalStoragePermission())
            permissionReadStorage.launch(arrayOf(READ_EXTERNAL_STORAGE))
        else
            openGallery()
    }

    private fun hasExternalStoragePermission() = ContextCompat.checkSelfPermission(
        requireActivity(),
        READ_EXTERNAL_STORAGE
    ).let { result -> result == PackageManager.PERMISSION_GRANTED }

    private val permissionReadStorage =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
            val readResult = grantResults[READ_EXTERNAL_STORAGE]
            if (readResult == true) {
                openGallery()
            } else {
                binding.root.showSnackbar(R.string.permission_read_denied, Snackbar.LENGTH_SHORT)
            }
        }

    val getContent = registerForActivityResult(GetImageContract()) { uri: Uri? ->
        if (uri != null) {
            try {
                val path = uri.parseImagePath(requireContext())
                binding.profileIcon.apply {
                    visible()
                    Glide
                        .with(this)
                        .load(uri)
                        .circleCrop()
                        .into(this)
                }
                binding.profileIconPlaceholder.invisible()
                vm.onAvatarSet(path = path)
            } catch (e: Exception) {
                toast("Error while parsing path for cover image")
                Timber.d(e, "Error while parsing path for cover image")
            }
        } else {
            toast("Error while upload cover image, URI is null")
            Timber.e("Error while upload cover image, URI is null")
        }
    }

    override fun injectDependencies() {
        componentManager().createAccountComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createAccountComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateAccountBinding = FragmentCreateAccountBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val ARGS_CODE = "args.code"
        const val EMPTY_CODE = ""
        const val SELECT_IMAGE_CODE = 1
        const val REQUEST_PERMISSION_CODE = 2
    }
}