package io.github.takusan23.kanemochicamera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class BBCanvas(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    //めんどいのでBB素材一個だけな
    var xPos = 200F
    var yPos = 200F
    //Bitmap / Paint
    var bitmap: Bitmap? = null
    var paint = Paint()
    //Bitmapの大きさ
    var bitmapWidth = 100
    var bitmapHeight = 100
    //Bitmapのアスペクト比
    var bitmapAspectHeight = 16
    var bitmapAspectWidth = 9

    //タッチイベントを通すか
    //複数重ねるときに使ってね
    //非推奨
    var isTouchEvent = true

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // for (i in 0 until bbListBitmap.size) {
        // }

        if (bitmap != null) {

            //中心出す？
            //図形の中心とタッチしてるところを合わせるため
            val widthCenter = bitmap?.width?.div(2) ?: 0
            val heightCenter = bitmap?.height?.div(2) ?: 0
            //val bitmap = bbListBitmap[i]
            // val paint = bbListPaint[i]
            canvas?.drawBitmap(bitmap!!, xPos - widthCenter, yPos - heightCenter, paint)
        }

    }

    //最大公約数
    fun gcd(x: Int, y: Int): Int {
        if (y == 0) return x
        return gcd(y, x % y)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //if (isTouchEvent) {
        when (event?.action) {
            MotionEvent.ACTION_MOVE -> {
                xPos = event.x
                yPos = event.y
                invalidate()
            }
        }
        //  }
        return true
    }

    //bitmapWidth / bitmapHeight に値を入れる
    fun getBitmapSizeToValue() {
        if (bitmap != null) {
            bitmapHeight = bitmap?.height ?: 100
            bitmapWidth = bitmap?.width ?: 100
        }
    }

    //アスペクト比の計算。getBitmapSizeToValue()を呼んでから呼んでね
    fun calcAspect() {
        //アスペクト比
        bitmapAspectHeight = bitmapHeight / gcd(bitmapWidth, bitmapHeight)
        bitmapAspectWidth = bitmapWidth / gcd(bitmapWidth, bitmapHeight)
        println("アスペクト比 $bitmapAspectHeight:$bitmapAspectWidth")
    }

}