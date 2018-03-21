package com.premise.premiseprogressbar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class TestView : View {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val path by lazy {
        Path().apply {
            moveTo(0f, 0f)
            lineTo(width.toFloat(), 0f)
        }
    }

    val paint by lazy {
        Paint().apply {
            color = Color.RED
            strokeWidth = height.toFloat()
            isDither = true
            style = Paint.Style.STROKE
            pathEffect =
                    DashPathEffect(
                            floatArrayOf(
                                    width * .2f, width * .3f
                            ),
                            width * .2f
                    )
        }

    }

    val fillPaint by lazy {
        Paint().apply {
            color = Color.BLUE
            style = Paint.Style.FILL
        }

    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fillPaint)

        canvas?.drawPath(path, paint)
    }
}
