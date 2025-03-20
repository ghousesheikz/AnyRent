package com.shaikhomes.anyrent

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.shaikhomes.anyrent.ui.models.TenantData
import com.shaikhomes.anyrent.ui.models.TenantList
import com.shaikhomes.anyrent.ui.network.RetrofitInstance
import com.shaikhomes.anyrent.ui.utils.PrefManager
import com.shaikhomes.anyrent.ui.utils.calculateDaysBetween
import com.shaikhomes.anyrent.ui.utils.currentonlydate
import com.shaikhomes.anyrent.ui.utils.dateFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    private val prefmanager: PrefManager by lazy {
        PrefManager(context)
    }

    override fun doWork(): Result {
        // Notification Channel ID

        if (prefmanager.selectedApartment != null && prefmanager.selectedApartment?.ID != null) {
            getTenants(success = { tenantData ->
                val ID = prefmanager.selectedApartment?.ID.toString()
                if (tenantData.tenant_list.isNotEmpty()) {
                    tenantData.tenant_list.filter { it.apartmentId == ID }.let { tenantListData ->
                        if (tenantListData.isNotEmpty()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                bindPendingAmount(tenantListData, { pendingAmt ->
                                    val count = tenantListData.size.toString()
                                    sendNotification(
                                        "${count} Tenants are due today for ${prefmanager.selectedApartment?.apartmentname}",
                                        "${count} Tenants need to pay AED ${pendingAmt}/-",
                                        applicationContext
                                    )
                                })
                            }
                        }
                    }
                }
            }, error = {}, "", "", "", "due")
        }


        // Indicate success
        return Result.success()
    }

    fun sendNotification(title: String, body: String, applicationContext: Context) {
        val channelId = "daily_notification_channel"
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            // Add any extra data if necessary
            putExtra("some_data", "some_value")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Step 2: Create a PendingIntent
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Create a notification manager
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel (for Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_logo)
            .setAutoCancel(true)
            .build()

        // Trigger the notification
        notificationManager.notify(1001, notification)
    }

    fun getTenants(
        success: (TenantData) -> Unit,
        error: (String) -> Unit,
        mobileNo: String, apartmentid: String, active: String, duerecords: String
    ) {
        try {
            RetrofitInstance.api.getTenants(mobileNo, apartmentid, active, duerecords)
                .enqueue(object : Callback<TenantData> {
                    override fun onResponse(
                        call: Call<TenantData>,
                        response: Response<TenantData>
                    ) {
                        if (response.body()?.status == "200") {
                            success.invoke(response.body()!!)
                        } else {
                            error.invoke("Something Went Wrong")
                        }
                    }

                    override fun onFailure(call: Call<TenantData>, t: Throwable) {
                        error.invoke(t.message.toString())
                    }
                })
        } catch (exp: Exception) {
            exp.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun bindPendingAmount(tenantListData: List<TenantList>, pendingAmount: (Long) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            var pendingAmt: Long = 0
            tenantListData.forEach { tenantList ->
                val checkOut = tenantList.checkout?.dateFormat("dd-MM-yyyy 00:00:00", "dd-MM-yyyy")
                val currentDate = currentonlydate("dd-MM-yyyy")
                val rent =
                    if (tenantList.rent.isNullOrEmpty()) 0 else tenantList.rent?.toInt()
                checkOut?.let {
                    val days = calculateDaysBetween(currentDate, it)
                    val totalRent = rent!! * days
                    pendingAmt += kotlin.math.abs(totalRent)
                }
            }
            pendingAmount.invoke(pendingAmt)

        }
    }

}