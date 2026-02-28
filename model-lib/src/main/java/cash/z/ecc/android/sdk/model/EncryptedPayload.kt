package cash.z.ecc.android.sdk.model

data class EncryptedPayload(
    val ephemeralPublicKey: ByteArray,
    val encryptedData: ByteArray,
    val symmetricKey: ByteArray?
)