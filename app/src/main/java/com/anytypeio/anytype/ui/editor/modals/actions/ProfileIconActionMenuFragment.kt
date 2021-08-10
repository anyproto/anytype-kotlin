package com.anytypeio.anytype.ui.editor.modals.actions

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.View.OVER_SCROLL_NEVER
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.extensions.avatarColor
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.firstDigitByHash
import com.anytypeio.anytype.core_utils.ext.parsePath
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.library_page_icon_picker_widget.ui.ActionMenuAdapter
import com.anytypeio.anytype.library_page_icon_picker_widget.ui.ActionMenuDivider
import com.anytypeio.anytype.presentation.editor.picker.DocumentIconActionMenuViewModel
import com.anytypeio.anytype.presentation.editor.picker.DocumentIconActionMenuViewModel.ViewState
import com.anytypeio.anytype.presentation.editor.picker.DocumentIconActionMenuViewModelFactory
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.action_toolbar_profile_icon.*
import timber.log.Timber
import javax.inject.Inject

class ProfileIconActionMenuFragment : BaseFragment(R.layout.action_toolbar_profile_icon),
    Observer<ViewState> {

    private val ctx get() = arg<Id>(ARG_CONTEXT_ID_KEY)

    /**
     * avatar image url
     */
    private val image: Url?
        get() = arguments?.getString(IMAGE_KEY)

    /**
     * user name
     */
    private val name: String?
        get() = arguments?.getString(NAME_KEY)

    /**
     * target (profile) id
     */
    private val target: Id
        get() = requireArguments()
            .getString(ARG_TARGET_ID_KEY)
            ?: throw IllegalStateException(MISSING_TARGET_ERROR)

    @Inject
    lateinit var factory: DocumentIconActionMenuViewModelFactory

    private val vm : DocumentIconActionMenuViewModel by viewModels { factory }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    override fun onChanged(state: ViewState) {
        when (state) {
            is ViewState.Exit -> exit()
            is ViewState.Error -> toast(state.message)
            is ViewState.Loading -> toast(getString(R.string.loading))
        }
    }

    private fun initialize() {
        container.setOnClickListener { exit() }
        setIcon()
        setupLogoTranslation()
        setupAdapter()
        showMenuWithAnimation()
    }

    private fun setIcon() {
        image?.let { url ->
            Glide
                .with(icon)
                .load(url)
                .centerInside()
                .circleCrop()
                .into(imageIcon)
        } ?: apply {
            imageText.text = name?.firstOrNull().toString()
            val pos = name?.firstDigitByHash() ?: 0
            icon.backgroundTintList = ColorStateList.valueOf(requireContext().avatarColor(pos))
            imageIcon.setImageDrawable(null)
        }
    }

    private fun setupLogoTranslation() {
        val y = arguments?.getFloat(Y_KEY)
        if (y != null && y != 0.0f) {
            val delta = y - icon.y
            icon.y = y
            menu.y = menu.y + delta
        }
    }

    private fun showMenuWithAnimation() {
        val xAnim = menu.animate().scaleX(1f).apply {
            duration = ANIM_DURATION
            startDelay = ANIM_START_DELAY
            interpolator = OvershootInterpolator()
        }
        val yAnim = menu.animate().scaleY(1f).apply {
            duration = ANIM_DURATION
            startDelay = ANIM_START_DELAY
            interpolator = OvershootInterpolator()
        }

        xAnim.start()
        yAnim.start()
    }

    private fun setupAdapter() {

        val drawable = requireContext().getDrawable(R.drawable.action_menu_divider)

        checkNotNull(drawable)

        val divider = ActionMenuDivider(drawable)

        recycler.apply {
            addItemDecoration(divider)
            overScrollMode = OVER_SCROLL_NEVER
            adapter = ActionMenuAdapter(
                options = intArrayOf(
                    ActionMenuAdapter.OPTION_CHOOSE_UPLOAD_PHOTO,
                    ActionMenuAdapter.OPTION_REMOVE
                )
            ) { option ->
                when (option) {
                    ActionMenuAdapter.OPTION_REMOVE -> vm.onEvent(
                        DocumentIconActionMenuViewModel.Contract.Event.OnRemoveEmojiSelected(
                            context = target,
                            target = target
                        )
                    )
                    ActionMenuAdapter.OPTION_CHOOSE_UPLOAD_PHOTO -> {
                        proceedWithImagePick()
                    }
                    else -> toast("Not implemented")
                }
            }
        }
    }

    private fun proceedWithImagePick() {
        if (!hasExternalStoragePermission())
            requestExternalStoragePermission()
        else
            openGallery()
    }

    private fun requestExternalStoragePermission() {
        requestPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_PERMISSION_CODE
        )
    }

    private fun openGallery() {
        Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.INTERNAL_CONTENT_URI
        ).let { intent ->
            startActivityForResult(intent, SELECT_IMAGE_CODE)
        }
    }

    private fun hasExternalStoragePermission() = ContextCompat.checkSelfPermission(
        requireActivity(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ).let { result -> result == PackageManager.PERMISSION_GRANTED }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_IMAGE_CODE) {
            try {
                data?.data?.let { uri ->
                    val path = uri.parsePath(requireContext())
                    vm.onEvent(
                        DocumentIconActionMenuViewModel.Contract.Event.OnImagePickedFromGallery(
                            context = target,
                            path = path
                        )
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, PARSE_PATH_ERROR)
                toast(PARSE_PATH_ERROR)
            }
        }
    }

    override fun injectDependencies() {
        componentManager().documentIconActionMenuComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().documentIconActionMenuComponent.release(ctx)
    }

    private fun exit() {
        parentFragment?.childFragmentManager?.popBackStack()
    }

    companion object {
        fun new(
            y: Float?,
            image: String?,
            name: String?,
            target: String,
            ctx: Id
        ): ProfileIconActionMenuFragment = ProfileIconActionMenuFragment().apply {
            arguments = bundleOf(
                Y_KEY to y,
                IMAGE_KEY to image,
                NAME_KEY to name,
                ARG_TARGET_ID_KEY to target,
                ARG_CONTEXT_ID_KEY to ctx
            )
        }

        private const val SELECT_IMAGE_CODE = 1
        private const val REQUEST_PERMISSION_CODE = 2
        private const val Y_KEY = "y"
        private const val IMAGE_KEY = "image_key"
        private const val NAME_KEY = "name_key"
        private const val ANIM_START_DELAY = 200L
        private const val ANIM_DURATION = 200L
        private const val ARG_TARGET_ID_KEY = "arg.picker.target.id"
        private const val ARG_CONTEXT_ID_KEY = "arg.picker.target.context"
        private const val MISSING_TARGET_ERROR = "Missing target id"

        private const val PARSE_PATH_ERROR = "Failed to parse path for image"
    }
}