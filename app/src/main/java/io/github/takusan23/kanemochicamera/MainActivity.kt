package io.github.takusan23.kanemochicamera

import android.Manifest
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Rational
import android.util.Size
import android.view.PixelCopy
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    val imageOpenCode = 1234

    var targetColor = Color.BLUE

    lateinit var bitmap: Bitmap

    lateinit var pref_setting: SharedPreferences

    lateinit var textureBitmap: Bitmap
    lateinit var bbBitmap: Bitmap

    //素材の配列
    val bbList = arrayListOf<BBCanvas>()
    //捜査中のBitmapCanvas
    var bbCanvas:BBCanvas?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        //素材配列に初めのやつ入れる
        bbList.add(bb_canvas)
        bbCanvas = bb_canvas

        pref_setting = PreferenceManager.getDefaultSharedPreferences(this)

        //画像選択
        select_img_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*";
            startActivityForResult(intent, imageOpenCode)
        }

        select_color_button.setOnClickListener {
            //EditText
            val editText = EditText(this)
            //ダイアログ
            val dialog = AlertDialog.Builder(this)
                .setTitle("透過するカラーコードを入力")
                .setView(editText)
                .setNegativeButton("キャンセル") { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
                .setPositiveButton("設定") { dialogInterface: DialogInterface, i: Int ->
                    val editor = pref_setting.edit()
                    editor.putString("target_color", editText.text.toString())
                    editor.apply()
                }
            dialog.show()
        }

        take_picture_button.setOnClickListener {
            getTextureViewBitmap()
        }

        //権限チェック
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //権限リクエスト
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            //カメラスタート
            startCamera()
        }

        //クリックイベント初期化
        initBBSizeChangeButton()

        //レイヤー
        layer_button.setOnClickListener {
            val layerBottomSheetFragment = LayerBottomSheetFragment()
            layerBottomSheetFragment.show(supportFragmentManager,"layer")
        }


    }

    fun initBBSizeChangeButton() {
        //倍率
        val zoomValue = pref_setting.getString("size_value", "5")?.toInt() ?: 5

        //素材の大きさ調整
        size_add_button.setOnClickListener {
            bbCanvas?.apply {
                bitmapHeight += bitmapAspectHeight * zoomValue
                bitmapWidth += bitmapAspectWidth * zoomValue
                bitmap = Bitmap.createScaledBitmap(bitmap!!, bitmapWidth, bitmapHeight, false)
                invalidate()
            }
        }
        size_remove_button.setOnClickListener {
            bbCanvas?.apply {
                bitmapHeight -= bitmapAspectHeight * zoomValue
                bitmapWidth -= bitmapAspectWidth * zoomValue
                bitmap = Bitmap.createScaledBitmap(bitmap!!, bitmapWidth, bitmapHeight, false)
                invalidate()
            }
        }
        size_value_button.setOnClickListener {
            //EditText
            val editText = EditText(this)
            //ダイアログ
            val dialog = AlertDialog.Builder(this)
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

    private fun startCamera() {

        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(textureView.width, textureView.height))
            setTargetRotation(windowManager.defaultDisplay.rotation)
            setTargetAspectRatio(Rational(9, 16))   //アスペクト比
        }.build()

        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = textureView.parent as ViewGroup
            parent.removeView(textureView)
            parent.addView(textureView, 0)

            textureView.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview)
    }

    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = textureView.width / 2f
        val centerY = textureView.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        textureView.setTransform(matrix)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == imageOpenCode) {
            val imageUri = data?.data
            if (imageUri != null) {
/*
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(this.contentResolver, imageUri)
                    bitmap = ImageDecoder.decodeBitmap(source)
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                }
*/

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

                false
            }
        }
    }

    fun getTextureViewBitmap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val bitmap =
                Bitmap.createBitmap(textureView.width, textureView.height, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            textureView.getLocationInWindow(locationOfViewInWindow)
            try {
                PixelCopy.request(
                    window,
                    Rect(
                        locationOfViewInWindow[0],
                        locationOfViewInWindow[1],
                        locationOfViewInWindow[0] + textureView.width,
                        locationOfViewInWindow[1] + textureView.height
                    ),
                    bitmap,
                    { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            textureBitmap = bitmap

                            //保存
                            val file =
                                File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
                            textureBitmap.compress(
                                Bitmap.CompressFormat.JPEG,
                                100,
                                file.outputStream()
                            )

                            Snackbar.make(
                                button_horizonal_scrollview,
                                "成功しました",
                                Snackbar.LENGTH_SHORT
                            ).setAnchorView(button_horizonal_scrollview).show()


                        } else {
                            Toast.makeText(this, "問題が発生しました", Toast.LENGTH_SHORT).show()
                        }
                        // possible to handle other result codes ...
                    },
                    Handler()
                )
            } catch (e: IllegalArgumentException) {
                // PixelCopy may throw IllegalArgumentException, make sure to handle it
                e.printStackTrace()
            }
        } else {
/*
            //今までの方法
            //PixelColor API がOreo以降じゃないと利用できないため
            paint_view.setDrawingCacheEnabled(true);
            paint_view.buildDrawingCache(true)
            val bitmap = paint_view.getDrawingCache(true).copy(Bitmap.Config.RGB_565, false)
            //nullチェック
            val uri = bitmapToUri(bitmap)
            if (uri != null) {
                intent.putExtra("paint_data", true)
                intent.putExtra("paint_uri", uri.toString())
                //画面推移
                startActivity(intent)
            } else {
                showToast(getString(R.string.paint_error_bitmap_to_uri))
            }
*/
        }
    }


    //置き換える
//https://stackoverflow.com/questions/7237915/replace-black-color-in-bitmap-with-red
    fun replaceColor(src: Bitmap, fromColor: Int, targetColor: Int): Bitmap {
        // Source image size
        val width = src.width
        val height = src.height
        val pixels = IntArray(width * height)
        //get pixels
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        for (x in pixels.indices) {
            pixels[x] = if (pixels[x] == fromColor) targetColor else pixels[x]
        }
        // create result bitmap output
        val result = Bitmap.createBitmap(width, height, src.config)
        //set pixels
        result.setPixels(pixels, 0, width, 0, 0, width, height)

        return result
    }
}
