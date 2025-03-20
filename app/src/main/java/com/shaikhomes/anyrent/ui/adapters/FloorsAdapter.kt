package com.shaikhomes.anyrent.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shaikhomes.anyrent.databinding.FloorLayoutBinding

class FloorsAdapter(
    private val context: Context,
    private val leadsList: ArrayList<String>,
    private val isAdmin: Boolean? = false
) :
    RecyclerView.Adapter<FloorsAdapter.LeadViewHolder>() {
    companion object {
        private const val POSITION_UNSET = -1
    }

    private var selectionPosition = POSITION_UNSET

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FloorsAdapter.LeadViewHolder {
        return LeadViewHolder(
            FloorLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private var floorClickListener: ((String) -> Unit)? = null

    fun setFloorClickListener(leadList: (String) -> Unit) {
        this.floorClickListener = leadList
    }


    override fun onBindViewHolder(holder: LeadViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return leadsList.size
    }

    fun updateList(leadsList: List<String>) {
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

    fun getList(): ArrayList<String> {
        return leadsList
    }

    inner class LeadViewHolder(val binding: FloorLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val position = adapterPosition
            val item = leadsList[position]
            if (position != RecyclerView.NO_POSITION) {
                binding.floorName.apply {
                    text = "Floor ${item}"
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