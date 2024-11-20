package com.shaikhomes.smartdiary.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shaikhomes.anyrent.databinding.FloorLayoutBinding
import com.shaikhomes.smartdiary.ui.models.FlatData
import com.shaikhomes.smartdiary.ui.models.RoomData

class RoomsAdapter(
    private val context: Context,
    private val leadsList: ArrayList<RoomData.RoomsList>,
    private val isAdmin: Boolean? = false
) :
    RecyclerView.Adapter<RoomsAdapter.LeadViewHolder>() {
    companion object {
        private const val POSITION_UNSET = -1
    }

    private var selectionPosition = POSITION_UNSET

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RoomsAdapter.LeadViewHolder {
        return LeadViewHolder(
            FloorLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private var floorClickListener: ((RoomData.RoomsList) -> Unit)? = null

    fun setRoomClickListener(leadList: (RoomData.RoomsList) -> Unit) {
        this.floorClickListener = leadList
    }


    override fun onBindViewHolder(holder: LeadViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return leadsList.size
    }

    fun updateList(leadsList: List<RoomData.RoomsList>) {
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

    fun getList(): ArrayList<RoomData.RoomsList> {
        return leadsList
    }

    inner class LeadViewHolder(val binding: FloorLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val position = adapterPosition
            val item = leadsList[position]
            if (position != RecyclerView.NO_POSITION) {
                binding.floorName.apply {
                    text = item.roomname
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