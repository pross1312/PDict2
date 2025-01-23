package com.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.data.PDictSqlite
import com.pdict.R;
import com.pdict.databinding.FragmentEntryBinding
import com.viewmodel.ViewModelEntry

class FragmentEntry: Fragment() {
    val viewModel: ViewModelEntry by lazy {
        ViewModelProvider(requireActivity())[ViewModelEntry::class.java];
    }
    var _binding: FragmentEntryBinding? = null;
    val binding get() = _binding!!;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntryBinding.inflate(inflater, container, false);
        binding.viewModel = viewModel;
        binding.lifecycleOwner = this;
        return binding.root;
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null;
    }

    fun search(keyword: String) {
        viewModel.search(keyword)
    }
}