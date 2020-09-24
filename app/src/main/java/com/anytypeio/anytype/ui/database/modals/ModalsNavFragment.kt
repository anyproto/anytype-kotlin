package com.anytypeio.anytype.ui.database.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.anytypeio.anytype.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ModalsNavFragment : BottomSheetDialogFragment(),
    ModalNavigation {

    companion object {
        const val TAG_CUSTOMIZE = "tag.customize"
        const val TAG_SWITCH = "tag.switch"
        const val TAG_DETAILS = "tag.details"
        const val TAG_DETAIL_EDIT = "tag.detail.edit"
        const val TAG_DETAILS_REORDER = "tag.details.reorder"
        const val ARGS_DB_ID = "args.database.id"
        const val ARGS_TAG_START = "args.tags.start"

        fun newInstance(databaseId: String, startTag: String): ModalsNavFragment =
            ModalsNavFragment().apply {
                arguments = bundleOf(ARGS_DB_ID to databaseId, ARGS_TAG_START to startTag)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_modals_nav, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showScreenByTag(arguments!!.getString(ARGS_TAG_START, ""))
    }

    private fun showScreenByTag(tag: String) {
        when (tag) {
            TAG_CUSTOMIZE -> showCustomizeScreen()
            TAG_SWITCH -> showSwitchScreen()
            TAG_DETAILS -> showDetailsScreen()
        }
    }

    override fun showCustomizeScreen() {
        childFragmentManager.beginTransaction()
            .replace(
                R.id.container,
                CustomizeDisplayFragment.newInstance(getDbId()),
                TAG_CUSTOMIZE
            )
            .commit()
    }

    override fun showSwitchScreen() {
        childFragmentManager.beginTransaction()
            .replace(
                R.id.container,
                SwitchDisplayFragment.newInstance(getDbId()),
                TAG_SWITCH
            )
            .commit()
    }

    override fun showDetailsScreen() {
        childFragmentManager.beginTransaction()
            .replace(
                R.id.container,
                DetailsFragment.newInstance(getDbId()),
                TAG_DETAILS
            )
            .commit()
    }

    override fun showDetailEditScreen(detailId: String) {
        childFragmentManager.beginTransaction()
            .replace(
                R.id.container,
                DetailEditFragment.newInstance(
                    propertyId = detailId,
                    databaseId = getDbId()
                ),
                TAG_DETAIL_EDIT
            )
            .commit()
    }

    override fun showReorderDetails() {
        childFragmentManager.beginTransaction()
            .replace(
                R.id.container,
                DetailsReorderFragment.newInstance(getDbId()),
                TAG_DETAILS_REORDER
            )
            .commit()
    }

    private fun getDbId() = arguments!!.getString(ARGS_DB_ID, "-1")
}

interface ModalNavigation {
    fun showCustomizeScreen()
    fun showSwitchScreen()
    fun showDetailsScreen()
    fun showDetailEditScreen(detailId: String)
    fun showReorderDetails()
}
