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
    }

    override fun onStart() {
        super.onStart()
        fragmentEntry.mode = FragmentEntry.EntryMode.Learn
        binding.nextBtn.setOnClickListener {
            if (!fragmentEntry.answerVisible) {
                fragmentEntry.answerVisible = true
            } else {
                if (!fragmentEntry.nextword()) {
                    // TODO: display no word to learn
                } else {
                    fragmentEntry.answerVisible = false
                }
            }
        }
    }

    companion object {
        val TAG = "PDict:ActivityLearn"
    }
}
