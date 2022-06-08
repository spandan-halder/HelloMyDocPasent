package com.hellomydoc

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hellomydoc.AnyListView.AnyListAdapter.MyViewHolder
import com.hellomydoc.views.SliderLayoutManager


class AnyListView : RecyclerView {
    var adapter: AnyListAdapter? = null
    private var _layoutManager: LayoutManager? = null
    fun removeItem(index: Int) {
        if (adapter != null) {
            adapter?.notifyItemRemoved(index)
        }
    }

    fun notifyDataSetChanged() {
        adapter?.notifyDataSetChanged()
    }

    fun notifyItemRemoved(index: Int) {
        adapter?.notifyItemRemoved(index)
    }

    fun notifyItemInserted(index: Int) {
        adapter?.notifyItemInserted(index)
    }

    fun notifyItemChanged(index: Int) {
        adapter?.notifyItemChanged(index)
    }

    interface EventCallback {
        fun onScrollPx(dx: Int, dy: Int)
        fun onView(position: Int, view: View?)
        val animationDuration: Int
        val itemCount: Int
        fun getViewId(position: Int): Int
        fun getLayoutId(position: Int): Int
        fun layoutGrid(): Boolean
        fun gridColumnCount(): Int
        fun getItemView(position: Int): View?
        fun fromLayout(): Boolean
        val isVertical: Boolean
        val isReverse: Boolean
        val visibility: Int
        fun getItemType(position: Int): Int
        fun animateItem(): Boolean
        fun animation(position: Int): Int
        fun onItemSelected(layoutPosition: Int)
        fun isSlider(): Boolean
        fun layoutStaggered(): Boolean
        fun itemDecorations(): List<ItemDecoration>

        val isExpanded: Boolean
    }

    private var callback: EventCallback? = null
    //private var context: Context? = null

    @IdRes
    private var view_id = -1

