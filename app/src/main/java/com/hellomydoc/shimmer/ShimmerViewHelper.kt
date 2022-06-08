package com.hellomydoc.shimmer

import android.graphics.*
import com.hellomydoc.R
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.hellomydoc.color
import java.lang.Exception

/**
 * Shimmer
 * User: romainpiel
 * Date: 10/03/2014
 * Time: 17:06
 */
class ShimmerViewHelper(
    private val view: View,
    private val paint: Paint,
    attributeSet: AttributeSet?
) {
    var layerPaint = Paint()

    interface AnimationSetupCallback {
        fun onSetupAnimation(target: View?)
    }

    // center position of the gradient
    private var gradientX = 0f

    // shader applied on the text view
    // only null until the first global layout
    private var linearGradient: LinearGradient? = null
    private var overlayGradient: LinearGradient? = null

    // shader's local matrix
    // never null
    private var linearGradientMatrix: Matrix? = null
    private var primaryColor = 0

    // shimmer reflection color
    private var reflectionColor = 0

    // true when animating
    var isShimmering = false

    // true after first global layout
    var isSetUp = false
        private set

    // callback called after first global layout
    private var callback: AnimationSetupCallback? = null
    fun getGradientX(): Float {
        return gradientX
    }

    fun setGradientX(gradientX: Float) {
        this.gradientX = gradientX
        view.invalidate()
    }

    fun setAnimationSetupCallback(callback: AnimationSetupCallback?) {
        this.callback = callback
    }

    fun getPrimaryColor(): Int {
        return primaryColor
    }

    fun setPrimaryColor(primaryColor: Int) {
        this.primaryColor = primaryColor
        if (isSetUp) {
            resetLinearGradient()
        }
    }

    fun getReflectionColor(): Int {
        return reflectionColor
    }

    fun setReflectionColor(reflectionColor: Int) {
        this.reflectionColor = reflectionColor
        if (isSetUp) {
            resetLinearGradient()
        }
    }

    private fun init(attributeSet: AttributeSet?) {
        reflectionColor = DEFAULT_REFLECTION_COLOR
        if (attributeSet != null) {
            val a = view.context.obtainStyledAttributes(attributeSet, R.styleable.ShimmerView, 0, 0)
            if (a != null) {
                try {
                    reflectionColor = a.getColor(
                        R.styleable.ShimmerView_reflectionColor,
                        DEFAULT_REFLECTION_COLOR
                    )
                } catch (e: Exception) {
                    Log.e("ShimmerTextView", "Error while creating the view:", e)
                } finally {
                    a.recycle()
                }
            }
        }
        linearGradientMatrix = Matrix()
    }

    private fun resetLinearGradient() {

        // our gradient is a simple linear gradient from textColor to reflectionColor. its axis is at the center
        // when it's outside of the view, the outer color (textColor) will be repeated (Shader.TileMode.CLAMP)
        // initially, the linear gradient is positioned on the left side of the view
        linearGradient = LinearGradient(
            -view.width.toFloat(), 0f, 0f, 0f, intArrayOf(
                primaryColor,
                reflectionColor,
                primaryColor
            ), floatArrayOf(
                0f,
                0.5f, 1f
            ),
            Shader.TileMode.CLAMP
        )
        overlayGradient = LinearGradient(
            -view.width.toFloat(), 0f, 0f, 0f, intArrayOf(
                R.color.shimmer_primary.color,
                reflectionColor,
                R.color.shimmer_primary.color
            ), floatArrayOf(
                0f,
                0.5f, 1f
            ),
            Shader.TileMode.CLAMP
        )
        paint.shader = linearGradient
        layerPaint.shader = overlayGradient
    }

    fun onSizeChanged() {
        resetLinearGradient()
        if (!isSetUp) {
            isSetUp = true
            callback?.onSetupAnimation(view)
        }
    }

    /**
     * content of the wrapping view's onDraw(Canvas)
     * MUST BE CALLED BEFORE SUPER STATEMENT
     */
    fun onDraw() {

        // only draw the shader gradient over the text while animating
        if (isShimmering) {

            // first onDraw() when shimmering
            if (paint.shader == null) {
                paint.shader = linearGradient
            }
            if (layerPaint.shader == null) {
                layerPaint.color = Color.GRAY
                layerPaint.style = Paint.Style.FILL
                layerPaint.shader = overlayGradient
            }

            // translate the shader local matrix
            linearGradientMatrix!!.setTranslate(2 * gradientX, 0f)

            // this is required in order to invalidate the shader's position
            linearGradient!!.setLocalMatrix(linearGradientMatrix)
            overlayGradient!!.setLocalMatrix(linearGradientMatrix)
        } else {
            // we're not animating, remove the shader from the paint
            paint.shader = null
            layerPaint.shader = null
        }
    }

    companion object {
        private const val DEFAULT_REFLECTION_COLOR = -0x1
    }

    init {
        init(attributeSet)
    }
}