package com.example.waveview

import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.waveview.databinding.ActivityMainBinding
import java.io.File
import java.lang.Exception
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

val TAG = "QuangLog"
class MainActivity : AppCompatActivity() {
    private val mediaRecorder = MediaRecorder()
    private var audioId = ""
    private var isRecording = false
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    //private val binding by lazy { BtnCreateNoteBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        prepareRecording()
        binding.btnStart.setOnClickListener {

            startRecording()
        }
        binding.btnEnd.setOnClickListener {
            stopRecording()
        }
    }

    fun prepareRecording() {
        if (!isRecording) {
            audioId = UUID.randomUUID().toString()
            val voiceNoteFile = getAudioFile(audioId)
            with(mediaRecorder) {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(44100 * 16)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(2)
                setOutputFile(voiceNoteFile.absolutePath)
                prepare()
            }
        }
    }

    fun startRecording() {
        if (!isRecording) {
            mediaRecorder.start()
            binding.recordView.audioRecord.startRecording()
            binding.recordView.btnActionVoiceRecord.setViewState(BtnActionVoiceRecordView.State.STATE_STOP)
            val timer = Timer()
            var i = 0
            val thread = Thread {
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        try {
                            if (isRecording) {
                                val currentMaxAmplitude = mediaRecorder.maxAmplitude
                                runOnUiThread {
                                    binding.recordView.audioRecord.update(currentMaxAmplitude) //redraw view
                                }
                                //i+=50
                            }
                        } catch (ex: Exception) {

                        }
                    }
                }, 0, 100)
            }
            thread.start()

            isRecording = true
        }
    }

    fun stopRecording() {
        if (isRecording) {
            binding.recordView.audioRecord.stopRecording()
            val voiceNoteFile = getAudioFile(audioId)
            if (stopAndReleaseRecorder()) {
                val mediaMetadataRetriever = MediaMetadataRetriever()
                try {
                    Log.i(TAG, "VoiceNoteFile: ${voiceNoteFile.absolutePath}")
                    mediaMetadataRetriever.setDataSource(voiceNoteFile.absolutePath)
                    val duration =
                        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    Log.i(TAG, "VoiceNote(${voiceNoteFile.name}): Duration=${duration}")
                } catch (e: RuntimeException) {
                    Log.e(TAG, "VoiceNoteFile: Failed to extract media data")
                }
            } else {
                voiceNoteFile.delete()
            }
        }
    }

    private fun stopAndReleaseRecorder(): Boolean {
        with(mediaRecorder) {
            return try {
                stop()
                true
            } catch (e: RuntimeException) {
                false
            } finally {
                release()
                isRecording = false
            }
        }
    }

    private fun getAudioFile(audioId: String) = File("${externalCacheDir?.absolutePath}","$audioId.m4a")
}