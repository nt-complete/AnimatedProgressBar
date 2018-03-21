package com.premise.premiseprogressbar

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator

class RoundPremiseProgressBar : View {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        attrs?.apply {
            context.obtainStyledAttributes(attrs, R.styleable.PremiseProgressBar, defStyleAttr, 0).apply {
                progress = this.getFloat(R.styleable.PremiseProgressBar_progress, 0.0f)
            }
        }
    }

    val TAG = "RoundPremiseProgressBar"

    val whiteColor = Color.WHITE
    val redColor = Color.parseColor("#FC1D00")

    val polygons = mutableListOf<Polygon>()

    val linePaint = Paint().apply {
        isDither = true
        style = Paint.Style.STROKE
    }
    val pathMeasure = PathMeasure()

    var useRoundedEdges = true
    var shouldComplete = false
    var shouldFill = false

    var progress = 0f
        set (value) {
            field = value
            invalidate()
        }

    var fill = 0
        set (value) {
            field = value
            shouldFill = true
            invalidate()
        }

    var radius = 0f
    var duration = 1250L
        set(value) {
            field = value
            continuousAnimator?.duration = field
            completeAnimator?.duration = (field * .75).toLong()
        }

    var endProgress = 0f

    var overallProgress: Float = 0f
    set(value) {
        field = value
    }

    var continuousAnimator: ObjectAnimator? = null

    var completeAnimator: ObjectAnimator? = null

    var radiusAnimator: ObjectAnimator? = null

    var fillAnimator: ObjectAnimator? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        setupAnimations()
        continuousAnimator?.start()
    }

    fun complete() {
        shouldComplete = true
        endProgress = progress
        continuousAnimator?.cancel()

        Log.d(TAG, "Progress at the end: $endProgress")
        AnimatorSet().apply {
            play(completeAnimator).before(fillAnimator).with(radiusAnimator)
            start()
        }

    }

    fun setupAnimations() {
        continuousAnimator?.cancel()
        completeAnimator?.cancel()
        fillAnimator?.cancel()

        continuousAnimator = ObjectAnimator.ofFloat(this, "progress", 0f, 1f).apply {
            repeatMode = ObjectAnimator.RESTART
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
        }
        continuousAnimator!!.duration = duration

        completeAnimator = ObjectAnimator.ofFloat(this, "progress", .25f, 1f).apply {
            interpolator = LinearInterpolator()
        }
        completeAnimator!!.duration = (duration * 0.5).toLong()

        fillAnimator = ObjectAnimator.ofInt(this, "fill", 0, 255).apply {
            duration = 500L
            interpolator = LinearInterpolator()
        }
    }

    fun setupRadiusAnimator() {
        radiusAnimator?.cancel()

        radiusAnimator = ObjectAnimator.ofFloat(this, "radius", polygons[0].width, 0f).apply {
            interpolator = LinearInterpolator()
        }
        radiusAnimator!!.duration = (duration * .75).toLong()

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        var width: Float = (right - left).toFloat()
        val height: Float = (bottom - top).toFloat()

        val offset = (width / 10)
        val thickness = (width * 0.05).toFloat()

        width -= thickness

        polygons.clear()
        polygons.add(Polygon((thickness / 2), height - (thickness / 2), width, redColor))
        polygons.add(Polygon(offset, height - offset, width / 2, whiteColor))

        linePaint.strokeWidth = thickness

        setupRadiusAnimator()

    }

    fun restart() {
        progress = 0f
        endProgress = 0f
        shouldFill = false
        shouldComplete = false
        setupAnimations()
        setupRadiusAnimator()
        continuousAnimator?.start()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        polygons.forEach {
            linePaint.color = it.color
            if (shouldFill) {
                it.usingCircular = false
                linePaint.pathEffect = CornerPathEffect(radius)
                linePaint.style = Paint.Style.STROKE
                canvas?.drawPath(it.path, linePaint)
                linePaint.alpha = fill
                linePaint.style = Paint.Style.FILL
                canvas?.drawPath(it.path, linePaint)
            } else {
                linePaint.style = Paint.Style.STROKE
                if (shouldComplete) {
                    it.usingCircular = true
                    linePaint.pathEffect =
                                    DashPathEffect(
                                            floatArrayOf(
                                                    it.length * progress,
                                                    it.length * (1 - progress)),
                                            (it.length * (1 - endProgress)))

                    canvas?.drawPath(it.circularPath, linePaint)
                } else {
                    it.usingCircular = true
                    linePaint.pathEffect =
                            DashPathEffect(
                                    floatArrayOf(it.length * 0.25f, it.length * 0.75f), (it.length * (1 - progress)))

                    canvas?.drawPath(it.circularPath, linePaint)
                }
            }
        }

    }

    inner class Polygon(val x: Float, val y: Float, val width: Float, val color: Int) {
        val path by lazy {
            createPath()
        }

        val circularPath by lazy {
            createCircularPath()
        }

        val rect by lazy {
            createRect()
        }

        var usingCircular: Boolean = true
        set(value) {
            field = value
            if (usingCircular) {
                pathMeasure.setPath(circularPath, false)
            } else {
                pathMeasure.setPath(path, false)
            }
            val len = pathMeasure.length
            segmentLength = len / 4
            length = len
        }

        var segmentLength: Float = 0f
        var length: Float = 0f


        fun createRect(): RectF {
            return RectF().apply {
                left = x
                bottom = y
                right = (x + width)
                top = (y - width)
            }
        }

        fun createCircularPath(): Path {
            return Path().apply {
                addCircle(x + (width / 2), y - (width / 2), width / 2, Path.Direction.CW)
            }
        }

        fun createPath(): Path {
            return Path().apply {
                moveTo(x, y - (width / 2))
                lineTo(x, y - width)
                lineTo(x + width, y - width)
                lineTo(x + width, y)
                lineTo(x, y)
                close()
            }

        }
    }
}
