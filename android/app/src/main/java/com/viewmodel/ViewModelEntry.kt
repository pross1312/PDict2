package com.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.data.PDictContract
import com.data.PDictSqlite
import com.pdict.databinding.DefinitionUsageItemBinding
import com.pdict.databinding.GroupItemBinding
import com.data.Entry

class GroupAdapter(private val data: List<String>) : RecyclerView.Adapter<GroupAdapter.ViewHolder>() {
    class ViewHolder(private val binding: GroupItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String) {
            binding.text.text = data;
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(GroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false));
    }

    override fun getItemCount(): Int = data.size;
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(data[position]);
}

class DefinitionUsageAdapter(private val data: List<String>) : RecyclerView.Adapter<DefinitionUsageAdapter.ViewHolder>() {
    class ViewHolder(private val binding: DefinitionUsageItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String) {
            binding.text.text = data;
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DefinitionUsageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false));
    }

    override fun getItemCount(): Int = data.size;
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(data[position]);
}

class ViewModelEntry : ViewModel() {
    val entry       : MutableLiveData<Entry> = MutableLiveData();
    val definitions : MutableList<String> = mutableListOf()
    val usages      : MutableList<String> = mutableListOf()
    val groups      : MutableList<String> = mutableListOf()

    val definitionAdapter = DefinitionUsageAdapter(definitions)
    val groupAdapter = GroupAdapter(groups)
    val usageAdapter = DefinitionUsageAdapter(usages)

    fun setEntry(newEntry: Entry) {
        entry.value = newEntry
        definitions.clear()
        definitions.addAll(newEntry.definitions)
        definitionAdapter.notifyDataSetChanged()

        usages.clear()
        usages.addAll(newEntry.usages)
        usageAdapter.notifyDataSetChanged()

        groups.clear()
        groups.addAll(newEntry.groups)
        groupAdapter.notifyDataSetChanged()
    }

    fun search(keyword: String): Boolean {
        val newEntry = PDictSqlite.instance.query(keyword)
        if (newEntry != null) setEntry(newEntry)
        return newEntry != null
    }

    fun nextword(): Boolean {
        val newEntry = PDictSqlite.instance.nextword()
        if (newEntry != null) setEntry(newEntry)
        return newEntry != null
    }
}
