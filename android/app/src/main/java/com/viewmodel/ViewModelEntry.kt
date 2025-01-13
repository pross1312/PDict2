package com.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.pdict.databinding.AdapterItemBinding

data class Entry(
    var keyword: String = "tuong",
    var pronounciation: String = "t u o n g",
    var definitions: Array<String> = arrayOf("tuong", "tuong123", "i1o2j3oi2", "tuong", "tuong", "tuong123", "i1o2j3oi2", "tuong", "tuong", "tuong123", "i1o2j3oi2", "tuong", "tuong", "tuong123", "i1o2j3oi2", "tuong"),
    var usages: Array<String> = arrayOf("tuong123", "tuong123", "J!IO@#JO@", "J!IO@#JIO!@#", "tuong123", "tuong123", "J!IO@#JO@", "J!IO@#JIO!@#", "tuong", "tuong123", "i1o2j3oi2", "tuong", "tuong", "tuong123", "i1o2j3oi2", "tuong"),
);

class Adapter(private val data: Array<String>) : RecyclerView.Adapter<Adapter.ViewHolder>() {
    class ViewHolder(private val binding: AdapterItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String) {
            binding.text.text = data;
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(AdapterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false));
    }

    override fun getItemCount(): Int = data.size;
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(data[position]);
}

class ViewModelEntry: ViewModel() {
    val entry: MutableLiveData<Entry> = MutableLiveData(Entry());

    val definitionAdapter = Adapter(entry.value?.definitions!!)
    val usageAdapter = Adapter(entry.value?.usages!!)
}