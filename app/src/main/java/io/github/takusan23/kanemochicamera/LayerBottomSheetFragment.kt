package io.github.takusan23.kanemochicamera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_fragment_layer.*

class LayerBottomSheetFragment :BottomSheetDialogFragment(){

    lateinit var mainActivity: MainActivity

    var recyclerViewList: ArrayList<ArrayList<*>> = arrayListOf()
    lateinit var giftRecyclerViewAdapter: LayerRecyclerViewAdapter
    lateinit var recyclerViewLayoutManager: RecyclerView.LayoutManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_fragment_layer,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity

        //Layer
        bottom_layer_recyclerview.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(context)
        bottom_layer_recyclerview.layoutManager = mLayoutManager as RecyclerView.LayoutManager?
        giftRecyclerViewAdapter = LayerRecyclerViewAdapter(recyclerViewList)
        giftRecyclerViewAdapter.mainActivity = activity as MainActivity
        bottom_layer_recyclerview.adapter = giftRecyclerViewAdapter
        recyclerViewLayoutManager = bottom_layer_recyclerview.layoutManager!!

        //RecyclerView
        mainActivity.apply {
            bbList.forEach {
                //BBCanvasなViewなら
                val item = arrayListOf<String>()
                item.add("")
                item.add(bbList.indexOf(it).toString())
                recyclerViewList.add(item)
            }
        }

        //Canvas追加
        bottom_layer_add_button.setOnClickListener {
            val bbCanvas = BBCanvas(context,null)
            bbCanvas.isTouchEvent=false
            //素材配列に追加
            mainActivity.bbList.add(bbCanvas)
            //Viewを全消し＋再構築
            mainActivity.bb_canvas_framelayout.removeAllViews()
            mainActivity.bbList.forEach {
                mainActivity.bb_canvas_framelayout.addView(it)
            }
        }

    }
}