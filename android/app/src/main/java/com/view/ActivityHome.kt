package com.view

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.data.PDictSqlite
import com.pdict.databinding.ActivityHomeBinding

class ActivityHome: AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding
    val fragmentEntry: FragmentEntry by lazy { supportFragmentManager.findFragmentById(binding.fragmentEntry.id) as FragmentEntry }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDictSqlite.instance.dir = filesDir.path
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.learnBtn.setOnClickListener {
            Log.i(TAG, "Learning")
            val intent = Intent(this, ActivityLearn::class.java)
            startActivity(intent)
        }

        binding.searchBtn.setOnClickListener {
            val keyword = binding.searchBox.editableText.toString().trim()
            if (keyword.isNotEmpty()) {
                Log.i(TAG, "Search for $keyword")
                fragmentEntry.search(keyword)
            }
        };

        binding.searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.searchBtn.performClick()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        if (!PDictSqlite.instance.isOpen && !PDictSqlite.instance.openDatabase(db_name, false)) {
            Log.i(TAG, "Could not open database $db_name")
            getContent.launch("*/*")
        }
    }

    override fun onStart() {
        super.onStart();
        fragmentEntry.search("ある")
    }

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            contentResolver.openInputStream(uri)?.let {
                PDictSqlite.instance.importDatabase(it, db_name);
            }
        }
    }


    companion object {
        val TAG = "PDict:ActivityHome"
        private val db_name: String = "pdict";
    }
}
