package com.hellomydoc.views;

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class SliderLayoutManager(context:Context?, val ornt: Int, reverse: Boolean) : LinearLayoutManager(context) {

    init {
         orientation = ornt;
        reverseLayout = reverse
    }

    var callback: OnItemSelectedListener? = null
    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToWindow(view: RecyclerView?) {
        super.onAttachedToWindow(view)
        recyclerView = view!!

        // Smart snapping
        try {
            LinearSnapHelper().attachToRecyclerView(recyclerView)
        } catch (e: Exception) {
        }
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
        super.onLayoutChildren(recycler, state)
        scaleDownView()
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        if (orientation == LinearLayoutManager.HORIZONTAL) {
            val scrolled = super.scrollHorizontallyBy(dx, recycler, state)
            scaleDownView()
            return scrolled
        } else {
            return 0
        }
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        if (orientation == LinearLayoutManager.VERTICAL) {
            val scrolled = super.scrollVerticallyBy(dy, recycler, state)
            scaleDownView()
            return scrolled
        } else {
            return 0
        }
    }

    private fun scaleDownView() {
        if(orientation== HORIZONTAL){
            val mid = width / 2.0f
            for (i in 0 until childCount) {

                // Calculating the distance of the child from the center
                val child = getChildAt(i)
                child?.apply {
                    val childMid = (getDecoratedLeft(child) + getDecoratedRight(child)) / 2.0f
                    val distanceFromCenter = abs(mid - childMid)

                    // The scaling formula
                    var scale = distanceFromCenter/(width)

                    // Set scale to view
                    scale = interpolateScale(scale)
                    child.scaleX = scale
                    child.scaleY = scale
                }

            }
        }
        else{
            val mid = height / 2.0f
            for (i in 0 until childCount) {

                // Calculating the distance of the child from the center
                val child = getChildAt(i)
                child?.apply {
                    val childMid = (getDecoratedTop(child) + getDecoratedBottom(child)) / 2.0f
                    val distanceFromCenter = abs(mid - childMid)

                    // The scaling formula
                    var scale = distanceFromCenter/(height)

                    // Set scale to view
                    scale = interpolateScale(scale)
                    child.scaleX = scale
                    child.scaleY = scale
                }

            }
        }
    }

    private fun interpolateScale(scale: Float): Float {
        return (1 - abs(scale/4).coerceAtMost(1f))
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)

        // When scroll stops we notify on the selected item
        if (state.equals(RecyclerView.SCROLL_STATE_IDLE)) {
            if(orientation== HORIZONTAL){
                // Find the closest child to the recyclerView center --> this is the selected item.
                val recyclerViewCenterX = getRecyclerViewCenterX()
                var minDistance = recyclerView.width
                var position = -1
                for (i in 0 until recyclerView.childCount) {
                    val child = recyclerView.getChildAt(i)
                    val childCenterX = getDecoratedLeft(child) + (getDecoratedRight(child) - getDecoratedLeft(child)) / 2
                    var newDistance = Math.abs(childCenterX - recyclerViewCenterX)
                    if (newDistance < minDistance) {
                        minDistance = newDistance
                        position = recyclerView.getChildLayoutPosition(child)
                    }
                }

                // Notify on item selection
                callback?.onItemSelected(position)
            }
            else{
                // Find the closest child to the recyclerView center --> this is the selected item.
                val recyclerViewCenterY = getRecyclerViewCenterY()
                var minDistance = recyclerView.height
                var position = -1
                for (i in 0 until recyclerView.childCount) {
                    val child = recyclerView.getChildAt(i)
                    val childCenterY = getDecoratedTop(child) + (getDecoratedBottom(child) - getDecoratedTop(child)) / 2
                    var newDistance = Math.abs(childCenterY - recyclerViewCenterY)
                    if (newDistance < minDistance) {
                        minDistance = newDistance
                        position = recyclerView.getChildLayoutPosition(child)
                    }
                }

                // Notify on item selection
                callback?.onItemSelected(position)
            }
        }
    }

    private fun getRecyclerViewCenterX() : Int {
        return (recyclerView.right - recyclerView.left)/2 + recyclerView.left
    }

    private fun getRecyclerViewCenterY() : Int {
        return (recyclerView.bottom - recyclerView.top)/2 + recyclerView.top
    }

    interface OnItemSelectedListener {
        fun onItemSelected(layoutPosition: Int)
    }
}