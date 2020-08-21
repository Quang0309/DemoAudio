package com.example.waveview

import android.content.Context
import android.util.AttributeSet

class BtnActionVoiceRecordView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {

    fun setViewState(state: State) {
        when(state) {
            State.STATE_PLAYING -> setImageResource(R.drawable.st_note_ic_pause)
            State.STATE_PAUSE -> setImageResource(R.drawable.st_note_ic_play)
            State.STATE_STOP -> setImageResource(R.drawable.st_note_ic_stop)
        }
    }
    enum class State {
        STATE_PLAYING,
        STATE_PAUSE,
        STATE_STOP
    }
}