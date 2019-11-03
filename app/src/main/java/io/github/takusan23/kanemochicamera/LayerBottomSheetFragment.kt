package io.github.takusan23.kanemochicamera

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_fragment_layer.*

class LayerBottomSheetFragment : BottomSheetDialogFragment() {

    lateinit var mainActivity: MainActivity

    var recyclerViewList: ArrayList<ArrayList<*>> = arrayListOf()

    lateinit var layerRecyclerViewAdapter: LayerRecyclerViewAdapter
    lateinit var recyclerViewLayoutManager: RecyclerView.LayoutManager

    lateinit var pref_setting: SharedPreferences

    var imageOpenCode = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_fragment_layer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity

        pref_setting = PreferenceManager.getDefaultSharedPreferences(context)

        //Layer
        bottom_layer_recyclerview.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(context)
        bottom_layer_recyclerview.layoutManager = mLayoutManager as RecyclerView.LayoutManager?
        layerRecyclerViewAdapter = LayerRecyclerViewAdapter(recyclerViewList)
        layerRecyclerViewAdapter.mainActivity = activity as MainActivity
        bottom_layer_recyclerview.adapter = layerRecyclerViewAdapter
        recyclerViewLayoutManager = bottom_layer_recyclerview.layoutManager!!

        //RecyclerView
        setRecyclerViewList()


        //Canvas追加
        bottom_layer_add_button.setOnClickListener {
            val bbCanvas = BBCanvas(context, null)
            //bbCanvas.isTouchEvent = false
            mainActivity.apply {
                //素材配列に追加
                bbList.add(bbCanvas)
                //Viewを全消し＋再構築
                bb_canvas_framelayout.removeAllViews()
                bbList.forEach {
                    bb_canvas_framelayout.addView(it)
                }
                setRecyclerViewList()
                //移動可能な状態へ
                mainActivity.bbCanvas = bbCanvas
                bbCanvas.bringToFront()
            }

            //画像編集用BottomSheetだs
            val selectImageResultBottomSheetFragment = SelectImageResultBottomSheetFragment()
            selectImageResultBottomSheetFragment.show(mainActivity.supportFragmentManager, "select")

            //this@LayerBottomSheetFragment.dismiss()

        }

        //倍率設定
        bottom_layer_size_change_button.setOnClickListener {
            //EditText
            val editText = EditText(context)
            editText.setText(pref_setting.getString("size_value", "10"))
            //ダイアログ
            val dialog = AlertDialog.Builder(context!!)
                .setTitle("拡大、縮小の倍率設定。\n素材が荒くなるときは使ってみてください。")
                .setView(editText)
                .setNegativeButton("キャンセル") { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
                .setPositiveButton("設定") { dialogInterface: DialogInterface, i: Int ->
                    val editor = pref_setting.edit()
                    editor.putString("size_value", editText.text.toString())
                    editor.apply()
                }
            dialog.show()
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == imageOpenCode) {
            val imageUri = data?.data
            if (imageUri != null) {
                mainActivity.apply {
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

                    //Canvasに描画
                    bbCanvas?.bitmap = replaceColor(
                        bitmap,
                        Color.parseColor(pref_setting.getString("target_color", "#ffffff")),
                        Color.TRANSPARENT
                    )
                    //Bitmap大きさ
                    bbCanvas?.getBitmapSizeToValue()
                    //Bitmapアスペクト比計算
                    bbCanvas?.calcAspect()
                    //再描画
                    bbCanvas?.invalidate()

                    //とじる
                    this@LayerBottomSheetFragment.dismiss()

                    false
                }
            }
        }
    }


    fun setRecyclerViewList() {
        recyclerViewList.clear()
        mainActivity.apply {
            bbList.forEach {
                //BBCanvasなViewなら
                val item = arrayListOf<String>()
                item.add("")
                item.add(bbList.indexOf(it).toString())
                recyclerViewList.add(item)
            }
            layerRecyclerViewAdapter.notifyDataSetChanged()
        }
    }

}