package com.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.MotionEvent
import androidx.fragment.app.FragmentActivity
import com.pdict.databinding.ActivityLearnBinding

class ActivityLearn : FragmentActivity() {
    lateinit var binding: ActivityLearnBinding
    val fragmentEntry: FragmentEntry by lazy { supportFragmentManager.findFragmentById(binding.fragmentEntry.id) as FragmentEntry }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nextBtn.setOnClickListener {
            if (!fragmentEntry.nextword()) {
                // TODO: display no word to learn
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            Log.i(TAG, "Activity learn touch event")
            fragmentEntry.toggleAnswer()
        }
        return true
    }

    companion object {
        val TAG = "PDict:ActivityLearn"
    }
}
