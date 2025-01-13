package com.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pdict.R;
import com.pdict.databinding.FragmentEntryBinding
import com.viewmodel.ViewModelEntry

class FragmentEntry: Fragment() {
    lateinit var viewModel: ViewModelEntry;
    var _binding: FragmentEntryBinding? = null;
    val binding get() = _binding!!;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[ViewModelEntry::class.java];
        _binding = FragmentEntryBinding.inflate(inflater, container, false);
        binding.viewModel = viewModel;
        binding.lifecycleOwner = this;
        return binding.root;
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null;
    }
}