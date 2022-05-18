package com.anytypeio.anytype.test_utils.utils

import android.content.Context
import android.content.res.Resources
import androidx.test.platform.app.InstrumentationRegistry

val context: Context
    get() = InstrumentationRegistry.getInstrumentation().targetContext

val resources: Resources
    get() = InstrumentationRegistry.getInstrumentation().targetContext.resources