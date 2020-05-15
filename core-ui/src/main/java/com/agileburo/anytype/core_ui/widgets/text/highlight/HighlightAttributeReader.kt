/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.agileburo.anytype.core_ui.widgets.text.highlight

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.content.res.getDrawableOrThrow
import com.agileburo.anytype.core_ui.R

/**
 * Reads default attributes that [HighlightAttributeReader] needs from resources.
 * @property horizontalPadding: the padding to be applied to left & right of the background
 * @property verticalPadding: the padding to be applied to top & bottom of the background
 * @property drawable: the drawable used to draw the background
 * @property drawableLeft: the drawable used to draw left edge of the background
 * @property drawableMid: the drawable used to draw for whole line
 * @property drawableRight: the drawable used to draw right edge of the background
 */
class HighlightAttributeReader(context: Context, attrs: AttributeSet?) {

    val horizontalPadding: Int
    val verticalPadding: Int
    val drawable: Drawable
    val drawableLeft: Drawable
    val drawableMid: Drawable
    val drawableRight: Drawable

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.HighlightDrawer,
            0,
            R.style.RoundedBgTextView
        )
        horizontalPadding = typedArray.getDimensionPixelSize(
            R.styleable.HighlightDrawer_roundedTextHorizontalPadding,
            0
        )
        verticalPadding = typedArray.getDimensionPixelSize(
            R.styleable.HighlightDrawer_roundedTextVerticalPadding,
            0
        )
        drawable = typedArray.getDrawableOrThrow(
            R.styleable.HighlightDrawer_roundedTextDrawable
        )
        drawableLeft = typedArray.getDrawableOrThrow(
            R.styleable.HighlightDrawer_roundedTextDrawableLeft
        )
        drawableMid = typedArray.getDrawableOrThrow(
            R.styleable.HighlightDrawer_roundedTextDrawableMid
        )
        drawableRight = typedArray.getDrawableOrThrow(
            R.styleable.HighlightDrawer_roundedTextDrawableRight
        )
        typedArray.recycle()
    }
}