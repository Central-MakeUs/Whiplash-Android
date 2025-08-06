package com.whiplash.presentation.component.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.whiplash.presentation.R

class OnboardingIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
    }

    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.grey_700)
    }

    private var totalCount = 4
    private var currentPosition = 0
    private val dotRadius = 8f
    private val dotSpacing = 16f

    fun setCurrentPosition(position: Int) {
        this.currentPosition = position
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = (totalCount * dotRadius * 2 + (totalCount - 1) * dotSpacing).toInt()
        val height = (dotRadius * 2).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerY = height / 2f
        var centerX = dotRadius

        for (i in 0 until totalCount) {
            val paint = if (i == currentPosition) activePaint else inactivePaint
            canvas.drawCircle(centerX, centerY, dotRadius, paint)
            centerX += dotRadius * 2 + dotSpacing
        }
    }
}