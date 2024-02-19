package com.espressodev.gptmap.core.mongodb.impl

import com.espressodev.gptmap.core.model.Exceptions.RealmUserNotLoggedInException
import com.espressodev.gptmap.core.mongodb.RealmAccountService
import com.espressodev.gptmap.core.mongodb.module.RealmManager
import com.espressodev.gptmap.core.mongodb.module.RealmManager.app
import io.realm.kotlin.mongodb.Credentials

class RealmAccountServiceImpl : RealmAccountService {
    override suspend fun loginWithEmail(token: String): Result<Unit> = runCatching {
        val user = app.login(Credentials.jwt(token))
            .also { if (!it.loggedIn) throw RealmUserNotLoggedInException() }
        RealmManager.initRealm(user)
    }
    override suspend fun logOut() {
        app.currentUser?.logOut()
    }
    override suspend fun revokeAccess(): Result<Unit> = runCatching {
        app.currentUser?.delete()
    }
}
