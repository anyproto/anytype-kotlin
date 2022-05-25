package com.anytypeio.anytype.core_utils.ui

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class NonScrollLinearLayoutManager(context: Context): LinearLayoutManager(context) {
    override fun canScrollVertically(): Boolean = false
}