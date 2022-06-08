package com.hellomydoc.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.LinearLayoutCompat
import com.hellomydoc.data.slots.DayPartSlot

class SlotsView : LinearLayoutCompat {
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
        orientation = VERTICAL
    }

    fun setData(slotSections: List<DayPartSlot>){
        removeAllViews()
        slotSections.forEachIndexed { index, slotSection ->
            renderSlotSection(index, slotSection)
        }
    }

    private var selectedPosition = -1
    private fun onCheckedCallback(checked: Boolean, pos: Int,slotPos: Int){
        //Log.d("fl1f2fsdfdf","$slotPos.$pos=$checked")
        val prev = selectedPosition
        selectedPosition = pos
        if(prev!=-1&&prev!=selectedPosition){
            (getChildAt(prev) as? SlotSectionView)
            ?.changeState(false)
        }
        if(checked){
            onChangeCallback?.invoke(pos,slotPos)
        }
        else{
            onChangeCallback?.invoke(-1,-1)
        }
        /*if(selectedPosition!=-1&&selectedPosition!=pos){
            (getChildAt(selectedPosition) as? SlotSectionView)?.changeState(false)
        }
        selectedPosition = pos//if(checked){pos}else{-1}
        if(checked){
            onChangeCallback?.invoke(pos,slotPos)
        }
        else{
            onChangeCallback?.invoke(-1,-1)
        }*/
    }

    var onChangeCallback: ((Int,Int)->Unit)? = null

    private fun renderSlotSection(index: Int, slotSection: DayPartSlot?) {
        slotSection?.let {
            addView(
                SlotSectionView(context).apply {
                    onCheckedCallback = {slotIndex,it->
                        onCheckedCallback(it,index,slotIndex)
                    }
                    setData(it)
                }
            )
        }
    }
}