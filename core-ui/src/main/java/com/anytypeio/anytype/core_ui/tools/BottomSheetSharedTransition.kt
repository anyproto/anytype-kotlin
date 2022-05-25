package com.anytypeio.anytype.core_ui.tools

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.transition.Transition
import android.transition.TransitionValues
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.animation.doOnEnd
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

/**
 * https://github.com/keymusicman/Bottom-Sheet-Transition-Demo
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class BottomSheetSharedTransition : Transition {

    @Suppress("unused")
    constructor() : super()

    @Suppress("unused")
    constructor(
        context: Context?,
        attrs: AttributeSet?
    ) : super(context, attrs)

    companion object {
        private const val PROP_HEIGHT = "heightTransition:height"

        // the property PROP_VIEW_TYPE is workaround that allows to run transition always
        // even if height was not changed. It's required as we should set container height
        // to WRAP_CONTENT after animation complete
        private const val PROP_VIEW_TYPE = "heightTransition:viewType"
        private const val ANIMATION_DURATION = 400L

        private val TransitionProperties = arrayOf(PROP_HEIGHT, PROP_VIEW_TYPE)
    }

    override fun getTransitionProperties(): Array<String> = TransitionProperties

    override fun captureStartValues(transitionValues: TransitionValues) {
        // Remember View start height...
        transitionValues.values[PROP_HEIGHT] = transitionValues.view.height
        transitionValues.values[PROP_VIEW_TYPE] = "start"

        // ... and then fix container height
        transitionValues.view.parent
            .let { it as? View }
            ?.also { view ->
                view.updateLayoutParams<ViewGroup.LayoutParams> {
                    height = view.height
                }
            }

    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        // measure and remember View height
        transitionValues.values[PROP_HEIGHT] = getViewHeight(transitionValues.view.parent as View)
        transitionValues.values[PROP_VIEW_TYPE] = "end"
    }

    override fun createAnimator(
        sceneRoot: ViewGroup?,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues == null || endValues == null) {
            return null
        }

        val animators = listOf<Animator>(
            prepareHeightAnimator(
                startValues.values[PROP_HEIGHT] as Int,
                endValues.values[PROP_HEIGHT] as Int,
                endValues.view
            ),
            prepareFadeInAnimator(endValues.view)
        )

        return AnimatorSet()
            .apply {
                interpolator = FastOutSlowInInterpolator()
                duration = ANIMATION_DURATION
                playTogether(animators)
            }
    }

    private fun prepareFadeInAnimator(view: View): Animator =
        ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)

    private fun prepareHeightAnimator(
        startHeight: Int,
        endHeight: Int,
        view: View
    ) = ValueAnimator.ofInt(startHeight, endHeight)
        .apply {
            val container = view.parent.let { it as View }

            // measure fragments container height
            addUpdateListener { animation ->
                container.updateLayoutParams<ViewGroup.LayoutParams> {
                    height = animation.animatedValue as Int
                }
            }

            // set height to WRAP_CONTENT on animation end
            doOnEnd {
                container.updateLayoutParams<ViewGroup.LayoutParams> {
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
            }
        }

    private fun getViewHeight(view: View): Int {
        // Get screen width
        val deviceWidth = getScreenWidth(view)

        // measure view height with the exact width
        val widthMeasureSpec =
            View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.EXACTLY)
        val heightMeasureSpec =
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

        return view
            // measure:
            .apply { measure(widthMeasureSpec, heightMeasureSpec) }
            // get measured height
            .measuredHeight
            // if the View wanna take more space than available, return screen height
            .coerceAtMost(getScreenHeight(view))
    }

    private fun getScreenHeight(view: View) =
        getDisplaySize(view).y - getStatusBarHeight(view.context)

    private fun getScreenWidth(view: View) =
        getDisplaySize(view).x

    private fun getDisplaySize(view: View) =
        Point().also { point ->
            view.context.getSystemService(Context.WINDOW_SERVICE)
                .let { it as WindowManager }
                .defaultDisplay
                .getSize(point)
        }

    private fun getStatusBarHeight(context: Context): Int =
        context.resources
            .getIdentifier("status_bar_height", "dimen", "android")
            .takeIf { resourceId -> resourceId > 0 }
            ?.let { resourceId -> context.resources.getDimensionPixelSize(resourceId) }
            ?: 0
}