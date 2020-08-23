package com.example.waveview

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class AudioRecordView : View {
    companion object {
        private const val MAX_REPORTABLE_AMP = 22760f //effective size,  max fft = 32760
        private const val UNINITIALIZED = 0f
        private const val RUN_SPEED = 7f
    }

    private val density = Resources.getSystem().displayMetrics.density
    private val chunkPaint = Paint()

    private var lastFFT = 0.toFloat()
    private var usageWidth = 0.toDouble()
    private var chunkHeights = ArrayList<Float>()
    private var chunkWidths = ArrayList<Float>()
    private var numberOfChunk = 0
    private var topBottomPadding = 10 * density

    private var chunkColor = Color.RED
    private var chunkWidth = 2 * density
    private var chunkSpace = 1 * density
    private var chunkMaxHeight = UNINITIALIZED
    private var chunkMinHeight = 3 * density  // don't recommendation size <= 10 dp
    private var chunkHorizontalScale = 0.0
    private var queue = ArrayList<Int>()

    private var isRecording = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init() {
        chunkPaint.strokeWidth = chunkWidth
        chunkPaint.color = chunkColor
    }

    private fun init(attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.AudioRecordView,
            0, 0
        ).apply {
            try {
                chunkSpace = getDimension(R.styleable.AudioRecordView_chunkSpace, chunkSpace)
                chunkMaxHeight = getDimension(R.styleable.AudioRecordView_chunkMaxHeight, chunkMaxHeight)
                chunkMinHeight = getDimension(R.styleable.AudioRecordView_chunkMinHeight, chunkMinHeight)

                chunkWidth = getDimension(R.styleable.AudioRecordView_chunkWidth, chunkWidth)
                chunkPaint.strokeWidth = chunkWidth

                chunkColor = getColor(R.styleable.AudioRecordView_chunkColor, chunkColor)
                chunkPaint.color = chunkColor

                setWillNotDraw(false)
                chunkPaint.isAntiAlias = true

                chunkHorizontalScale = (chunkWidth + chunkSpace).toDouble()
                spaceToNextChunk = chunkHorizontalScale
            } finally {
                recycle()
            }
        }
    }

    fun recreate() {
        lastFFT = 0f
        usageWidth = 0.0
        numberOfChunk = 0
        chunkHeights = ArrayList()
        invalidate()
    }

    fun update(fft: Int) {
        this.lastFFT = fft.toFloat()
        queue.add(fft)
    }

    fun startRecording() {
        isRecording = true
        invalidate()
    }

    fun stopRecording() {
        isRecording = false
    }

    private var spaceToNextChunk = 0.0
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val maxLineCount = width / chunkHorizontalScale
        val centerView = (height / 2).toFloat()

        if (chunkMaxHeight == UNINITIALIZED) {
            chunkMaxHeight = height - topBottomPadding * 2
        } else if (chunkMaxHeight > height - topBottomPadding * 2) {
            chunkMaxHeight = height - topBottomPadding * 2
        }

        val verticalDrawScale = chunkMaxHeight - chunkMinHeight
        if (verticalDrawScale == 0f) {
            return
        }
        if (isRecording) {
            spaceToNextChunk -= RUN_SPEED

            if (spaceToNextChunk <= 0.0) {
                spaceToNextChunk = chunkHorizontalScale
                lastFFT = if (queue.size > 0) {
                    queue.removeAt(0).toFloat()
                } else {
                    0f
                }
                val point = MAX_REPORTABLE_AMP / verticalDrawScale
                if (point == 0f) {
                    return
                }

                if (lastFFT == 0f) {
                    lastFFT = 1f // set default amplitude
                }

                var fftPoint = lastFFT / point

                fftPoint += chunkMinHeight

                if (fftPoint > chunkMaxHeight) {
                    fftPoint = chunkMaxHeight
                } else if (fftPoint < chunkMinHeight) {
                    fftPoint = chunkMinHeight
                }

                chunkWidths.add(chunkHeights.size, 0f)
                chunkHeights.add(chunkHeights.size, fftPoint)

                if (chunkWidths.size > maxLineCount) chunkWidths.removeAt(0)
                if (chunkHeights.size > maxLineCount) chunkHeights.removeAt(0)
            }
        }

        for (i in chunkHeights.size - 1 downTo 0) {

            val startX = width - chunkWidths[i]
            val stopX = width - chunkWidths[i]
            if (isRecording) {
                chunkWidths[i] = (chunkWidths[i] + RUN_SPEED)
            }

            val startY = centerView - chunkHeights[i] / 2
            val stopY = centerView + chunkHeights[i] / 2

            canvas.drawLine(startX, startY, stopX, stopY, chunkPaint)

        }
        postDelayed({
            invalidate()
        }, 16)
    }
}