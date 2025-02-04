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
        // Apparently this i a bug since API 1.0 https://stackoverflow.com/questions/45468698/app-create-a-new-instance-when-user-click-on-app-icon
        if (!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intent.action?.equals(Intent.ACTION_MAIN) ?: false) {
            Log.i(TAG, "Stupid bug striked again!!!")
            finish();
            return;
        }
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

        binding.editGroupBtn.setOnClickListener {
            fragmentEntry.currentEntry?.let {
                val intent = Intent(this, ActivityEditGroup::class.java)
                intent.putExtra(ActivityEditGroup.ID_KEY_NAME, it.id)
                Log.i(TAG, "Edit group for ${it.id}")
                startActivity(intent)
            }
        }

        if (!PDictSqlite.instance.isOpen && !PDictSqlite.instance.openDatabase(db_name, false)) {
            Log.i(TAG, "Could not open database $db_name")
            getContent.launch("*/*")
        } else {
            var keyword = intent.extras?.getString(SHARED_PREFERENCE_NAME) ?:
                          getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE).getString(SHARED_PREFERENCE_NAME, null)
            Log.i(TAG, "On create $keyword");
            if (keyword != null) {
                fragmentEntry.search(keyword)
            }
        }
    }

    override fun onPause() {
        Log.i(TAG, "On pause")
        if (intent.extras?.getString(SHARED_PREFERENCE_NAME) == null) {
            val last_search = fragmentEntry.currentKeyword
            if (last_search != null) {
                getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE).edit().apply {
                    putString(SHARED_PREFERENCE_NAME, last_search);
                    commit();
                }
            }
        }
        super.onPause()
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
        val SHARED_PREFERENCE_FILE = "pdict"
        val SHARED_PREFERENCE_NAME = "keyword"
    }
}
