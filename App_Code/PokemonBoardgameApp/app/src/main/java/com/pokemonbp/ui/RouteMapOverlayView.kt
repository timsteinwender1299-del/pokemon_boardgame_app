package com.pokemonbp.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.pokemonbp.data.RouteHotspot
import com.pokemonbp.data.RouteMapCoordinates

class RouteMapOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var mapView: SubsamplingScaleImageView? = null
    var hotspots: List<RouteHotspot> = emptyList()
    var legendaryNames: Set<String> = emptySet()
    var hoveredName: String? = null
    var showDebugGrid: Boolean = false

    private var imageW = 0
    private var imageH = 0
    private var selectedName: String? = null

    private val dp = resources.displayMetrics.density
    private val circleRadius = 22f * dp

    private val normalFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CC1565C0")
        style = Paint.Style.FILL
    }
    private val normalHoverFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EE1E88E5")
        style = Paint.Style.FILL
    }
    private val legendaryFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CCA01010")
        style = Paint.Style.FILL
    }
    private val legendaryHoverFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EEE53935")
        style = Paint.Style.FILL
    }
    private val selectedFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#DDFF8F00")
        style = Paint.Style.FILL
    }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f * dp
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        textSize = 15f * dp
        setShadowLayer(3f * dp, 0f, 0f, Color.BLACK)
    }
    private val gridLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#88FFFFFF")
        style = Paint.Style.STROKE
        strokeWidth = 1f * dp
    }
    private val gridLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
        textAlign = Paint.Align.LEFT
        textSize = 9f * dp
        setShadowLayer(2f * dp, 0f, 0f, Color.BLACK)
    }

    fun setImageDimensions(w: Int, h: Int) {
        imageW = w; imageH = h
        invalidate()
    }

    fun setSelected(name: String) {
        selectedName = name
        invalidate()
        postDelayed({ selectedName = null; invalidate() }, 400)
    }

    private fun drawDebugGrid(canvas: Canvas, iv: SubsamplingScaleImageView) {
        val steps = 10
        for (i in 0..steps) {
            val frac = i / steps.toFloat()

            // vertical line at x = frac
            val top    = iv.sourceToViewCoord(frac * imageW, 0f)
            val bottom = iv.sourceToViewCoord(frac * imageW, imageH.toFloat())
            if (top != null && bottom != null)
                canvas.drawLine(top.x, top.y, bottom.x, bottom.y, gridLinePaint)

            // horizontal line at y = frac
            val left  = iv.sourceToViewCoord(0f, frac * imageH)
            val right = iv.sourceToViewCoord(imageW.toFloat(), frac * imageH)
            if (left != null && right != null)
                canvas.drawLine(left.x, left.y, right.x, right.y, gridLinePaint)

            // label at intersection (i, 0) — top edge
            if (top != null && i > 0)
                canvas.drawText("%.1f".format(frac), top.x + 2f, top.y + gridLabelPaint.textSize, gridLabelPaint)

            // label at intersection (0, i) — left edge
            if (left != null && i > 0)
                canvas.drawText("%.1f".format(frac), left.x + 2f, left.y - 2f, gridLabelPaint)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val iv = mapView ?: return
        if (imageW == 0 || imageH == 0) return

        if (showDebugGrid) drawDebugGrid(canvas, iv)

        for (hs in hotspots) {
            val srcX = hs.x * imageW
            val srcY = hs.y * imageH
            val pt = iv.sourceToViewCoord(srcX, srcY) ?: continue

            val isHovered  = hs.routeName == hoveredName
            val isSelected = hs.routeName == selectedName
            val isLegendary = hs.routeName in legendaryNames

            if (!isHovered && !isSelected) continue

            val fill = when {
                isSelected  -> selectedFill
                isLegendary -> legendaryHoverFill
                else        -> normalHoverFill
            }
            val r = circleRadius * 1.25f
            canvas.drawCircle(pt.x, pt.y, r, fill)
            canvas.drawCircle(pt.x, pt.y, r, stroke)

            val label = RouteMapCoordinates.shortLabel(hs.routeName)
            val textY = pt.y + (labelPaint.textSize / 3f)
            canvas.drawText(label, pt.x, textY, labelPaint)
        }
    }
}
