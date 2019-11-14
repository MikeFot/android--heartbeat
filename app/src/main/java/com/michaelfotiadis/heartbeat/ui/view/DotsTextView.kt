package com.michaelfotiadis.heartbeat.ui.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet

import androidx.appcompat.widget.AppCompatTextView
import kotlin.properties.Delegates

/**
 * Written by  Michael Fotiadis
 * https://gist.github.com/MikeFot
 */
class DotsTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : AppCompatTextView(
    context,
    attrs,
    defStyleAttr
) {

    private var currentPosition: Int = 0
    private var prefix = ""

    private var dotsHandler by Delegates.notNull<Handler>()

    private var statusChecker: Runnable = object : Runnable {
        override fun run() {
            updateStatus()
            dotsHandler.postDelayed(this, INTERVAL.toLong())
        }
    }

    init {
        this.currentPosition = 0
        this.dotsHandler = Handler(Looper.getMainLooper())
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startRepeatingTask()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRepeatingTask()
    }

    fun startRepeatingTask() {
        dotsHandler.removeCallbacks(statusChecker)
        statusChecker.run()
    }

    fun stopRepeatingTask() {
        dotsHandler.removeCallbacks(statusChecker)
    }

    fun setPrefix(mPrefix: String) {
        this.prefix = mPrefix
    }

    fun setAndStop(charSequence: CharSequence) {
        stopRepeatingTask()
        text = charSequence
    }

    private fun updateStatus() {

        if (currentPosition == VALUES.size) {
            currentPosition = 0
        }

        text = String.format("%s%s", prefix, VALUES[currentPosition])
        currentPosition++
    }

    companion object {

        private const val INTERVAL = 350
        private val VALUES = arrayOf("   ", ".  ", ".. ", "...")
    }

}