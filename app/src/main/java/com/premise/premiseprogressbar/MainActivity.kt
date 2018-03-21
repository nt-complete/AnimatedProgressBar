package com.premise.premiseprogressbar

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        completeButton.setOnClickListener {

            if (premiseProgressBar.shouldComplete) {
                premiseProgressBar.restart()
            } else {
                premiseProgressBar.complete()
                startActivity(Intent(this, DumbActivity::class.java))
            }
        }

        nextActivityButton.setOnClickListener {
            startActivity(Intent(this, DumbActivity::class.java))
        }
//        radiusSeekbar.max = 100
//        radiusSeekbar.progress = premiseProgressBar.outerAdjustment.toInt() * 10
//        radiusTextView.text = "Speed factor: ${premiseProgressBar.outerAdjustment}x"
//        radiusSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                val speedFactor = progress.toFloat() / 10.0.toFloat()
//                radiusTextView.text = "Speed factor: ${speedFactor}x"
//                premiseProgressBar.outerAdjustment = speedFactor
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//            }
//
//        })
    }
}
