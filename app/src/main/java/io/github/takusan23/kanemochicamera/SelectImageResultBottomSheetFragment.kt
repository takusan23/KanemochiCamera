package io.github.takusan23.kanemochicamera

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.blue
import androidx.core.graphics.red
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_fragment_select_image_result.*

class SelectImageResultBottomSheetFragment : BottomSheetDialogFragment() {

    private val imageOpenCode: Int = 1234
    lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_fragment_select_image_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity

        //画像選択画面出す
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*";
        startActivityForResult(intent, imageOpenCode)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == imageOpenCode) {
            val imageUri = data?.data
            if (imageUri != null) {
                mainActivity.apply {
                    //Bitmap取り出す。なんか非推奨だからなおさねば
                    val tmpBitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                    //ImageViewに出す
                    this@SelectImageResultBottomSheetFragment.bottom_select_image_image_view.setImageBitmap(
                        tmpBitmap
                    )

                    //プレビューを選択して透過画像を選べるように
                    this@SelectImageResultBottomSheetFragment.bottom_select_image_image_view.setOnTouchListener { v, event ->
                        when (event.action) {
                            MotionEvent.ACTION_MOVE -> {
                                val x = event.x
                                val y = event.y
                                if (x < tmpBitmap.width && y < tmpBitmap.height) {

                                    val pixel = tmpBitmap.getPixel(x.toInt(), y.toInt())
                                    val redValue = Color.red(pixel)
                                    val blueValue = Color.blue(pixel)
                                    val greenValue = Color.green(pixel)
                                    val color = Color.rgb(redValue, greenValue, blueValue)

                                    // val color = tmpBitmap.getColor(x.toInt(), y.toInt())
                                    //Color -> Hex
                                    //https://stackoverflow.com/questions/6539879/how-to-convert-a-color-integer-to-a-hex-string-in-android
                                    val hexColor =
                                        String.format("#%06X", 0xFFFFFF and color)
                                    this@SelectImageResultBottomSheetFragment.bottom_select_image_color_textinput.setText(
                                        hexColor
                                    )
                                    //変更する
                                    this@SelectImageResultBottomSheetFragment.bottom_select_image_image_view.setImageBitmap(
                                        replaceColor(
                                            tmpBitmap,
                                            color,
                                            Color.TRANSPARENT
                                        )
                                    )
                                }
                            }
                        }
                        true
                    }

                    //それぞれ色のボタン
                    this@SelectImageResultBottomSheetFragment.apply {
                        bottom_select_image_color_red_button.setOnClickListener {
                            bottom_select_image_color_textinput.setText("#ff0000")
                            setPreview(tmpBitmap)
                        }
                        bottom_select_image_color_blue_button.setOnClickListener {
                            bottom_select_image_color_textinput.setText("#0000ff")
                            setPreview(tmpBitmap)
                        }
                        bottom_select_image_color_green_button.setOnClickListener {
                            bottom_select_image_color_textinput.setText("#008000")
                            setPreview(tmpBitmap)
                        }
                        bottom_select_image_color_white_button.setOnClickListener {
                            bottom_select_image_color_textinput.setText("#ffffff")
                            setPreview(tmpBitmap)
                        }
                        bottom_select_image_color_black_button.setOnClickListener {
                            bottom_select_image_color_textinput.setText("#000000")
                            setPreview(tmpBitmap)
                        }
                    }


                    //実行ボタン押した
                    //ここで透過色を確定させる。
                    this@SelectImageResultBottomSheetFragment.bottom_select_image_ok_button.setOnClickListener {
                        //だいにゅ
                        bitmap = tmpBitmap
                        //透過色特定
                        val transparent =
                            this@SelectImageResultBottomSheetFragment.bottom_select_image_color_textinput.text.toString()
                        //Canvasに描画
                        bitmap = replaceColor(
                            bitmap,
                            Color.parseColor(transparent),
                            Color.TRANSPARENT
                        )
                        bbCanvas?.bitmap = bitmap

                        //Bitmap大きさ
                        bbCanvas?.getBitmapSizeToValue()
                        //Bitmapアスペクト比計算
                        bbCanvas?.calcAspect()
                        //再描画
                        bbCanvas?.invalidate()

                        //とじる
                        this@SelectImageResultBottomSheetFragment.dismiss()

                        //RecyclerView更新
                        val fragment =
                            mainActivity.supportFragmentManager.findFragmentByTag("layer") as LayerBottomSheetFragment
                        fragment.setRecyclerViewList()

                    }

                    false
                }
            }
        }
    }

    fun setPreview(bitmap: Bitmap) {
        //透過色特定
        val transparent =
            this@SelectImageResultBottomSheetFragment.bottom_select_image_color_textinput.text.toString()
        //変更する
        this@SelectImageResultBottomSheetFragment.bottom_select_image_image_view.setImageBitmap(
            mainActivity.replaceColor(
                bitmap,
                Color.parseColor(transparent),
                Color.TRANSPARENT
            )
        )
    }

}