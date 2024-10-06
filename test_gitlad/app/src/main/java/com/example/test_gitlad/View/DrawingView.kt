package com.example.test_gitlad.View

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var paint: Paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND // Mặc định là nét bút tròn cho vẽ tự do
    }

    private var path = Path()
    private var bitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null

    private var startX = 0f // Tọa độ X điểm bắt đầu của đường thẳng
    private var startY = 0f // Tọa độ Y điểm bắt đầu của đường thẳng

    enum class BrushShape {
        STRAIGHT, ROUND, SQUARE, FREEHAND
    }

    private var currentBrushShape: BrushShape = BrushShape.FREEHAND // Default to FREEHAND

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        canvas.drawPath(path, paint)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmap!!)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                path.reset()
                path.moveTo(startX, startY)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                when (currentBrushShape) {
                    BrushShape.STRAIGHT -> {
                        // Draw straight line
                        path.reset()
                        path.moveTo(startX, startY)
                        path.lineTo(event.x, event.y)
                    }
                    BrushShape.FREEHAND -> {
                        // Draw freehand
                        path.lineTo(event.x, event.y)
                    }
                    BrushShape.ROUND, BrushShape.SQUARE -> {
                        // Continue to update shape
                        path.reset()
                        when (currentBrushShape) {
                            BrushShape.ROUND -> {
                                val radius = Math.hypot((event.x - startX).toDouble(), (event.y - startY).toDouble()).toFloat()
                                path.addCircle(startX, startY, radius, Path.Direction.CW)
                            }
                            BrushShape.SQUARE -> {
                                val left = Math.min(startX, event.x)
                                val top = Math.min(startY, event.y)
                                val right = Math.max(startX, event.x)
                                val bottom = Math.max(startY, event.y)
                                path.addRect(left, top, right, bottom, Path.Direction.CW)
                            }

                            BrushShape.STRAIGHT -> TODO()
                            BrushShape.FREEHAND -> TODO()
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                bitmapCanvas?.drawPath(path, paint)
                path.reset()
            }
        }
        invalidate()
        return true
    }

    fun clear() {
        path.reset()
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmap!!)
        invalidate()
    }

    fun save(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

    fun setBrushShape(shape: BrushShape) {
        currentBrushShape = shape
        paint.strokeCap = when (shape) {
            BrushShape.ROUND -> Paint.Cap.ROUND
            BrushShape.SQUARE -> Paint.Cap.SQUARE
            BrushShape.STRAIGHT -> Paint.Cap.BUTT
            BrushShape.FREEHAND -> Paint.Cap.ROUND // FREEHAND uses round cap
        }
        invalidate()
    }

    fun setBrushSize(size: Float) {
        paint.strokeWidth = size
        invalidate()
    }

    fun setBrushColor(color: Int) {
        paint.color = color
        invalidate()
    }
}
