package io.github.takusan23.kanemochicamera

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.acos

class LayerRecyclerViewAdapter(private val arrayListArrayAdapter: ArrayList<ArrayList<*>>) :
    RecyclerView.Adapter<LayerRecyclerViewAdapter.ViewHolder>() {

    lateinit var mainActivity: MainActivity
    lateinit var pref_setting: SharedPreferences

    //画像かぺいんとか
    var isPictureLayer = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_layer, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayListArrayAdapter.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = arrayListArrayAdapter[position] as ArrayList<String>
        val name = item.get(1)

        pref_setting = PreferenceManager.getDefaultSharedPreferences(mainActivity)

        val fragment =
            mainActivity.supportFragmentManager.findFragmentByTag("layer") as LayerBottomSheetFragment

        isPictureLayer = mainActivity.bbList[position] is BBCanvas

        if (isPictureLayer) {
            holder.layerImageView.setImageBitmap((mainActivity.bbList[position] as BBCanvas).bitmap)
            holder.layerEditButton.text = mainActivity.getString(R.string.position_change)
        } else {
            holder.layerImageView.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_gesture_24px))
            holder.layerEditButton.text = mainActivity.getString(R.string.draw)
        }

        holder.layerEditButton.setOnClickListener {
            //Bitmap切り替える
            mainActivity.apply {
                bb_canvas_framelayout.removeAllViews()
                bbList.forEach {
                    bb_canvas_framelayout.addView(it)
                }
                if (isPictureLayer) {
                    bbCanvas = bbList[position] as BBCanvas
                }
                bbList[position].bringToFront()
            }
        }

        //画像変更
        if (isPictureLayer) {
            holder.changeImageButton.setOnClickListener {
                //Fragment取得？
                //まず位置変更可能な状態へ
                mainActivity.apply {
                    bbCanvas = bbList[position] as BBCanvas
                    bbList[position].bringToFront()
                }
                //画像編集用BottomSheetだs
                val selectImageResultBottomSheetFragment = SelectImageResultBottomSheetFragment()
                selectImageResultBottomSheetFragment.show(
                    mainActivity.supportFragmentManager,
                    "select"
                )
            }
        } else {
            holder.changeImageButton.visibility = View.GONE
        }

        //ザイズ変更
        if (isPictureLayer) {
            (mainActivity.bbList[position] as BBCanvas).apply {
                //倍率
                val zoomValue = pref_setting.getString("size_value", "5")?.toInt() ?: 5
                holder.sizeAddButton.setOnClickListener {
                    //拡大
                    bitmapHeight += bitmapAspectHeight * zoomValue
                    bitmapWidth += bitmapAspectWidth * zoomValue
                    bitmap = Bitmap.createScaledBitmap(bitmap!!, bitmapWidth, bitmapHeight, false)
                    invalidate()
                }
                holder.sizeMinusButton.setOnClickListener {
                    //縮小
                    bitmapHeight -= bitmapAspectHeight * zoomValue
                    bitmapWidth -= bitmapAspectWidth * zoomValue
                    bitmap = Bitmap.createScaledBitmap(bitmap!!, bitmapWidth, bitmapHeight, false)
                    invalidate()
                }
            }
        } else {
            //ペイントモードの時は非表示
            holder.sizeAddButton.visibility = View.GONE
            holder.sizeMinusButton.visibility = View.GONE
        }

        mainActivity.apply {
            //コピー
            holder.copyButton.setOnClickListener {
                if (isPictureLayer) {
                    //val bbCanvas = bbList[position]
                    val bbCanvas = bbList[position] as BBCanvas
                    val copyBBCanvas = BBCanvas(mainActivity, null)
                    //値をわたす
                    copyBBCanvas.apply {
                        bitmap = bbCanvas.bitmap
                        xPos = bbCanvas.xPos
                        yPos = bbCanvas.yPos
                        bitmapWidth = bbCanvas.bitmapWidth
                        bitmapHeight = bbCanvas.bitmapHeight
                        bitmapAspectHeight = bbCanvas.bitmapAspectHeight
                        bitmapAspectWidth = bbCanvas.bitmapAspectWidth
                    }
                    bbList.add(copyBBCanvas)
                    fragment.setRecyclerViewList()
                    //Bitmap切り替える
                    setBBView()
                    this.bbCanvas = bbList[position] as BBCanvas
                    bbList[position].bringToFront()
                } else {
                    holder.copyButton.visibility = View.GONE
                }
            }
            //削除
            holder.deleteButton.setOnClickListener {
                bbList.removeAt(position)
                //再生成
                fragment.setRecyclerViewList()
                setBBView()
            }
            //非表示
            holder.visibilityButton.setOnClickListener {
                if (bbList[position].visibility == View.GONE) {
                    bbList[position].visibility = View.VISIBLE
                } else {
                    bbList[position].visibility = View.GONE
                }
                setBBView()
            }

            //もどす
            if(isPictureLayer){
                holder.paintBackButton.visibility = View.GONE
            }else{
                holder.paintBackButton.visibility = View.VISIBLE
            }
            holder.paintBackButton.setOnClickListener{
                (bbList[position] as PaintCanvas).undoPaint()
            }
        }

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //var layerTextView: TextView
        //位置変更
        var layerEditButton: Button
        //なんの画像？
        var layerImageView: ImageView
        //画像再選択
        var changeImageButton: Button
        //サイズ変更
        var sizeAddButton: Button
        var sizeMinusButton: Button
        //コピー・削除・非表示
        var copyButton: Button
        var deleteButton: Button
        var visibilityButton: Button
        //ペイント戻す
        var paintBackButton:Button

        init {
            //layerTextView = itemView.findViewById(R.id.adapter_layer_layer_textview)
            layerImageView = itemView.findViewById(R.id.adapter_layer_imageview)
            layerEditButton = itemView.findViewById(R.id.adapter_layer_edit_button)

            changeImageButton = itemView.findViewById(R.id.adapter_layer_change_image_button)

            sizeAddButton = itemView.findViewById(R.id.adapter_layer_size_add)
            sizeMinusButton = itemView.findViewById(R.id.adapter_layer_size_minus)

            copyButton = itemView.findViewById(R.id.adapter_layer_copy_button)
            deleteButton = itemView.findViewById(R.id.adapter_layer_delete_button)
            visibilityButton = itemView.findViewById(R.id.adapter_layer_visibility_button)

            paintBackButton = itemView.findViewById(R.id.adapter_layer_paint_back_button)

        }
    }
}