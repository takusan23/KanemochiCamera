package io.github.takusan23.kanemochicamera

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import androidx.appcompat.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
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
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    val imageOpenCode = 1234

    val permissionResultCode = 512

    var targetColor = Color.BLUE

    lateinit var bitmap: Bitmap

    lateinit var pref_setting: SharedPreferences

    lateinit var textureBitmap: Bitmap
    lateinit var bbBitmap: Bitmap

    //素材の配列
    val bbList = arrayListOf<BBCanvas>()
    //捜査中のBitmapCanvas
    var bbCanvas: BBCanvas? = null

    //外、うちカメラ
    var cameraLends = CameraX.LensFacing.BACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        //素材配列に初めのやつ入れる
        bbList.add(bb_canvas)
        bbCanvas = bb_canvas

        pref_setting = PreferenceManager.getDefaultSharedPreferences(this)

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
            requestPermissions(arrayOf(Manifest.permission.CAMERA), permissionResultCode)
        } else {
            //カメラスタート
            startCamera()
        }

        //レイヤー
        layer_button.setOnClickListener {
            val layerBottomSheetFragment = LayerBottomSheetFragment()
            layerBottomSheetFragment.show(supportFragmentManager, "layer")
        }

        //カメラ切り替え
        camera_back_flont_change_button.setOnClickListener {
            if (cameraLends == CameraX.LensFacing.BACK) {
                cameraLends = CameraX.LensFacing.FRONT
            } else {
                cameraLends = CameraX.LensFacing.BACK
            }
            CameraX.unbindAll()
            //カメラ開始
            startCamera()
        }

    }

    //素材再設置
    fun setBBView() {
        bb_canvas_framelayout.removeAllViews()
        bbList.forEach {
            bb_canvas_framelayout.addView(it)
        }
    }


    private fun startCamera() {

        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(textureView.width, textureView.height))
            setTargetRotation(windowManager.defaultDisplay.rotation)
            setTargetAspectRatio(Rational(9, 16))   //アスペクト比
            setLensFacing(cameraLends)//カメラ切り替え
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
                                take_picture_button,
                                "成功しました",
                                Snackbar.LENGTH_SHORT
                            ).setAnchorView(take_picture_button).setAction("写真を共有") {
                                //File.toUriは使えない(file://から始まるので
                                //content://から始まるUriを生成する
                                showShareScreen(generateUri(file))
                            }.show()


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
            //PixelColor API がOreo以降じゃないと利用できないため

            //TextureViewのBitmap取得（カメラ部分
            val textureBitmap = textureView.bitmap
            //Bitmap合成をするので元になるBitmap作成
            val finalBitmap = Bitmap.createBitmap(
                textureBitmap.width,
                textureBitmap.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(finalBitmap)
            //背景（かめら）描画
            canvas.drawBitmap(textureBitmap, 0F, 0F, null)
            //素材描画
            bbList.forEach {
                if (it.bitmap != null) {
                    //中心出す？
                    //図形の中心とタッチしてるところを合わせるため
                    val widthCenter = it.bitmap?.width?.div(2) ?: 0
                    val heightCenter = it.bitmap?.height?.div(2) ?: 0
                    canvas.drawBitmap(
                        it.bitmap!!,
                        it.xPos - widthCenter,
                        it.yPos - heightCenter,
                        null
                    )
                }
            }

            //保存
            val file =
                File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
            finalBitmap.compress(
                Bitmap.CompressFormat.JPEG,
                100,
                file.outputStream()
            )

            Snackbar.make(
                take_picture_button,
                "成功しました",
                Snackbar.LENGTH_SHORT
            ).setAnchorView(take_picture_button).setAction("写真を共有") {
                //File.toUriは使えない(file://から始まるので
                //content://から始まるUriを生成する
                showShareScreen(generateUri(file))
            }.show()

        }
    }

    //content://なUriを生成する
    private fun generateUri(file: File): Uri? {
        return FileProvider.getUriForFile(
            this,
            BuildConfig.APPLICATION_ID + ".fileprovider",
            file
        )
    }

    /*
    * 共有画面出す
    * */
    fun showShareScreen(uri: Uri?) {
        val shareCompat = ShareCompat.IntentBuilder.from(this)
        shareCompat.apply {
            setChooserTitle("写真を共有")
            setStream(uri)
            setType("image/jpeg")
            //開く
            startChooser()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionResultCode) {
            //成功？
            //配列になってるけどこれはリクエストの時配列を使えば複数権限をリクエストできるため結果もそうなる
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //カメラオープン
                startCamera()
            } else {
                //何もできない
                Toast.makeText(this, "権限が付与されませんでした。何もできません。", Toast.LENGTH_SHORT).show()
            }
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
