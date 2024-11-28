package com.shaikhomes.smartdiary

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.shaikhomes.anyrent.databinding.ActivityLoginBinding
import com.shaikhomes.smartdiary.ui.PropertyActivity
import com.shaikhomes.smartdiary.ui.customviews.LoadDialog
import com.shaikhomes.smartdiary.ui.customviews.dismissProgress
import com.shaikhomes.smartdiary.ui.customviews.showProgress
import com.shaikhomes.smartdiary.ui.models.UserRegister
import com.shaikhomes.smartdiary.ui.network.RetrofitInstance
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.hideKeyboard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    protected val loadDialog: LoadDialog by lazy {
        LoadDialog(this)
    }
    private var permission = arrayOf(android.Manifest.permission.CALL_PHONE)
    val MY_PERMISSIONS_REQUEST_CALL_PHONE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { view ->
            hideKeyboard(view)
            if (binding.edtMobileNumber.text?.length!! >= 7 && binding.otpView.getStringFromFields() == "122333") {
                getUserData(binding.edtMobileNumber.text?.trim().toString(), view)
            } else {
                Snackbar.make(view, "Please Enter Valid Mobile Number", Snackbar.LENGTH_LONG)
                    .setAction("", null).show()
            }
        }
        binding.txtRegister.setOnClickListener {
            startActivity(
                Intent(
                    this@LoginActivity,
                    com.shaikhomes.smartdiary.ui.UserRegister::class.java
                )
            )
            finish()
        }
        askPermission()
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(
            this,
            permission,
            MY_PERMISSIONS_REQUEST_CALL_PHONE
        )
    }

    private fun getUserData(mobileno: String, view: View) {
        loadDialog.showProgress()
        RetrofitInstance.api.getUserData(mobileno, "")
            .enqueue(object : Callback<UserRegister> {
                override fun onResponse(
                    call: Call<UserRegister>,
                    response: Response<UserRegister>
                ) {
                    loadDialog.dismissProgress()
                    if (response.body()?.status == "200") {
                        response.body()?.userDetailsList.let {
                            if (it?.size!! > 0) {
                                if (it.first().Active == "1") {
                                    PrefManager(this@LoginActivity).apply {
                                        this.userData = it.first()
                                        this.isLoggedIn = true
                                    }
                                    startActivity(
                                        Intent(
                                            this@LoginActivity,
                                            MainActivity::class.java
                                        )
                                    )
                                    finish()
                                } else {
                                    Snackbar.make(
                                        view,
                                        "User is not active",
                                        Snackbar.LENGTH_LONG
                                    )
                                        .setAction("", null).show()
                                }
                            } else {
                                Snackbar.make(
                                    view,
                                    "User Not Found. Please Register.",
                                    Snackbar.LENGTH_LONG
                                )
                                    .setAction("", null).show()
                            }
                        }
                    } else {
                        return
                    }
                }

                override fun onFailure(call: Call<UserRegister>, t: Throwable) {
                    loadDialog.dismissProgress()
                    Snackbar.make(view, t.message.toString(), Snackbar.LENGTH_LONG)
                        .setAction("", null).show()
                }
            })
    }
}