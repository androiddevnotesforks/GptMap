package com.espressodev.gptmap.core.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.espressodev.gptmap.core.mongodb.RealmAccountService
import com.espressodev.gptmap.core.mongodb.UserManagementDataSource

@HiltWorker
class DeleteUserFromRealmWorker(
    private val userManagementDataSource: UserManagementDataSource,
    private val realmAccountService: RealmAccountService,
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result = try {
        userManagementDataSource.deleteUser().getOrThrow()
        realmAccountService.revokeAccess()
        Result.success()
    } catch (e: Exception) {
        Log.e("DeleteUserFromRealmWorker", "doWork: failure $e")
        Result.failure()
    }

    class Factory(
        private val userManagementDataSource: UserManagementDataSource,
        private val realmAccountService: RealmAccountService
    ) : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ) = if (workerClassName == DeleteUserFromRealmWorker::class.java.name) {
            DeleteUserFromRealmWorker(
                userManagementDataSource,
                realmAccountService,
                appContext,
                workerParameters
            )
        } else {
            null
        }
    }
}
