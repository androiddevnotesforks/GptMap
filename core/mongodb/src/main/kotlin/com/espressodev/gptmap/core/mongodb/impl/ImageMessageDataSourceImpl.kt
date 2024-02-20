package com.espressodev.gptmap.core.mongodb.impl

import com.espressodev.gptmap.core.model.ImageMessage
import com.espressodev.gptmap.core.model.realm.RealmImageAnalysis
import com.espressodev.gptmap.core.model.realm.RealmImageMessage
import com.espressodev.gptmap.core.model.realm.toImageAnalysis
import com.espressodev.gptmap.core.model.sortByDate
import com.espressodev.gptmap.core.mongodb.ImageMessageDataSource
import com.espressodev.gptmap.core.mongodb.RealmDataSourceBase
import com.espressodev.gptmap.core.mongodb.module.RealmManager.realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ImageMessageDataSourceImpl : ImageMessageDataSource, RealmDataSourceBase() {
    override suspend fun addImageMessageToImageAnalysis(
        imageAnalysisId: String,
        message: RealmImageMessage
    ): Result<Unit> = performRealmTransaction {
        val imageAnalysisToUpdate: RealmImageAnalysis = query<RealmImageAnalysis>(
            "userId == $0 AND imageId == $1",
            realmUserId,
            imageAnalysisId
        )
            .find()
            .first()
        findLatest(imageAnalysisToUpdate)?.let { realmImageAnalysis ->
            realmImageAnalysis.messages?.add(message)
        }
    }

    override suspend fun updateImageMessageInImageAnalysis(
        imageAnalysisId: String,
        messageId: String,
        text: String,
    ): Result<Unit> = performRealmTransaction {
        val imageAnalysis: RealmImageAnalysis = query<RealmImageAnalysis>(
            "userId == $0 AND imageId == $1",
            realmUserId,
            imageAnalysisId
        )
            .find()
            .first()

        findLatest(imageAnalysis)?.let { realmImageAnalysis ->
            realmImageAnalysis.messages?.find { it.id == messageId }?.response = text
        }
    }

    override fun getImageAnalysisMessages(imageAnalysisId: String): Flow<List<ImageMessage>> =
        realm.query<RealmImageAnalysis>(
            "userId == $0 AND imageId == $1",
            realmUserId,
            imageAnalysisId
        )
            .find()
            .asFlow()
            .map { results ->
                results.list.flatMap { realmImageAnalysis ->
                    realmImageAnalysis.toImageAnalysis().sortByDate().messages
                }
            }

    override fun getImageType(imageAnalysisId: String): String =
        realm.query<RealmImageAnalysis>(
            "userId == $0 AND imageId == $1",
            realmUserId,
            imageAnalysisId
        )
            .find()
            .first()
            .imageType
}