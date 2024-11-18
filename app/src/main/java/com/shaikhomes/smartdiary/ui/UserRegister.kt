package com.shaikhomes.smartdiary.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.shaikhomes.smartdiary.LoginActivity
import com.shaikhomes.smartdiary.MainActivity
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.ActivityUserRegisterBinding
import com.shaikhomes.smartdiary.ui.customviews.LoadDialog
import com.shaikhomes.smartdiary.ui.customviews.dismissProgress
import com.shaikhomes.smartdiary.ui.customviews.showProgress
import com.shaikhomes.smartdiary.ui.models.ResponseData
import com.shaikhomes.smartdiary.ui.models.UserDetailsList
import com.shaikhomes.smartdiary.ui.network.RetrofitInstance
import com.shaikhomes.smartdiary.ui.utils.currentdate
import com.shaikhomes.smartdiary.ui.utils.hideKeyboard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRegister : AppCompatActivity() {

    private lateinit var binding: ActivityUserRegisterBinding
    private var empType: String? = ""
    protected val loadDialog: LoadDialog by lazy {
        LoadDialog(this)
    }

    private fun validations(): Boolean {
        var flag = true
        if (binding.edtName.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show()
        } else if (binding.edtMobileNumber.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Number", Toast.LENGTH_SHORT).show()
        } else if (binding.edtAddress.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Address", Toast.LENGTH_SHORT).show()
        } else if (empType?.isEmpty() == true) {
            flag = false
            Toast.makeText(this, "Please Select Employee Type", Toast.LENGTH_SHORT).show()
        }
        return flag
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnSignup.setOnClickListener { view ->
            hideKeyboard(view)
            if (validations()) {
                postRegister(
                    binding.edtName.text.toString().trim(),
                    binding.edtMobileNumber.text.toString().trim(),
                    binding.edtAddress.text.toString().trim(),
                    view
                )
            }
        }
    }

    private fun postRegister(name: String, mobileNumber: String, address: String, view: View) {
        loadDialog.showProgress()
        val userData = UserDetailsList(
            Active = "1",
            IsAdmin = empType,
            OTP = "",
            UserMobileNo = mobileNumber,
            UserName = name,
            Address = address,
            DOB = "",
            Gender = "",
            Profession = "",
            GuardianType = "",
            GName = "",
            OtherInfo = "",
            GNumber = "",
            UserImage = "",
            ProofType = "",
            ProofNumber = "",
            ProofImageF = "",
            ProofImageB = "",
            CreatedBy = "mobileapp",
            UpdatedOn = currentdate(),

        )
        RetrofitInstance.api.postUser(userData)
            .enqueue(object : Callback<ResponseData> {
                override fun onResponse(
                    call: Call<ResponseData>,
                    response: Response<ResponseData>
                ) {
                    loadDialog.dismissProgress()
                    if (response.body()?.status == "200") {
                        Toast.makeText(
                            this@UserRegister,
                            "User Added Successfully",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        startActivity(
                            Intent(
                                this@UserRegister,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    } else {
                        return
                    }
                }

                override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                    loadDialog.dismissProgress()
                    Snackbar.make(view, t.message.toString(), Snackbar.LENGTH_LONG)
                        .setAction("", null).show()
                }
            })
    }
}