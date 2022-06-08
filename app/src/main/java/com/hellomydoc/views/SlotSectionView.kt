package com.hellomydoc.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.hellomydoc.AnyListView
import com.hellomydoc.HorizontalSpaceItemDecoration
import com.hellomydoc.R
import com.hellomydoc.data.slots.DayPartSlot
import com.hellomydoc.dpToPx

class SlotSectionView : LinearLayoutCompat {
    private var head: TextView? = null
    private var chips: AnyListView? = null
    constructor(context: Context) : super(context) {init()}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {init()}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init(){
        inflate(context, R.layout.slot_section_layout, this)
        chips = findViewById(R.id.cg_slots)
        head = findViewById(R.id.tv_head)
    }

    var selectedIndex = -1
    var onCheckedCallback: ((Int,Boolean)->Unit)? = null
    var slotSectionData: DayPartSlot? = null
    fun setData(slotSection: DayPartSlot?){
        slotSectionData = slotSection
        head?.text = slotSection?.name?:""
        chips?.configure(
            AnyListView.Configurator(
                itemDecorations = {
                    val spanCount = 1
                    val spacing = 10.dpToPx
                    val includeEdge = false
                    //listOf(GridSpacingItemDecoration(spanCount, spacing, includeEdge))
                    listOf(HorizontalSpaceItemDecoration(spacing.dpToPx))
                },
                gridColumnCount = {1},
                isGrid = {false},
                isVertical = {false},
                itemType = {it},
                isExpanded = {false},
                state = AnyListView.STATE.DATA,
                itemCount = {slotSection?.slots?.size?:0},
                onView = {index,view->
                    val slotView = ((view as? ViewGroup)?.findViewById<View>(R.id.slot_chip) as? SlotChip)
                    slotView?.apply {
                        //Log.d("fdflfjldfs",slotSectionData?.slots?.getOrNull(index)?.selected.toString())
                        //changeState(slotSectionData?.slots?.getOrNull(index)?.selected==true)
                        Log.d("fjsdfdsf12","$selectedIndex==$index")
                        changeState(selectedIndex==index)
                        onCheckChanged = {
                            val prev = selectedIndex
                            if(it){
                                selectedIndex = index
                                onCheckedCallback?.invoke(index,it)
                            }
                            else{
                                selectedIndex = -1
                                onCheckedCallback?.invoke(index,it)
                            }
                            if(prev!=selectedIndex&&prev!=-1){
                                chips?.notifyItemChanged(prev)
                            }
                            //Log.d("fdflfjldfs1","$index:$it")
                            /*var k = false
                            if(index!=selectedIndex&&selectedIndex!=-1&&it){
                                k = true
                            }
                            onCheckedCallback?.invoke(index,it)
                            selectedIndex = index//if(it){index} else{-1}
                            if(k){
                                slotSectionData?.slots?.getOrNull(selectedIndex)?.selected = false
                                chips?.notifyItemChanged(selectedIndex)
                            }*/
                        }
                    }
                },
                itemView = {
                    LinearLayout(context).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
                        gravity = Gravity.CENTER
                        layoutParams = layoutParams
                        addView(
                            SlotChip(context).apply {
                                tag = "slot_chip"
                                id = R.id.slot_chip
                                text = (slotSection?.slots?.getOrNull(it)?.timestamp?:"$it")
                                maxLines = 1
                                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
                                layoutParams = layoutParams
                            }
                        )

                    }

                }
            )
        )
    }

    fun changeState(checked: Boolean) {
        if(!checked){
            var prev = selectedIndex
            selectedIndex = -1
            if(prev!=-1){
                slotSectionData?.slots?.getOrNull(prev)?.selected = false
                chips?.notifyItemChanged(prev)
            }
        }
    }
}