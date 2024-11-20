package com.shaikhomes.smartdiary.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shaikhomes.anyrent.databinding.FloorLayoutBinding
import com.shaikhomes.smartdiary.ui.models.FlatData

class FlatAdapter(
    private val context: Context,
    private val leadsList: ArrayList<FlatData.FlatList>,
    private val isAdmin: Boolean? = false
) :
    RecyclerView.Adapter<FlatAdapter.LeadViewHolder>() {
    companion object {
        private const val POSITION_UNSET = -1
    }

    private var selectionPosition = POSITION_UNSET

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FlatAdapter.LeadViewHolder {
        return LeadViewHolder(
            FloorLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private var floorClickListener: ((FlatData.FlatList) -> Unit)? = null

    fun setFlatClickListener(leadList: (FlatData.FlatList) -> Unit) {
        this.floorClickListener = leadList
    }


    override fun onBindViewHolder(holder: LeadViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return leadsList.size
    }

    fun updateList(leadsList: List<FlatData.FlatList>) {
        this.leadsList.apply {
            if (this.size > 0) this.clear()
            this.addAll(leadsList)
            notifyDataSetChanged()
        }
    }

    fun clearSelection() {
        selectionPosition = POSITION_UNSET
        notifyDataSetChanged()
    }

    fun getList(): ArrayList<FlatData.FlatList> {
        return leadsList
    }

    inner class LeadViewHolder(val binding: FloorLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val position = adapterPosition
            val item = leadsList[position]
            if (position != RecyclerView.NO_POSITION) {
                binding.floorName.apply {
                    text = item.flatname
                }
                binding.root.isActivated = selectionPosition == position
                binding.root.setOnClickListener {
                    if (position != selectionPosition) {
                        val curPosition = selectionPosition
                        selectionPosition = position
                        floorClickListener?.invoke(item)
                        notifyItemChanged(curPosition)
                        notifyItemChanged(selectionPosition)
                    }
                }
            }
        }
    }
}