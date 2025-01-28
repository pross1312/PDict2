package com.view

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.pdict.databinding.ActivityEditGroupBinding
import com.viewmodel.ViewModelEditGroup

class ActivityEditGroup: ComponentActivity() {
    val viewModel: ViewModelEditGroup by lazy {
        ViewModelProvider(viewModelStore, ViewModelEditGroup.factory, MutableCreationExtras().apply {
            set(ViewModelEditGroup.ID_KEY, intent.extras!!.getLong(ID_KEY_NAME))
        })[ViewModelEditGroup::class.java];
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityEditGroupBinding.inflate(layoutInflater)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setContentView(binding.root)

        binding.addBtn.setOnClickListener {
            val newGroup = binding.input.editableText.toString().trim()
            if (newGroup.isNotEmpty()) {
                if (!viewModel.addIfNotExist(newGroup)) {
                    // TODO: show some thing if can't add ?
                } else {
                    // TODO: show some thing if success ?
                }
            }
        }
        binding.input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.addBtn.performClick()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    companion object {
        val TAG = "PDict:ActivityEditGroup"
        val ID_KEY_NAME = "com.viewmodel.id"
    }
}
