package com.view

import android.os.Bundle
import android.view.LayoutInflater
import android.util.Log
import android.view.View
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
// import androidx.recyclerview.widget.DividerItemDecoration
import com.data.PDictSqlite
import com.pdict.R;
import com.pdict.databinding.FragmentEntryBinding
import com.viewmodel.ViewModelEntry

class FragmentEntry: Fragment() {
    public enum class EntryMode {
        Learn, Edit
    }
    val viewModel: ViewModelEntry by lazy {
        ViewModelProvider(requireActivity())[ViewModelEntry::class.java];
    }
    var _binding: FragmentEntryBinding? = null;
    val binding get() = _binding!!;
    private var _mode = EntryMode.Edit
    var mode: EntryMode
        get() = _mode
        set(value) {
            when (value) {
                EntryMode.Learn -> {
                    if (_mode != EntryMode.Learn) {
                        answerVisible = false
                        binding.pronounciation.focusable = View.NOT_FOCUSABLE
                        if (!viewModel.nextword()) {
                            // TODO: handle no word to learn
                        }
                    }
                }
                EntryMode.Edit -> {
                    answerVisible = true
                    binding.pronounciation.focusable = View.FOCUSABLE
                    // TODO: handle edit mode
                }
            }
            _mode = value
        }

    var answerVisible: Boolean
        get() = binding.keyword.visibility == View.VISIBLE
        set(visible) {
            binding.keyword.visibility = if (visible) { View.VISIBLE } else { View.INVISIBLE }
            binding.pronounciation.visibility = if (visible) { View.VISIBLE } else { View.INVISIBLE }
            // binding.groups.visibility = if (visible) { View.VISIBLE } else { View.INVISIBLE }
            // binding.definitions.visibility = if (visible) { View.VISIBLE } else { View.INVISIBLE }
            binding.usages.visibility = if (visible) { View.VISIBLE } else { View.INVISIBLE }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntryBinding.inflate(inflater, container, false);
        binding.viewModel = viewModel;
        binding.lifecycleOwner = this;
        // binding.groups.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL));
        return binding.root;
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun nextword(): Boolean = viewModel.nextword();
    fun search(keyword: String) = viewModel.search(keyword)
    fun toggleAnswer(): Boolean {
        answerVisible = !answerVisible
        return answerVisible
    }

    companion object {
        val TAG = "PDict:FragmentEntry"
    }
}
