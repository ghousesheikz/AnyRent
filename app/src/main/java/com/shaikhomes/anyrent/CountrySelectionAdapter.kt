package com.shaikhomes.anyrent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shaikhomes.anyrent.databinding.ItemCountryBinding
import com.shaikhomes.anyrent.ui.models.CountryCode
import com.shaikhomes.anyrent.ui.utils.hideKeyboard

class CountrySelectionAdapter(
    private var countryList: List<CountryCode>,
    private var selectedCountry: CountryCode?
) :
    RecyclerView.Adapter<CountrySelectionAdapter.CountryViewHolder>() {

    private var filteredList: MutableList<CountryCode> = countryList.toMutableList()

    private var onCountryClick: ((CountryCode) -> Unit)? = null

    fun onCountryClickListener(listener: (CountryCode) -> Unit) {
        this.onCountryClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        return CountryViewHolder(
            ItemCountryBinding.inflate(LayoutInflater.from(parent.context),parent, false)

        )
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(position)
    }

    fun filter(query: String?) {
        if (query == null) {
            filteredList = countryList.toMutableList()
            notifyDataSetChanged()
        } else {
            filteredList = countryList.filter {
                it.name.contains(query, ignoreCase = true) ||
                 it.dial_code.removePrefix("+").let { it.contains(query, ignoreCase = true)}
            }.toMutableList()
            notifyDataSetChanged()
        }
    }

    inner class CountryViewHolder(val itemCountryBinding: ItemCountryBinding) : RecyclerView.ViewHolder(itemCountryBinding.root) {
        fun bind(position: Int) {
            if (position != RecyclerView.NO_POSITION) {
                val country = filteredList[position]
                with(itemCountryBinding) {
                    textCountry?.text = country.name
                    textCountryCode?.text = country.dial_code.trim()
                    itemView.isActivated = selectedCountry?.name == country.name
                    root.setOnClickListener {
                        root.context.hideKeyboard(root)
                        onCountryClick?.invoke(country)
                    }
                }
            }
        }
    }
}