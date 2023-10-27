package com.jo.shadowlayout

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlin.math.max
import kotlin.math.sqrt

class ShadowLayout(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    private var shadowColor = 0
    private var shadowBlur = 0f
    private var shadowDx = 0f
    private var shadowDy = 0f
    private var shadowPaint: Paint
    private var erasePaint: Paint
    private var originBitmap: Bitmap? = null
    private var originPath: Path

    init {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.ShadowLayout, 0, 0)
        shadowBlur = attr.getDimension(
            R.styleable.ShadowLayout_shadowBlur,
            resources.getDimension(R.dimen.def_shadow_blur)
        )
        shadowDx = attr.getDimension(R.styleable.ShadowLayout_shadowDx, 0f)
        shadowDy = attr.getDimension(R.styleable.ShadowLayout_shadowDy, 0f)
        shadowColor = attr.getColor(
            R.styleable.ShadowLayout_shadowColor,
            context.getColor(R.color.color_default_shadow)
        )
        attr.recycle()
        clipChildren = false
        clipToPadding = false
        setWillNotDraw(false)
        shadowPaint = Paint()
        erasePaint = Paint()
        originPath = Path()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!originPath.isEmpty) {
            clearShadow(canvas)
        }
        buildDrawingCache()
        originBitmap = drawingCache
        originPath.reset()
        val leftSide: MutableList<Pair<Int, Int>> = mutableListOf()
        val rightSide: MutableList<Pair<Int, Int>> = mutableListOf()
        for (row in 0 until originBitmap!!.height) {
            for (col in 0 until originBitmap!!.width) {
                if (originBitmap!!.getPixel(col, row) != Color.TRANSPARENT) {
                    leftSide.add(Pair(col, row))
                    break
                }
            }
            for (col in (originBitmap!!.width - 1) downTo 0) {
                if (originBitmap!!.getPixel(col, row) != Color.TRANSPARENT) {
                    rightSide.add(Pair(col, row))
                    break
                }
            }
        }
        // TODO outBound 구하는 함수 제대로 만들기
        if (leftSide.isNotEmpty() && rightSide.isNotEmpty()) {
            // 우선 leftSide를 다 연결하고
            originPath.moveTo(leftSide[0].first.toFloat(), leftSide[0].second.toFloat())
            for (idx in 1 until leftSide.size) {
                originPath.lineTo(leftSide[idx].first.toFloat(), leftSide[idx].second.toFloat())
            }
            // leftSide, rightSide 연결한 뒤에
            originPath.lineTo(
                rightSide.last().first.toFloat(),
                rightSide.last().second.toFloat()
            )
            // rightSide 에 있는 것을 연결하고
            for (idx in (rightSide.lastIndex - 1) downTo 0) {
                originPath.lineTo(
                    rightSide[idx].first.toFloat(),
                    rightSide[idx].second.toFloat()
                )
            }
            // leftSide, rightSide 연결한다
            originPath.lineTo(leftSide[0].first.toFloat(), leftSide[0].second.toFloat())
        }
        drawShadow(canvas)
    }

    private fun clearShadow(canvas: Canvas) {
        val endBlurStep = shadowBlur.toInt()
        val maxD = max(shadowDx, shadowDy) * 2
        for (blurStep in 1..endBlurStep) {
            val matrix = Matrix()
            val widthScale =
                (width + blurStep - maxD) / width.toFloat()
            val heightScale =
                (height + blurStep - maxD) / height.toFloat()
            matrix.setScale(widthScale, heightScale)
            matrix.postTranslate(
                -((blurStep - maxD) / 2),
                -((blurStep - maxD) / 2)
            )
            matrix.postTranslate(
                shadowDx,
                shadowDy
            )
            val copyedPath = Path(originPath)
            copyedPath.transform(matrix)
            canvas.drawPath(copyedPath, erasePaint.apply {
                color = Color.TRANSPARENT
            })
        }
    }

    private fun drawShadow(canvas: Canvas) {
        val endBlurStep = shadowBlur.toInt()
        val maxD = max(shadowDx, shadowDy) * 2
        for (blurStep in 1..endBlurStep) {
            val matrix = Matrix()
            val widthScale =
                (width + blurStep - maxD) / width.toFloat()
            val heightScale =
                (height + blurStep - maxD) / height.toFloat()
            matrix.setScale(widthScale, heightScale)
            matrix.postTranslate(
                -((blurStep - maxD) / 2),
                -((blurStep - maxD) / 2)
            )
            matrix.postTranslate(
                shadowDx,
                shadowDy
            )
            val copyedPath = Path(originPath)
            copyedPath.transform(matrix)
            canvas.drawPath(copyedPath, shadowPaint.apply {
                isAntiAlias = true
                isDither = true
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                val calcAlpha =
                    (Color.alpha(shadowColor) * (1 - sqrt(blurStep.toFloat() / endBlurStep))).toInt()
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = Color.argb(
                    calcAlpha,
                    Color.red(shadowColor),
                    Color.green(shadowColor),
                    Color.blue(shadowColor)
                )
            })
        }
    }

}