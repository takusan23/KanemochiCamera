package io.github.takusan23.kanemochicamera

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class LayerRecyclerViewAdapter(private val arrayListArrayAdapter: ArrayList<ArrayList<*>>) :
    RecyclerView.Adapter<LayerRecyclerViewAdapter.ViewHolder>() {

    lateinit var mainActivity: MainActivity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_layer, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayListArrayAdapter.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = arrayListArrayAdapter[position] as ArrayList<String>
        val name = item.get(1)

        holder.layerTextView.text = name

        holder.layerEditButton.setOnClickListener {
            //Bitmap切り替える
            mainActivity.apply {
                bb_canvas_framelayout.removeAllViews()
                bbList.forEach {
                    bb_canvas_framelayout.addView(it)
                }
                bbCanvas = bbList[position]
                bbList[position].bringToFront()
            }
        }


    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var layerTextView: TextView
        var layerEditButton:Button

        init {
            layerTextView = itemView.findViewById(R.id.adapter_layer_layer_textview)
            layerEditButton = itemView.findViewById(R.id.adapter_layer_edit_button)
        }
    }
}