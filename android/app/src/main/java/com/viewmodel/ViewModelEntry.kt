package com.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.data.PDictContract
import com.data.PDictSqlite
import com.pdict.databinding.AdapterItemBinding

data class Entry(
    var id: Int = 0,
    var keyword: String = "",
    var pronounciation: String = "",
    var definitions: List<String> = emptyList(),
    var usages: List<String> = emptyList(),
    var groups: List<String> = emptyList(),
    var last_read: Int = 0,
);

class Adapter(private val data: List<String>) : RecyclerView.Adapter<Adapter.ViewHolder>() {
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

class ViewModelEntry : ViewModel() {
    val entry       : MutableLiveData<Entry> = MutableLiveData(Entry());
    val definitions : MutableList<String> = mutableListOf()
    val usages      : MutableList<String> = mutableListOf()
    val groups      : MutableList<String> = mutableListOf()

    val definitionAdapter = Adapter(definitions)
    val groupAdapter = Adapter(groups)
    val usageAdapter = Adapter(usages)

    var editable              : MutableLiveData<Boolean> = MutableLiveData(true)
    var keywordVisible        : MutableLiveData<Boolean> = MutableLiveData(true)
    var pronounciationVisible : MutableLiveData<Boolean> = MutableLiveData(true)
    var groupVisible          : MutableLiveData<Boolean> = MutableLiveData(true)
    var definitionVisible     : MutableLiveData<Boolean> = MutableLiveData(true)
    var usageVisible          : MutableLiveData<Boolean> = MutableLiveData(true)

    fun setEntry(newEntry: Entry) {
        entry.value = newEntry
        val definitionsOldLength = definitions.size
        definitions.clear()
        definitions.addAll(newEntry.definitions)
        definitionAdapter.notifyItemRangeChanged(0, maxOf(definitionsOldLength, definitions.size))

        val usagesOldLength = usages.size
        usages.clear()
        usages.addAll(newEntry.usages)
        usageAdapter.notifyItemRangeChanged(0, maxOf(usagesOldLength, usages.size))

        val groupsOldLength = groups.size
        groups.clear()
        groups.addAll(newEntry.groups)
        groupAdapter.notifyItemRangeChanged(0, maxOf(groupsOldLength, groups.size))
    }

    fun search(keyword: String) {
        val newEntry = PDictSqlite.instance.query(keyword)
        setEntry(newEntry ?: Entry())
    }

    fun nextword(): Boolean {
        val newEntry = PDictSqlite.instance.nextword()
        setEntry(newEntry ?: Entry())
        return newEntry != null
    }

    fun toggleAnswer() {
        keywordVisible.value = !(keywordVisible.value ?: false)
        pronounciationVisible.value = !(pronounciationVisible.value ?: false)
        definitionVisible.value = !(definitionVisible.value ?: false)
        usageVisible.value = !(usageVisible.value ?: false)
    }
}
