package com.example.waveview

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.waveview.databinding.BgBtnCreateNoteBinding

class CreateNoteButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding = BgBtnCreateNoteBinding.inflate(LayoutInflater.from(context), this)

    init {
        background = context.getDrawable(R.drawable.rounded_btn_create_note)
        elevation = getPx(10f)
    }

    fun getPx(dp: Float) : Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        )
    }
}