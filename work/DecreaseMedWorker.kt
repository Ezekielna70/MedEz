package com.protel.myapplication.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.protel.myapplication.api.ApiClient
import com.protel.myapplication.api.DecreaseMedRequest
import com.protel.myapplication.api.DecreaseMedResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DecreaseMedWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val devId = inputData.getString("dev_id") ?: return Result.failure()
        val medId = inputData.getString("med_id") ?: return Result.failure()
        val medRemaining = inputData.getInt("med_remaining", -1)
        if (medRemaining == -1) return Result.failure()

        val request = DecreaseMedRequest(dev_id = devId, med_id = medId, med_remaining = medRemaining)

        // Memanggil API secara sinkron: Disarankan menggunakan callback synchronous atau implementasi blocking
        // Namun WorkManager di latar belakang dapat menggunakan callback blocking dengan Retrofit

        var result: Result

        val response = ApiClient.apiService.decreaseMed(request).execute()
        if (response.isSuccessful && response.body()?.status == "success") {
            result = Result.success()
        } else {
            result = Result.failure()
        }

        return result
    }
}
