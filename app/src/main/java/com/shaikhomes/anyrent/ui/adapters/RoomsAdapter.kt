package com.shaikhomes.anyrent.ui.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.RoomsLayoutBinding
import com.shaikhomes.anyrent.ui.models.Beds
import com.shaikhomes.anyrent.ui.models.RoomData

class RoomsAdapter(
    private val context: Context,
    private val leadsList: ArrayList<RoomData.RoomsList>,
    private val isAdmin: Boolean? = false,
    private val hideDelete: Boolean? = false
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

    private var bedClickListener: ((RoomData.RoomsList, Beds) -> Unit)? = null
    private var qrCodeClickListener: ((RoomData.RoomsList, Beds) -> Unit)? = null
    private var tenantClickListener: ((RoomData.RoomsList, Beds) -> Unit)? = null

    fun setBedClickListener(leadList: (RoomData.RoomsList, Beds) -> Unit) {
        this.bedClickListener = leadList
    }

    fun setQRCodeClickListener(leadList: (RoomData.RoomsList, Beds) -> Unit) {
        this.qrCodeClickListener = leadList
    }

    fun setTenantClickListener(leadList: (RoomData.RoomsList, Beds) -> Unit) {
        this.tenantClickListener = leadList
    }

    private var deleteClickListener: ((RoomData.RoomsList) -> Unit)? = null

    fun setDeleteClickListener(leadList: (RoomData.RoomsList) -> Unit) {
        this.deleteClickListener = leadList
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
                Log.v("ROOM_NAME", item.roomname.toString())
                binding.txtCapacity.text = "Capacity : ${item.roomcapacity}"
                binding.root.isActivated = selectionPosition == position
                binding.deleteRoom.setOnClickListener {
                    deleteClickListener?.invoke(item)
                }
                binding.root.setOnClickListener {
                    if (position != selectionPosition) {
                        val curPosition = selectionPosition
                        selectionPosition = position
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
                            val parentLayout = LinearLayout(binding.root.context).apply {
                                orientation = LinearLayout.VERTICAL
                                setPadding(16)
                                gravity = Gravity.CENTER // Optional: Center align the layout
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT
                                )
                                setBackgroundColor(Color.WHITE) // Set background color
                            }
                            val imageView = createImageView(binding.root.context, bed, item)
                            val qrCodeView = createQRCode(binding.root.context, bed, item)
                            val textView = createBedsNumber(binding.root.context, bed, item)
                            parentLayout.addView(imageView)
                            parentLayout.addView(textView)
                            parentLayout.addView(qrCodeView)
                            binding.container.addView(parentLayout)
                        }
                    }
                }
                if (hideDelete == true) binding.deleteRoom.visibility =
                    View.GONE else binding.deleteRoom.visibility = View.VISIBLE
            }
        }

        private fun showAvailableOccupied(list: ArrayList<Beds>) {
            var occupied: Int = 0
            var available: Int = 0
            list.forEach {
                if (it.userId.isNullOrEmpty()) {
                    occupied += 1
                } else available += 1
            }
            binding.txtOccupied.text = "Occupied : ${available}"
            binding.txtVacate.text = "Available : ${occupied}"
        }

        private fun createBedsNumber(
            context: Context,
            beds: Beds,
            item: RoomData.RoomsList
        ): TextView {
            // Create a TextView
            val textView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 5 // Add some margin between ImageView and TextView
                    gravity = Gravity.CENTER // Optional: Center align the text
                }
                text = beds.number
                textSize = 14f
                setTextColor(Color.BLACK)
            }

            return textView
        }

        private fun createQRCode(
            context: Context,
            beds: Beds,
            item: RoomData.RoomsList
        ): ImageView {
            val imageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 8 // Add some margin between ImageView and TextView
                    bottomMargin = 2 // Add some margin between ImageView and TextView
                    rightMargin = 8 // Add some margin between ImageView and TextView
                    leftMargin = 8 // Add some margin between ImageView and TextView
                    gravity = Gravity.CENTER // Optional: Center align the text
                }
            }
            // Set image properties
            //   imageView.setBackgroundResource(R.drawable.border_bg)
            //   imageView.setPadding(15,15,15,15)
            if (beds.userId.isNullOrEmpty()) {
                imageView.setImageResource(R.drawable.ic_qr_code) // Replace with your drawable resource
                imageView.setOnClickListener {
                    qrCodeClickListener?.invoke(item, beds)
                }
                imageView.visibility = View.VISIBLE
            } else imageView.visibility = View.GONE
            imageView.layoutParams = LinearLayout.LayoutParams(
                80, // Width in pixels
                80  // Height in pixels
            ).apply {
                setMargins(15, 15, 15, 5) // Add some margin between views
            }
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP // Optional: Adjust scaling
            return imageView
        }

        private fun createImageView(
            context: Context,
            beds: Beds,
            item: RoomData.RoomsList
        ): ImageView {
            val imageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 8 // Add some margin between ImageView and TextView
                    bottomMargin = 8 // Add some margin between ImageView and TextView
                    rightMargin = 8 // Add some margin between ImageView and TextView
                    leftMargin = 8 // Add some margin between ImageView and TextView
                    gravity = Gravity.CENTER // Optional: Center align the text
                }
            }
            // Set image properties
            //   imageView.setBackgroundResource(R.drawable.border_bg)
            //   imageView.setPadding(15,15,15,15)
            if (beds.userId.isNullOrEmpty()) {
                imageView.setImageResource(R.drawable.ic_bed) // Replace with your drawable resource
                imageView.setOnClickListener {
                    bedClickListener?.invoke(item, beds)
                }
            } else {
                imageView.setImageResource(R.drawable.ic_bed_occupied)
                imageView.setOnClickListener {
                    tenantClickListener?.invoke(item, beds)
                }

            }// Replace with your drawable resource
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