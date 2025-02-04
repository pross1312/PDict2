package com.viewmodel

import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import android.util.Log
import android.view.ViewGroup
import android.view.LayoutInflater
import com.data.PDictSqlite
import com.pdict.databinding.EditGroupItemBinding
import com.pdict.R
import com.data.EditGroupEntry

class EditGroupAdapter(private var data: List<EditGroupEntry>, private val onEditGroupItemClicked: (entry: EditGroupEntry) -> Unit) : RecyclerView.Adapter<EditGroupAdapter.ViewHolder>() {
    class ViewHolder(private val binding: EditGroupItemBinding, private val onEditGroupItemClicked: (entry: EditGroupEntry) -> Unit): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: EditGroupEntry) {
            binding.item.text = data.group
            binding.item.setTextColor(if (data.isIncluded) {
                binding.root.context.resources.getColor(R.color.group_included, null)
            } else {
                binding.root.context.resources.getColor(R.color.text, null)
            })
            binding.item.setOnClickListener {
                data.isIncluded = !data.isIncluded
                binding.item.setTextColor(if (data.isIncluded) {
                    binding.root.context.resources.getColor(R.color.group_included, null)
                } else {
                    binding.root.context.resources.getColor(R.color.text, null)
                })
                onEditGroupItemClicked(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(EditGroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                          onEditGroupItemClicked)
    }

    override fun getItemCount(): Int = data.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(data[position])
}

class ViewModelEditGroup(val id: Long): ViewModel() {
    var groups: MutableList<EditGroupEntry> = PDictSqlite.instance.findGroups(id).toMutableList()
    val adapter = EditGroupAdapter(groups) { entry ->
        if (entry.isIncluded) {
            if (!PDictSqlite.instance.addGroup(id, entry.group)) {
                // TODO: handle add error
            }
        } else {
            if (PDictSqlite.instance.removeGroup(id, entry.group) <= 0) {
                // TODO: handle remove error
            }
        }
    }

    fun addIfNotExist(newGroup: String): Boolean {
        val groupExisted = groups.find { it -> it.group == newGroup } != null
        if (!groupExisted) {
            if (!PDictSqlite.instance.addGroup(id, newGroup)) {
                // TODO: handle add error
                return false
            } else {
                groups.add(0, EditGroupEntry(newGroup, true))
                adapter.notifyDataSetChanged() // Stupid notifyItemInserted not working and i'm too lazy to figure out why
                return true
            }
        }
        return false
    }

    companion object {
        val ID_KEY = object : CreationExtras.Key<Long> {}
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return when (modelClass.kotlin) {
                    ViewModelEditGroup::class -> modelClass.cast(ViewModelEditGroup(checkNotNull(extras[ID_KEY])))!!
                    else -> super.create(modelClass)
                }
            }
        }
        val TAG = "PDict:ViewModelEditGroup"
    }
}
