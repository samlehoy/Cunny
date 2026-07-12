package com.eleonorez.cunny.data.sync

import android.content.Context
import android.util.Log
import androidx.lifecycle.asFlow
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class SyncManager(context: Context) {

    private val workManager = WorkManager.getInstance(context)

    companion object {
        private const val TAG = "SyncManager"
        private const val SYNC_WORK_PREFIX = "cunny_sync_"
        private const val SYNC_WORK_TAG_ALL = "cunny_sync_work_all"
    }

    /**
     * Enqueue a retry work request for a failed sync operation.
     * Uses KEEP policy: if the same sync type+key is already pending, don't duplicate.
     */
    fun enqueueSyncWork(syncType: String, data: Map<String, Any>) {
        val inputDataBuilder = Data.Builder()
            .putString(SyncWorker.KEY_SYNC_TYPE, syncType)

        // Map all data into WorkManager's Data
        data.forEach { (key, value) ->
            when (value) {
                is String -> inputDataBuilder.putString(key, value)
                is Int -> inputDataBuilder.putInt(key, value)
                is Long -> inputDataBuilder.putLong(key, value)
                is Boolean -> inputDataBuilder.putBoolean(key, value)
                is Array<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    inputDataBuilder.putStringArray(key, value as Array<String>)
                }
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    inputDataBuilder.putStringArray(key, (value as List<String>).toTypedArray())
                }
            }
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(inputDataBuilder.build())
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, // initial backoff 30 seconds
                TimeUnit.SECONDS
            )
            .addTag(SYNC_WORK_PREFIX + syncType)
            .addTag(SYNC_WORK_TAG_ALL)
            .build()

        // Generate unique work name from type + distinguishing key
        val uniqueKey = when (syncType) {
            SyncWorker.SYNC_TYPE_PROGRESS -> data[SyncWorker.KEY_LESSON_SLUG]?.toString() ?: "unknown"
            SyncWorker.SYNC_TYPE_BADGES -> data[SyncWorker.KEY_BADGES]?.toString() ?: "unknown"
            else -> syncType
        }
        val uniqueWorkName = "${SYNC_WORK_PREFIX}${syncType}_$uniqueKey"

        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.KEEP,
            workRequest
        )

        Log.d(TAG, "📤 Enqueued sync work: $uniqueWorkName")
    }

    /**
     * Observable count of pending/enqueued/running sync work items.
     * Used by SyncStatusIndicator to show ⚠️ badge.
     */
    fun getPendingSyncCount(): Flow<Int> {
        return workManager.getWorkInfosByTagLiveData(SYNC_WORK_TAG_ALL)
            .asFlow()
            .map { workList ->
                workList.count { workInfo ->
                    workInfo.state == WorkInfo.State.ENQUEUED ||
                    workInfo.state == WorkInfo.State.RUNNING ||
                    workInfo.state == WorkInfo.State.BLOCKED
                }
            }
    }
}
