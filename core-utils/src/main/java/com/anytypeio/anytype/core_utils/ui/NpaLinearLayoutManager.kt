package com.anytypeio.anytype.core_utils.ui

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * https://stackoverflow.com/questions/30220771/recyclerview-inconsistency-detected-invalid-item-position
 */
class NpaLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {
    /**
     * Disable predictive animations. There is a bug in RecyclerView which causes views that
     * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
     * adapter size has decreased since the ViewHolder was recycled.
     */
    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
}