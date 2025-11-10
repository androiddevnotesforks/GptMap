package com.espressodev.gptmap.core.data.repository.impl

import android.util.Log
import com.espressodev.gptmap.core.data.repository.UserRepository
import com.espressodev.gptmap.core.data.util.runCatchingWithContext
import com.espressodev.gptmap.core.datastore.DataStoreService
import com.espressodev.gptmap.core.firebase.AccountService
import com.espressodev.gptmap.core.firebase.FirestoreRepository
import com.espressodev.gptmap.core.model.di.Dispatcher
import com.espressodev.gptmap.core.model.di.GmDispatchers.IO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    @param:Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    private val firestoreRepository: FirestoreRepository,
    private val accountService: AccountService,
    private val dataStoreService: DataStoreService
) : UserRepository {

    override suspend fun deleteUser(): Result<Unit> = runCatchingWithContext(ioDispatcher) {
        launch {
            dataStoreService.clear()
        }
        val user = firestoreRepository.deleteUser().getOrThrow()
        accountService.revokeAccess().getOrElse { throwable ->
            firestoreRepository.saveUser(user)
            throw throwable
        }
    }

    override suspend fun getUserFirstChar(): Result<Char> = runCatchingWithContext(ioDispatcher) {
        val fullName = dataStoreService.getUserFullName().first()
        if (fullName.isEmpty()) {
            val fetchedFullName = fetchAndSetFullName().getOrThrow()
            fetchedFullName.first()
        } else {
            'U'
        }
    }

    override suspend fun getLatestImageId(): Result<String> = runCatchingWithContext(ioDispatcher) {
        dataStoreService.getLatestImageIdForChat().first()
    }

    private suspend fun fetchAndSetFullName() = runCatching {
        val fullName = firestoreRepository.getUserFullName().getOrThrow()
        dataStoreService.setUserFullName(fullName)
        fullName
    }.onFailure { throwable ->
        Log.e(TAG, "Failed to fetch and set full name", throwable)
    }

    private companion object {
        private const val TAG = "UserRepositoryImpl"
    }
}
