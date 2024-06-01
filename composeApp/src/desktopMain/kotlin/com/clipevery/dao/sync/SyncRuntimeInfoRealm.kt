package com.clipevery.dao.sync

import com.clipevery.dto.sync.SyncInfo
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmInstant

class SyncRuntimeInfoRealm(private val realm: Realm) : SyncRuntimeInfoDao {

    override fun getAllSyncRuntimeInfos(): RealmResults<SyncRuntimeInfo> {
        return realm.query(SyncRuntimeInfo::class).sort("createTime", Sort.DESCENDING).find()
    }

    override fun getSyncRuntimeInfo(appInstanceId: String): SyncRuntimeInfo? {
        return realm.query(SyncRuntimeInfo::class, "appInstanceId == $0", appInstanceId).first().find()
    }

    override fun update(
        syncRuntimeInfo: SyncRuntimeInfo,
        block: SyncRuntimeInfo.() -> Unit,
    ): SyncRuntimeInfo? {
        return realm.writeBlocking {
            findLatest(syncRuntimeInfo)?.let {
                return@writeBlocking it.apply(block)
            } ?: run {
                query(SyncRuntimeInfo::class, "appInstanceId == $0", syncRuntimeInfo.appInstanceId).first().find()?.let {
                    return@writeBlocking it.apply(block)
                }
            }
        }
    }

    override suspend fun suspendUpdate(
        syncRuntimeInfo: SyncRuntimeInfo,
        block: SyncRuntimeInfo.() -> Unit,
    ): SyncRuntimeInfo? {
        return realm.write {
            findLatest(syncRuntimeInfo)?.let {
                return@write it.apply(block)
            } ?: run {
                query(SyncRuntimeInfo::class, "appInstanceId == $0", syncRuntimeInfo.appInstanceId).first().find()?.let {
                    return@write it.apply(block)
                }
            }
        }
    }

    private fun updateSyncRuntimeInfo(
        syncRuntimeInfo: SyncRuntimeInfo,
        syncInfo: SyncInfo,
    ): Boolean {
        var hasModify = false
        if (syncRuntimeInfo.appVersion != syncInfo.appInfo.appVersion) {
            syncRuntimeInfo.appVersion = syncInfo.appInfo.appVersion
            hasModify = true
        }

        if (syncRuntimeInfo.userName != syncInfo.appInfo.userName) {
            syncRuntimeInfo.userName = syncInfo.appInfo.userName
            hasModify = true
        }

        if (syncRuntimeInfo.deviceId != syncInfo.endpointInfo.deviceId) {
            syncRuntimeInfo.deviceId = syncInfo.endpointInfo.deviceId
            hasModify = true
        }

        if (syncRuntimeInfo.deviceName != syncInfo.endpointInfo.deviceName) {
            syncRuntimeInfo.deviceName = syncInfo.endpointInfo.deviceName
            hasModify = true
        }

        if (syncRuntimeInfo.platformName != syncInfo.endpointInfo.platform.name) {
            syncRuntimeInfo.platformName = syncInfo.endpointInfo.platform.name
            hasModify = true
        }

        if (syncRuntimeInfo.platformVersion != syncInfo.endpointInfo.platform.version) {
            syncRuntimeInfo.platformVersion = syncInfo.endpointInfo.platform.version
            hasModify = true
        }

        if (syncRuntimeInfo.platformArch != syncInfo.endpointInfo.platform.arch) {
            syncRuntimeInfo.platformArch = syncInfo.endpointInfo.platform.arch
            hasModify = true
        }

        if (syncRuntimeInfo.platformBitMode != syncInfo.endpointInfo.platform.bitMode) {
            syncRuntimeInfo.platformBitMode = syncInfo.endpointInfo.platform.bitMode
            hasModify = true
        }

        if (hostListEqual(syncRuntimeInfo.hostList, syncInfo.endpointInfo.hostList)) {
            syncRuntimeInfo.hostList = syncInfo.endpointInfo.hostList.toRealmList()
            hasModify = true
        }

        if (syncRuntimeInfo.port != syncInfo.endpointInfo.port) {
            syncRuntimeInfo.port = syncInfo.endpointInfo.port
            hasModify = true
        }

        // When the state is not connected,
        // we will update the modifyTime at least to drive the refresh
        if (hasModify || syncRuntimeInfo.connectState != SyncState.CONNECTED) {
            syncRuntimeInfo.modifyTime = RealmInstant.now()
        }
        return hasModify
    }

    override fun insertOrUpdate(syncInfo: SyncInfo): Boolean {
        try {
            return realm.writeBlocking {
                query(SyncRuntimeInfo::class, "appInstanceId == $0", syncInfo.appInfo.appInstanceId)
                    .first()
                    .find()?.let {
                        return@let updateSyncRuntimeInfo(it, syncInfo)
                    } ?: run {
                    copyToRealm(createSyncRuntimeInfo(syncInfo))
                    return@run true
                }
            }
        } catch (e: Exception) {
            return false
        }
    }

    override fun deleteSyncRuntimeInfo(appInstanceId: String) {
        realm.writeBlocking {
            query(SyncRuntimeInfo::class, "appInstanceId == $0", appInstanceId)
                .first()
                .find()?.let {
                    delete(it)
                }
        }
    }
}
