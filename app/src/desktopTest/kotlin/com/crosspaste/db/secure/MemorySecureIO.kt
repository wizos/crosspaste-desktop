package com.crosspaste.db.secure

import io.ktor.util.collections.ConcurrentMap

class MemorySecureIO : SecureIO {

    private val cryptPublicKeyMap = ConcurrentMap<String, ByteArray>()

    override fun saveCryptPublicKey(
        appInstanceId: String,
        serialized: ByteArray,
    ): Boolean {
        return cryptPublicKeyMap.put(appInstanceId, serialized) == null
    }

    override fun existCryptPublicKey(appInstanceId: String): Boolean {
        return cryptPublicKeyMap.containsKey(appInstanceId)
    }

    override fun deleteCryptPublicKey(appInstanceId: String) {
        cryptPublicKeyMap.remove(appInstanceId)
    }

    override fun serializedPublicKey(appInstanceId: String): ByteArray? {
        return cryptPublicKeyMap[appInstanceId]
    }
}