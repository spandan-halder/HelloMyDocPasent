package com.hellomydoc.shimmer

import android.content.Context
import android.widget.TextView
import com.hellomydoc.shimmer.ShimmerViewBase
import com.hellomydoc.shimmer.Shimmer
import com.hellomydoc.shimmer.ShimmerTextView
import com.hellomydoc.shimmer.ShimmerViewHelper
import com.hellomydoc.shimmer.ShimmerViewHelper.AnimationSetupCallback
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import com.hellomydoc.R

/**
 * Shimmer
 * User: romainpiel
 * Date: 06/03/2014
 * Time: 10:19
 *
 * Shimmering TextView
 * Dumb class wrapping a ShimmerViewHelper
 */
class ShimmerTextView : TextView, ShimmerViewBase {
    private val myPaint = Paint()
    private val clipPath = Path()
    private val shimmer = Shimmer()
    private var backShimmer = false
    private fun commonConstructor() {
        myPaint.color = Color.RED
        myPaint.style = Paint.Style.FILL
        shimmer
    }

    private var shimmerViewHelper: ShimmerViewHelper?

    constructor(context: Context?) : super(context) {
        shimmerViewHelper = ShimmerViewHelper(this, paint, null)
        shimmerViewHelper?.setPrimaryColor(currentTextColor)
        commonConstructor()
    }

    fun startShimmer(){
        shimmer.start(this)
    }

    fun cancelShimmer(){
        shimmer.cancel()
    }

    fun enableBackShimmer(yes: Boolean){
        backShimmer = yes
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        shimmerViewHelper = ShimmerViewHelper(this, paint, attrs)
        shimmerViewHelper?.setPrimaryColor(currentTextColor)
        commonConstructor()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        shimmerViewHelper = ShimmerViewHelper(this, paint, attrs)
        shimmerViewHelper?.setPrimaryColor(currentTextColor)
        commonConstructor()
    }

    override fun getGradientX(): Float {
        return shimmerViewHelper?.getGradientX()?:0f
    }

    override fun setGradientX(gradientX: Float) {
        shimmerViewHelper?.setGradientX(gradientX)
    }

    override fun isShimmering(): Boolean {
        return shimmerViewHelper!!.isShimmering
    }

    override fun setShimmering(isShimmering: Boolean) {
        shimmerViewHelper!!.isShimmering = isShimmering
    }

    override fun isSetUp(): Boolean {
        return shimmerViewHelper!!.isSetUp
    }

    override fun setAnimationSetupCallback(callback: AnimationSetupCallback) {
        shimmerViewHelper!!.setAnimationSetupCallback(callback)
    }

    override fun getPrimaryColor(): Int {
        return shimmerViewHelper?.getPrimaryColor()?:Color.BLACK
    }

    override fun setPrimaryColor(primaryColor: Int) {
        shimmerViewHelper?.setPrimaryColor(primaryColor)
    }

    override fun getReflectionColor(): Int {
        return shimmerViewHelper?.getReflectionColor()?:Color.BLACK
    }

    override fun setReflectionColor(reflectionColor: Int) {
        shimmerViewHelper?.setReflectionColor(reflectionColor)
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        if (shimmerViewHelper != null) {
            shimmerViewHelper?.setPrimaryColor(currentTextColor)
        }
    }

    override fun setTextColor(colors: ColorStateList) {
        super.setTextColor(colors)
        if (shimmerViewHelper != null) {
            shimmerViewHelper?.setPrimaryColor(currentTextColor)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (shimmerViewHelper != null) {
            shimmerViewHelper!!.onSizeChanged()
        }
    }

    public override fun onDraw(canvas: Canvas) {
        if (shimmerViewHelper != null) {
            shimmerViewHelper!!.onDraw()
            if (backShimmer&& shimmerViewHelper!!.isShimmering && shimmerViewHelper!!.layerPaint != null) {
                canvas.drawRect(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    shimmerViewHelper!!.layerPaint
                )
            }
        }
        /*if (shimmerViewHelper?.isShimmering != true) {
            super.onDraw(canvas)
        }*/
        super.onDraw(canvas)
    }

    override fun onDetachedFromWindow() {
        shimmer.clear()
        super.onDetachedFromWindow()
    }
}