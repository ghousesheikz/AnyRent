package com.shaikhomes.smartdiary.ui.adapters

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.FloorLayoutBinding
import com.shaikhomes.anyrent.databinding.RoomsLayoutBinding
import com.shaikhomes.smartdiary.ui.models.Beds
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
    val bedsType = object : TypeToken<ArrayList<Beds>>() {}.type
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RoomsAdapter.LeadViewHolder {
        return LeadViewHolder(
            RoomsLayoutBinding.inflate(
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

    inner class LeadViewHolder(val binding: RoomsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val position = adapterPosition
            val item = leadsList[position]
            if (position != RecyclerView.NO_POSITION) {
                binding.floorName.apply {
                    text = item.roomname
                }
                binding.txtCapacity.text = "Capacity : ${item.roomcapacity}"
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
                binding.container.removeAllViews()
                // Add multiple ImageViews horizontally
                if (item.available != null) {
                    if (!item.available.isNullOrEmpty()) {
                        val list: ArrayList<Beds> = Gson().fromJson(item?.available, bedsType)
                        showAvailableOccupied(list)
                        list.forEach { bed ->
                            val imageView = createImageView(binding.root.context,bed)
                            binding.container.addView(imageView)
                        }
                    }
                }
            }
        }

        private fun showAvailableOccupied(list: ArrayList<Beds>) {
            var occupied: Int = 0
            var available: Int = 0
            list.forEach {
                if (it.userId.isNullOrEmpty()) {
                    occupied += 1
                }else available += 1
            }
            binding.txtOccupied.text = "Occupied : ${occupied}"
            binding.txtVacate.text = "Available : ${available}"
        }

        private fun createImageView(context: Context,beds: Beds): ImageView {
            val imageView = ImageView(context)
            // Set image properties
         //   imageView.setBackgroundResource(R.drawable.border_bg)
         //   imageView.setPadding(15,15,15,15)
            if(beds.userId.isNullOrEmpty()) {
                imageView.setImageResource(R.drawable.ic_bed) // Replace with your drawable resource
            }else imageView.setImageResource(R.drawable.ic_bed_occupied) // Replace with your drawable resource
            imageView.layoutParams = LinearLayout.LayoutParams(
                150, // Width in pixels
                150  // Height in pixels
            ).apply {
                setMargins(15, 15, 15, 15) // Add some margin between views
            }
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP // Optional: Adjust scaling
            return imageView
        }
    }
}