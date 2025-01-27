package com.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.MotionEvent
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.pdict.databinding.ActivityLearnBinding

class ActivityLearn : FragmentActivity() {
    lateinit var binding: ActivityLearnBinding
    val fragmentEntry: FragmentEntry by lazy { supportFragmentManager.findFragmentById(binding.fragmentEntry.id) as FragmentEntry }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearnBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.i(TAG, "On create")
    }

    override fun onStart() {
        Log.i(TAG, "On start")
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
        binding.editBtn.setOnClickListener {
            val intent = Intent(this, ActivityHome::class.java)
            intent.putExtra(ActivityHome.SHARED_PREFERENCE_NAME, fragmentEntry.currentKeyword)
            startActivity(intent)
        }
    }

    override fun onStop() {
        Log.i(TAG, "On stop")
        super.onStop()
    }
    override fun onDestroy() {
        Log.i(TAG, "On destroy")
        super.onDestroy()
    }

    companion object {
        val TAG = "PDict:ActivityLearn"
    }
}
