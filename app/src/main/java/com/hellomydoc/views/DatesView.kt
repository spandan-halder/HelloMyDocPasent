package com.hellomydoc.views

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hellomydoc.*
import com.hellomydoc.data.DateSlotsSummary
import java.util.*

class DatesView : ConstraintLayout {
    private val itemLayout: Int = R.layout.appointment_dates_item_view_layout
    private var rvDates: RecyclerView? = null

    private val layoutId = R.layout.appointment_dates_view_layout
    //declare views here
    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context!!, attrs, defStyleAttr
    ) {
        init()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(
        context!!, attrs, defStyleAttr, defStyleRes
    ) {
        init()
    }

    private fun init() {
        inflate(context, layoutId, this)
        setupViews()
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        //bundle.putInt("stuff", this.stuff); // ... save stuff
        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        var state: Parcelable? = state
        if (state is Bundle) // implicit null check
        {
            //this.stuff = bundle.getInt("stuff"); // ... load stuff
            state = state.getParcelable("superState")
        }
        super.onRestoreInstanceState(state)
    }

    fun setData(data: List<DateSlotsSummary>){
        (rvDates?.adapter as? SliderAdapter)?.setData(data)
    }

    var onSelectedCallback: ((Int)->Unit)? = null

    private fun setupViews() {
        rvDates = findViewById(R.id.rv_dates)

        val padding: Int = ScreenUtils.getScreenWidth(context)/2 - ScreenUtils.dpToPx(context, 40)
        rvDates?.setPadding(padding, 0, padding, 0)

        rvDates?.layoutManager = SliderLayoutManager(context,RecyclerView.HORIZONTAL,false).apply {
            callback = object : SliderLayoutManager.OnItemSelectedListener {
                override fun onItemSelected(layoutPosition: Int) {
                    onSelectedCallback?.invoke(layoutPosition)
                }
            }
        }

        rvDates?.adapter = SliderAdapter().apply {
            setData(data)
            callback = object : Callback {
                override fun onItemClicked(view: View) {
                    rvDates?.getChildLayoutPosition(view)?.let { rvDates?.smoothScrollToPosition(it) }
                }
            }
        }
    }

    inner class SliderAdapter : RecyclerView.Adapter<SliderItemViewHolder>() {


        val data: MutableList<DateSlotsSummary> = MutableList(0){
            DateSlotsSummary(
                id = 0,
                date = "",
                slots = 0
            )
        }
        var callback: Callback? = null
        val clickListener = object : View.OnClickListener {
            override fun onClick(v: View?) {
                v?.let { callback?.onItemClicked(it) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderItemViewHolder {
            val itemView: View = LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)

            itemView.setOnClickListener(clickListener)

            val horizontalViewHolder = SliderItemViewHolder(itemView)
            return horizontalViewHolder
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: SliderItemViewHolder, position: Int) {
            val view = holder.viewItem
            view?.apply {
                val dateView = findViewById<TextView>(R.id.tv_date)
                val slotView = findViewById<TextView>(R.id.tv_slots)
                val positionData = data[position]
                positionData?.apply {
                    val dateValue = date.dateFormat(
                        R.string.mysql_date_format.string,
                        R.string.eee_mmm_d.string,
                        true)

                    dateView.text = dateValue

                        when{
                            slots<=0->{
                                dateView.setTextColor(R.color.white.color)
                                slotView.setTextColor(R.color.white.color)
                                slotView.text = R.string.no_slots.string
                                background = R.drawable.appointment_date_background_disabled.drawable()
                            }
                            slots>0->{
                                dateView.setTextColor(R.color.black.color)
                                slotView.setTextColor(R.color.gray.color)
                                slotView.text =slots.toString() + Constants.SPACE + R.string.slots.string
                                background = R.drawable.appointment_date_background_normal.drawable()
                            }
                            else -> ""
                        }
                }
            }
        }

        fun setData(data: List<DateSlotsSummary>) {
            this.data.clear()
            this.data.addAll(data)
            notifyDataSetChanged()
        }


    }

    inner class SliderItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val viewItem: ViewGroup? = itemView?.findViewById(R.id.item_view)
    }

    interface Callback {
        fun onItemClicked(view: View)
    }
}

fun String.dateFormat(inputFormat: String, outputFormat: String, b: Boolean): String {

    val date = Dt.calendar(inputFormat,this)?: Calendar.getInstance()
    val ret = if(!b){
        Dt.dateString(outputFormat,date)
    }
    else{
        val today = Dt.calendar(System.currentTimeMillis())
        when(Dt.dayDifCount(today,date)){
            -1->{
                R.string.yesterday.string+","+Dt.dateString(outputFormat,date).split(",").getOrNull(1)?:""
            }
            0->{
                R.string.today.string+","+Dt.dateString(outputFormat,date).split(",").getOrNull(1)?:""
            }
            1->{
                R.string.tomorrow.string+","+Dt.dateString(outputFormat,date).split(",").getOrNull(1)?:""
            }
            else->Dt.dateString(outputFormat,date)
        }
    }
    ret.apply {
        return this
    }
}


