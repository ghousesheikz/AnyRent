package com.shaikhomes.smartdiary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.DialogCountrySelectionBinding
import com.shaikhomes.smartdiary.ui.models.CountryCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CountrySelectionDialog : BottomSheetDialogFragment() {

    private var countrySelectionListner: ((CountryCode) -> Unit)? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var adapter: CountrySelectionAdapter? = null
    private var countryList: List<CountryCode>? = emptyList()
    private lateinit var dialogCountrySelectinBinding: DialogCountrySelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialogCountrySelectinBinding =
            DialogCountrySelectionBinding.inflate(inflater, container, false)
        return dialogCountrySelectinBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        coroutineScope.launch {
            adapter = CountrySelectionAdapter(
                countryList!!,
                CountryCode("", "", "+971")
            ).apply {
                onCountryClickListener {
                    dismiss()
                    countrySelectionListner?.invoke(it)
                }
            }
            dialogCountrySelectinBinding.recyclerViewCountry.adapter = adapter
        }
        dialogCountrySelectinBinding.editTextSearch?.doAfterTextChanged {
            val query = it?.toString()
            adapter?.filter(query)
        }
        dialog?.show()
    }

    private fun init(
        listener: (CountryCode) -> Unit,
        list: List<CountryCode>?
    ) {
        this.countrySelectionListner = listener
        this.countryList = list
    }

    @Keep
    companion object {
        /**
         * Create parameters in [VerifyOtpDialog]
         *
         * @param onClickListener Dialog Button click listener
         * @return [VerifyOtpDialog]
         */
        fun newInstance(
            listener: (CountryCode) -> Unit,
            list: List<CountryCode>?
        ): CountrySelectionDialog {
            val verifyOtpDialog = CountrySelectionDialog()
            verifyOtpDialog.init(listener, list)
            return verifyOtpDialog
        }
    }
}