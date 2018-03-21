package com.premise.premiseprogressbar

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator

class PremiseProgressBar : View {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        attrs?.apply {
            context.obtainStyledAttributes(attrs, R.styleable.PremiseProgressBar, defStyleAttr, 0).apply {
                progress = this.getFloat(R.styleable.PremiseProgressBar_progress, 0.0f)
                duration = this.getInt(R.styleable.PremiseProgressBar_duration, 1500).toLong()
                innerColor = this.getColor(R.styleable.PremiseProgressBar_innerColor, Color.parseColor("#D0021B"))
                outerColor = this.getColor(R.styleable.PremiseProgressBar_outerColor, Color.parseColor("#FC1D00"))
            }
        }
    }

    val TAG = "PremiseProgressBar"

    var innerColor = Color.parseColor("#D0021B")
    var outerColor = Color.parseColor("#FC1D00")

    val polygons = mutableListOf<Polygon>()

    val linePaint = Paint().apply {
        isDither = true
        style = Paint.Style.STROKE
    }
    val pathMeasure = PathMeasure()

    var shouldFill = false
    var shouldComplete = false

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

    var lengthMod = .25f

    var duration = 1500L
        set(value) {
            field = value
            continuousAnimator?.duration = field
        }

    var outerAdjustment = 2f

    var endProgress = 0f

    var continuousAnimator: ObjectAnimator? = null

    var animatorSet: AnimatorSet = AnimatorSet()

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        setupContinuousAnimation()
        continuousAnimator?.start()
    }

    /**
     * Cancels the current animation and starts the animation to complete.
     */
    fun complete() {
        endProgress = progress
        continuousAnimator?.cancel()
        shouldComplete = true

        if (animatorSet.childAnimations.isEmpty()) {
            val completeAnimator = ObjectAnimator.ofFloat(this, "progress", endProgress, 1f).apply {
                interpolator = DecelerateInterpolator()
            }
            completeAnimator.duration = (duration * (1f - endProgress)).toLong()

            val fillAnimator = ObjectAnimator.ofInt(this, "fill", 0, 255).apply {
                duration = 500L
                interpolator = LinearInterpolator()
            }

            animatorSet.apply {
                playSequentially(completeAnimator, fillAnimator)
                start()
            }
        } else {
            animatorSet.start()
        }
    }

    /**
     * Creates the continuous animation that runs indefinitely
     */
    fun setupContinuousAnimation() {
        cancelAnimations()

        continuousAnimator = ObjectAnimator.ofFloat(this, "progress", 0f, 1f).apply {
            repeatMode = ObjectAnimator.RESTART
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        continuousAnimator?.duration = duration
    }

    fun cancelAnimations() {
        continuousAnimator?.cancel()
        animatorSet.cancel()
    }

    /**
     * Figures out which animation should start and starts it
     */
    fun startAnimation() {
        if (shouldComplete) {
            complete()
        } else {
            continuousAnimator?.start()
        }
    }

    /**
     * Function is called when the View's size has changed, including on the initial setup
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        var width = w.toFloat()
        var height = h.toFloat()

        if (width > height) {
            width = height
        } else {
            height = width
        }

        val offset = (width / 10)
        val thickness = (width * 0.025).toFloat()

        width -= thickness

        polygons.clear()
        polygons.add(Polygon((thickness / 2), height - (thickness / 2), width, outerAdjustment, outerColor, outerColor))
        polygons.add(
                Polygon(offset, height - offset, width / 2, 1f, innerColor, Color.WHITE)
        )

        linePaint.strokeWidth = thickness
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            startAnimation()
        } else {
            cancelAnimations()
        }
    }

    fun restart() {
        progress = 0f
        endProgress = 0f
        fill = 0
        shouldFill = false
        shouldComplete = false
        setupContinuousAnimation()
        continuousAnimator?.start()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        polygons.forEach {
            if (shouldFill) {
                linePaint.color = it.startColor
                linePaint.pathEffect = null
                linePaint.style = Paint.Style.STROKE
                canvas?.drawPath(it.path, linePaint)
                linePaint.color = it.endColor
                linePaint.alpha = fill
                linePaint.style = Paint.Style.FILL_AND_STROKE
                canvas?.drawPath(it.path, linePaint)
            } else {
                linePaint.color = it.startColor
                linePaint.style = Paint.Style.STROKE
                linePaint.color = it.startColor
                linePaint.style = Paint.Style.STROKE

                // Set a length mod based on the idea that, at the beginning (and halfway through) the path should be
                // full size and at the halfway point it should be its smallest. However, I didn't like that it
                // disappeared entirely so I said it can't be smaller than 1/10 the total path length.
                lengthMod = (Math.max(.1f, (Math.abs(0.5 - progress) * 2).toFloat()))

                // The DashPathEffect takes 2 parameters:
                // 1. A list of floats - the first is the length of the "on" part of the path, and the second is the
                //    length of the "off". Ex. [length*.2f, length*.3f] would result in a the first 20% of the path
                //    being colored, and the next 30% being not colored (and this would repeat until the end)
                // 2. The beginning offset - how far into the previous list the path list would start. For the previous
                //    example, 0, length, length * 0.5 would all result in the same value.
                linePaint.pathEffect =
                        DashPathEffect(
                                floatArrayOf(
                                        it.length * lengthMod,
                                        it.length * (1 - lengthMod)),
                                (it.length * (1 - progress) * it.speedModifier)
                        )

                canvas?.drawPath(it.path, linePaint)
            }
        }

    }

    inner class Polygon(private val x: Float, private val y: Float, private val width: Float,
                        var speedModifier: Float,
                        @ColorInt val startColor: Int, @ColorInt val endColor: Int) {
        val path by lazy {
            Path().apply {
                moveTo(x, y - width)
                lineTo(x, y)
                lineTo(x + width, y)
                lineTo(x + width, y - width)
                close()
            }
        }

        val length by lazy {
            pathMeasure.setPath(path, false)
            pathMeasure.length
        }
    }
}