    @LayoutRes
    private var layout_id = -1
    fun setup(callback: EventCallback?) {
        addOnScrollListener(object: OnScrollListener() {
            private var ddx = 0
            private var ddy = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                ddx += dx
                ddy += dy
                callback?.onScrollPx(ddx,ddy)
                super.onScrolled(recyclerView, dx, dy)
            }
        })
        layout_id = layout_id
        view_id = view_id
        this.callback = callback
        adjustVisibility()
        adapter = AnyListAdapter(context)
        setAdapter(adapter)
        setupLayoutManager()
        //addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
    }

    private fun adjustVisibility() {
        if (callback != null) {
            visibility = callback?.visibility?:View.VISIBLE
        }
    }

    private fun setupLayoutManager() {
        _layoutManager = if (callback != null) {
            if (callback?.layoutGrid()==true) {
                val span = callback?.gridColumnCount()?:1
                GridLayoutManager(context, span)
            }
            else if (callback?.layoutStaggered()==true) {
                val span = callback?.gridColumnCount()?:1
                StaggeredGridLayoutManager(span, StaggeredGridLayoutManager.VERTICAL)
            }
            else {
                newLayoutManager
            }
        } else {
            newLayoutManager
        }
        layoutManager = _layoutManager

        setupItemDecoration()
    }

    private fun setupItemDecoration() {
        callback?.itemDecorations()?.forEach {
            addItemDecoration(it)
        }
    }

    private val newLayoutManager: LayoutManager
        private get() {
            val slider = callback?.isSlider()?:false
            val vertical = callback?.isVertical?:false
            val reverse = callback?.isReverse?:false
            return LinearLaoutManagerProvider(slider, vertical, reverse)
            /*val vertical = isVerticalLayout
            if (!vertical) {
                val reverse = isReverse
                return LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, reverse)
            }
            return LinearLayoutManager(context)*/
        }

    private fun LinearLaoutManagerProvider(slider: Boolean = false, vertical: Boolean = true, reverse: Boolean = false): LinearLayoutManager{
        if(slider){
            return SliderLayoutManager(context,
                if(vertical) LinearLayoutManager.VERTICAL else LinearLayoutManager.HORIZONTAL,
                reverse
            ).apply {
                callback = object : SliderLayoutManager.OnItemSelectedListener {
                    override fun onItemSelected(layoutPosition: Int) {
                        this@AnyListView.callback?.onItemSelected(layoutPosition)
                    }
                }
            }
        }
        else{
            return LinearLayoutManager(context, if(vertical) LinearLayoutManager.VERTICAL else LinearLayoutManager.HORIZONTAL, reverse)
        }
    }

    private val isReverse: Boolean
        private get() = if (callback != null) {
            callback?.isReverse?:false
        } else false
    private val isVerticalLayout: Boolean
        private get() = if (callback != null) {
            callback?.isVertical?:true
        } else true

    private fun commonConstructor(context: Context) {
        //this.context = context
    }

    constructor(context: Context) : super(context) {
        commonConstructor(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        commonConstructor(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        commonConstructor(context)
    }

    /** */
    inner class AnyListAdapter     // Provide a suitable constructor (depends on the kind of dataset)
        (private val context: Context?) : Adapter<MyViewHolder>() {
        inner class MyViewHolder(itemView: View) : ViewHolder(itemView) {
            lateinit var thisItemView: View

            init {
                var done = false
                if (callback != null) {
                    if (callback?.fromLayout()==true) {
                        thisItemView = itemView.findViewById(view_id)
                        done = true
                    }
                }
                if(!done){
                    thisItemView = itemView
                }

            }
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            if (callback != null) {
                val view = callback?.getItemView(viewType)
                if (view != null) {
                    return MyViewHolder(view)
                }
            }
            val v =
                LayoutInflater.from(parent.context).inflate(getLayoutId(viewType), parent, false)
            return MyViewHolder(v)
        }

        override fun getItemViewType(position: Int): Int {
            return callback?.getItemType(position)?:0
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            if (callback != null) {
                if(callback?.animateItem()==true){
                    setAnimation(holder.thisItemView,position)
                }
                val view = holder.thisItemView
                callback?.onView(position, view)
            }
        }
        private var lastPosition = -1
        private fun setAnimation(viewToAnimate: View, position: Int) {
            if (position > lastPosition) {
                val animation: Animation =
                    AnimationUtils.loadAnimation(context, callback?.animation(position)?:R.anim.list_item_animation)

                if(callback?.animationDuration?:0>0){

                }

                animation.duration = 1000
                viewToAnimate.startAnimation(animation)
                lastPosition = position
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount(): Int {
            return if (callback != null) {
                callback?.itemCount?:0
            } else 0
        }
    }

    private fun getLayoutId(position: Int): Int {
        if (callback != null) {
            view_id = callback?.getViewId(position)?:0
            return callback?.getLayoutId(position)?:0
        }
        return 0
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (callback?.isExpanded == true) {
            val expandSpec = MeasureSpec.makeMeasureSpec(
                Int.MAX_VALUE shr 2,
                MeasureSpec.AT_MOST
            )
            super.onMeasure(widthMeasureSpec, expandSpec)
            return
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    enum class STATE{
        LOADING,
        NO_ITEMS,
        DATA
    }

    data class Configurator(
        val isSlider: Boolean = false,
        val onItemSelected: (Int)->Unit = {position->},
        val animationDuration: Int = 1000,
        val animation: (Int)->Int = {position->R.anim.list_item_animation},
        val animateItem: Boolean = false,
        val overScroll: Boolean = true,
        val noItemText: String = "No items yet",
        val onView:(Int,View?)->Unit = { position,view->},
        val itemCount: ()->Int = {0},
        val viewId: (Int)->Int = {positon->0},
        val layoutId: (Int)->Int = {positon->0},
        val isGrid: ()->Boolean = {false},
        val gridColumnCount: ()->Int = {1},
        val itemView: (Int)->View? = {position->null},
        val fromLayout: ()->Boolean = {false},
        val isVertical: ()->Boolean = {true},
        val isReverse: ()->Boolean = {false},
        val visibility: ()->Int = {View.VISIBLE},
        val itemType: (Int)->Int = {position->-1},
        val isExpanded: ()->Boolean = {false},
        val showNoItemsYetOnNoData: Boolean = true,
        val state:STATE = STATE.LOADING,
        val loaderView: View? = null,
        val noItemView: View? = null,
        val staggered: Boolean = false,
        val itemDecorations: ()->List<ItemDecoration> = { emptyList() },
        val onScrollPx: ((Int,Int)->Unit)? = null,
        val progressColor: (()->Int)? = {R.color.red.color}
    )

    fun configure(configurator: Configurator){
        if(!configurator.overScroll){
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        setup(object: EventCallback{
            override fun onScrollPx(dx: Int, dy: Int) {
                configurator.onScrollPx?.invoke(dx,dy)
            }

            override fun onView(position: Int, view: View?) {
                configurator.onView(position,view)
            }

            override val animationDuration: Int
                get() = configurator.animationDuration

            override val itemCount: Int
                get(){
                    return when(configurator.state){
                        STATE.LOADING -> 1
                        STATE.NO_ITEMS -> 1
                        STATE.DATA -> {
                            val count = configurator.itemCount()
                            if(count<1&&configurator.showNoItemsYetOnNoData){
                                return 1
                            }
                            return count
                        }
                    }
                }

            override fun getViewId(position: Int): Int {
                return configurator.viewId(position)
            }

            override fun getLayoutId(position: Int): Int {
                return configurator.layoutId(position)
            }

            override fun layoutGrid(): Boolean {
                return configurator.isGrid()
            }

            override fun gridColumnCount(): Int {
                return configurator.gridColumnCount()
            }

            override fun getItemView(position: Int): View? {
                return when(configurator.state){
                    STATE.LOADING -> configurator.loaderView?:progressView(configurator,position)
                    STATE.NO_ITEMS -> configurator.noItemView?:noItemView(configurator,position)
                    STATE.DATA -> {
                        if(configurator.itemCount()<1&&configurator.showNoItemsYetOnNoData){
                            configurator.noItemView?:noItemView(configurator,position)
                        }
                        else{
                            configurator.itemView(position)
                        }
                    }
                }
            }

            override fun fromLayout(): Boolean {
                return configurator.fromLayout()
            }

            override val isVertical: Boolean
                get(){return configurator.isVertical()}
            override val isReverse: Boolean
                get(){return configurator.isReverse()}
            override val visibility: Int
                get(){return configurator.visibility()}

            override fun getItemType(position: Int): Int {
                return configurator.itemType(position)
            }

            override fun animateItem(): Boolean {
                return configurator.animateItem
            }

            override fun animation(position: Int): Int {
                return configurator.animation(position)
            }

            override fun onItemSelected(layoutPosition: Int) {
                configurator.onItemSelected(layoutPosition)
            }

            override fun isSlider(): Boolean {
                return configurator.isSlider
            }

            override fun layoutStaggered(): Boolean {
                return configurator.staggered
            }

            override fun itemDecorations(): List<ItemDecoration> {
                return configurator.itemDecorations()
            }

            override val isExpanded: Boolean
                get(){ return configurator.isExpanded()}

        })
    }

    private fun noItemView(configurator: Configurator, position: Int): View? {
        return TextView(context).apply {
            text = configurator.noItemText
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams =
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT)
        }
    }

    private fun progressView(configurator: Configurator, position: Int): View {
        return ProgressBar(context).apply {
            indeterminateTintList = ColorStateList.valueOf(configurator.progressColor?.invoke()?:Color.BLACK)
            layoutParams =
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDetachedFromWindow() {
        clearOnScrollListeners()
        super.onDetachedFromWindow()
    }

    fun loading(color: Int = Color.BLACK) {
        configure(
            Configurator(
                progressColor = {color},
                itemType = {
                    it
                },
                state = STATE.LOADING,
            )
        )
    }

    fun clear() {
        configure(
            Configurator(
                itemType = {
                    it
                },
                state = STATE.DATA,
            )
        )
    }
}