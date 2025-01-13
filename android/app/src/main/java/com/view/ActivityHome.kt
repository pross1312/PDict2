package com.view

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.data.PDictSqlite
import com.pdict.R

class ActivityHome: AppCompatActivity(R.layout.activity_home) {
    val pdictDb by lazy { PDictSqlite(filesDir.path) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pdictDb.openDatabase("pdict", true);
    }

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            contentResolver.openInputStream(uri)?.let {
                pdictDb.importDatabase(it, "test");
            }
        }
    }


    companion object {
        val TAG = "PDict:ActivityHome"
    }
}