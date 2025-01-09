package com.crosspaste.utils

import com.crosspaste.dto.secure.PairingRequest
import com.crosspaste.dto.secure.PairingResponse
import com.crosspaste.secure.SecureKeyPair
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDH
import dev.whyoleg.cryptography.algorithms.ECDSA
import dev.whyoleg.cryptography.algorithms.SHA256
import io.ktor.utils.io.core.*

object CryptographyUtils {

    private val provider = CryptographyProvider.Default
    private val sha256 = provider.get(SHA256)

    private val codecsUtils = getCodecsUtils()

    fun generateSecureKeyPair(): SecureKeyPair {
        val provider = CryptographyProvider.Default
        val ecdsa = provider.get(ECDSA)
        val ecdh = provider.get(ECDH)
        val signKeyPairGenerator = ecdsa.keyPairGenerator(EC.Curve.P256)
        val signKeyPair = signKeyPairGenerator.generateKeyBlocking()

        val cryptKeyPairGenerator = ecdh.keyPairGenerator(EC.Curve.P256)
        val cryptKeyPair = cryptKeyPairGenerator.generateKeyBlocking()
        return SecureKeyPair(signKeyPair, cryptKeyPair)
    }

    fun signData(
        privateKey: ECDSA.PrivateKey,
        createSignData: () -> ByteArray,
    ): ByteArray {
        return privateKey.signatureGenerator(sha256.id, ECDSA.SignatureFormat.DER)
            .generateSignatureBlocking(createSignData())
    }

    fun verifyData(
        publicKey: ECDSA.PublicKey,
        signature: ByteArray,
        createVerifyData: () -> ByteArray,
    ): Boolean {
        return publicKey.signatureVerifier(sha256.id, ECDSA.SignatureFormat.DER)
            .tryVerifySignatureBlocking(createVerifyData(), signature)
    }

    fun signPairingRequest(
        privateKey: ECDSA.PrivateKey,
        pairingRequest: PairingRequest,
    ): ByteArray {
        return signData(privateKey) {
            buildString {
                append(codecsUtils.base64Encode(pairingRequest.signPublicKey))
                append(codecsUtils.base64Encode(pairingRequest.cryptPublicKey))
                append(pairingRequest.token)
                append(pairingRequest.timestamp)
            }.toByteArray()
        }
    }

    fun verifyPairingRequest(
        publicKey: ECDSA.PublicKey,
        pairingRequest: PairingRequest,
        signature: ByteArray,
    ): Boolean {
        return verifyData(publicKey, signature) {
            buildString {
                append(codecsUtils.base64Encode(pairingRequest.signPublicKey))
                append(codecsUtils.base64Encode(pairingRequest.cryptPublicKey))
                append(pairingRequest.token)
                append(pairingRequest.timestamp)
            }.toByteArray()
        }
    }

    fun signPairingResponse(
        privateKey: ECDSA.PrivateKey,
        pairingResponse: PairingResponse,
    ): ByteArray {
        return signData(privateKey) {
            buildString {
                append(codecsUtils.base64Encode(pairingResponse.signPublicKey))
                append(codecsUtils.base64Encode(pairingResponse.cryptPublicKey))
                append(pairingResponse.timestamp)
            }.toByteArray()
        }
    }

    fun verifyPairingResponse(
        publicKey: ECDSA.PublicKey,
        pairingResponse: PairingResponse,
        signature: ByteArray,
    ): Boolean {
        return verifyData(publicKey, signature) {
            buildString {
                append(codecsUtils.base64Encode(pairingResponse.signPublicKey))
                append(codecsUtils.base64Encode(pairingResponse.cryptPublicKey))
                append(pairingResponse.timestamp)
            }.toByteArray()
        }
    }
}
