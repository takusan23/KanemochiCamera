package io.github.takusan23.kanemochicamera

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import java.util.jar.Attributes

class PaintCanvas(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    var pathList: ArrayList<Path>
    var paintList: ArrayList<Paint>
    var color = Color.BLACK

    init {
        pathList = ArrayList()
        paintList = ArrayList()
        backgroundTintList = null
        //paint(context, attrs)
    }

/*
    fun paint(context: Context, attrs: AttributeSet?) {
        //super(context, attrs)
        //val path = Path()
        val paint = Paint()
        paint.setColor(Color.parseColor(colorCode))
        paint.setStyle(Paint.Style.STROKE)
        paint.setStrokeJoin(Paint.Join.ROUND)
        paint.setStrokeCap(Paint.Cap.ROUND)
        paint.setStrokeWidth(10f)
        paintList.add(paint)
    }
*/

    override fun onDraw(canvas: Canvas) {
        for (paint in paintList) {
            val position = paintList.indexOf(paint)
            val path = pathList.get(position)
            canvas.drawPath(path, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //タッチダウン（触れたらここ動く）
                //Pathを生成する
                val path = Path()
                path.moveTo(x, y)

                //Paint生成する。色変更とかここで
                val paint = Paint()
                paint.setColor(color) //変数にした。
                paint.setStyle(Paint.Style.STROKE)
                paint.setStrokeJoin(Paint.Join.ROUND)
                paint.setStrokeCap(Paint.Cap.ROUND)
                paint.strokeWidth = 20F
                //消しゴムモードはBitmap消すやつつける
                if (color == Color.TRANSPARENT) {
                    paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR) as Xfermode?);
                }

                //配列に入れる。
                //これで複数の色を扱えるようになる
                paintList.add(paint)
                pathList.add(path)
                //画面更新
                invalidate()

            }
            MotionEvent.ACTION_MOVE -> {
                //タッチダウンしたときに生成したPathを取得してなぞれるようにする
                val size = pathList.size - 1
                val path = pathList.get(size)
                path.lineTo(x, y)
                //なぞったら配列に戻して更新してあげる
                pathList.set(size, path)
                //画面更新
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                //タッチダウンしたときに生成したPathを取得して終了できるようにする
                val size = pathList.size - 1
                val path = pathList.get(size)
                path.lineTo(x, y)
                //なぞったら配列に戻して更新してあげる
                pathList.set(size, path)
                //画面更新
                invalidate()
            }
        }
        return true
    }

    fun clear() {
        //Path回す
        for (path in pathList) {
            //リセットさん
            path.reset()
        }
        invalidate()
    }

    fun setPan(colorInt: Int) {
        //カラーコード
        //色反映は画面に触れたとき。そのときにPaintが生成される
        color = colorInt
    }

    fun setEraser() {
        //カラーコード
        //色反映は画面に触れたとき。そのときにPaintが生成される
        color = Color.TRANSPARENT
    }

    /*
    * 一個前に戻す
    * */
    fun undoPaint() {
        //配列の中身から一個消す
        //Undoしすぎないようにする
        if (paintList.size - 1 >= 0) {
            paintList.removeAt(paintList.size - 1)
            pathList.removeAt(pathList.size - 1)
        } else {
            Toast.makeText(context, "これ以上戻すことはできません。", Toast.LENGTH_SHORT).show()
        }
        //画面更新
        invalidate()
    }

    /*
    * 現在のモードがペンかどうか
    * */
    fun isPenMode(): Boolean {
        //透明じゃなければ
        if (color != Color.TRANSPARENT) {
            return true
        }
        return false
    }


    /*
    * 現在のモードが消しゴムかどうか
    * */
    fun isEraserMode(): Boolean {
        //透明かどうか
        if (color == Color.TRANSPARENT) {
            return true
        }
        return false
    }
}

