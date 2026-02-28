package cash.z.ecc.android.sdk.tool

import cash.z.ecc.android.sdk.internal.Derivation
import cash.z.ecc.android.sdk.internal.SuspendingLazy
import cash.z.ecc.android.sdk.internal.TypesafeDerivationToolImpl
import cash.z.ecc.android.sdk.internal.jni.RustDerivationTool
import cash.z.ecc.android.sdk.model.Account
import cash.z.ecc.android.sdk.model.EphemeralPublicKey
import cash.z.ecc.android.sdk.model.UnifiedFullViewingKey
import cash.z.ecc.android.sdk.model.UnifiedSpendingKey
import cash.z.ecc.android.sdk.model.ShieldedSpendingKey
import cash.z.ecc.android.sdk.model.SharedSecret
import cash.z.ecc.android.sdk.model.ZcashNetwork
import cash.z.ecc.android.sdk.model.ChannelKeys
import cash.z.ecc.android.sdk.model.DecryptParams
import cash.z.ecc.android.sdk.model.EncryptedPayload
interface DerivationTool {
    /**
     * Given a seed and a number of accounts, return the associated Unified Full Viewing Keys.
     *
     * @param seed the seed from which to derive viewing keys.
     * @param numberOfAccounts the number of accounts to use. Multiple accounts are not fully
     * supported so the default value of 1 is recommended.
     *
     * @return the UFVKs derived from the seed, encoded as Strings.
     */
    suspend fun deriveUnifiedFullViewingKeys(
        seed: ByteArray,
        network: ZcashNetwork,
        numberOfAccounts: Int
    ): List<UnifiedFullViewingKey>


/*    suspend fun deriveViewingKey(
        seed: ByteArray,
        network: ZcashNetwork,
        numberOfAccounts: Int
    ): ByteArray
*/
    /**
     * Given a unified spending key, return the associated unified full viewing key.
     *
     * @param usk the key from which to derive the viewing key.
     *
     * @return a unified full viewing key.
     */
    suspend fun deriveUnifiedFullViewingKey(
        usk: UnifiedSpendingKey,
        network: ZcashNetwork
    ): UnifiedFullViewingKey

    /**
     * Derives and returns a unified spending key from the given seed for the given account ID.
     *
     * Returns the newly created [ZIP 316] account identifier, along with the binary encoding
     * of the [`UnifiedSpendingKey`] for the newly created account. The caller should store
     * the returned spending key in a secure fashion.
     *
     * @param seed the seed from which to derive spending keys.
     * @param account the account to derive.
     *
     * @return the unified spending key for the account.
     */
    suspend fun deriveUnifiedSpendingKey(
        transparentKey: ByteArray,
        extendedSecretKey: ByteArray,
        seed: ByteArray,
        network: ZcashNetwork,
        account: Account
    ): UnifiedSpendingKey


    suspend fun deriveSaplingSpendingKey(
        seed: ByteArray,
        network: ZcashNetwork,
        account: Account
    ): ShieldedSpendingKey


    /**
     * Given a seed and account index, return the associated Unified Address.
     *
     * @param seed the seed from which to derive the address.
     * @param account the index of the account to use for deriving the address.
     *
     * @return the address that corresponds to the seed and account index.
     */
    suspend fun deriveUnifiedAddress(
        seed: ByteArray,
        network: ZcashNetwork,
        account: Account
    ): String

    /**
     * Given a seed and account index, return the associated Shielded Address.
     *
     * @param seed the seed from which to derive the address.
     * @param account the index of the account to use for deriving the address.
     *
     * @return the address that corresponds to the seed and account index.
     */
    suspend fun deriveShieldedAddress(
        seed: ByteArray,
        network: ZcashNetwork,
        account: Account
    ): String

    /**
     * Given a Unified Full Viewing Key string, return the associated Unified Address.
     *
     * @param viewingKey the viewing key to use for deriving the address. The viewing key is tied to
     * a specific account so no account index is required.
     *
     * @return the address that corresponds to the viewing key.
     */
    suspend fun deriveUnifiedAddress(
        viewingKey: String,
        network: ZcashNetwork
    ): String

    /**
     * Given a Unified Full Viewing Key string, return the associated Unified Address.
     *
     * @param viewingKey the viewing key to use for deriving the address. The viewing key is tied to
     * a specific account so no account index is required.
     *
     * @return the address that corresponds to the viewing key.
     */
    suspend fun deriveShieldedAddress(
        viewingKey: String,
        network: ZcashNetwork
    ): String

    suspend fun isValidShieldedAddress(
        address: String,
        network: ZcashNetwork
    ): Boolean

    suspend fun getSymmetricKey(
        viewingKey: String,
        ephemeralPublicKey: ByteArray,
        network: ZcashNetwork
    ): String

    suspend fun generateSymmetricKey(
        saplingAddress: String,
        network: ZcashNetwork
    ): String

    suspend fun getEncryptionAddress(
        seed: ByteArray,
        fromId: ByteArray,
        toId: ByteArray,
        accountIndex: Int,
        network: ZcashNetwork
    ): String

    /**
     * Derives a deterministic z-address for encrypted messaging between two parties. This is a Verus-specific feature.
     *
     * @param seed The user's wallet seed. Can be null if spendingKey is provided.
     * @param spendingKey The user's extended spending key. Can be null if seed is provided.
     * @param fromId A unique identifier for the sender (e.g., a hex-encoded VerusID). Can be null.
     * @param toId A unique identifier for the recipient (e.g., a hex-encoded VerusID). Can be null.
     * @param hdIndex The HD account index to use if deriving from a seed. Defaults to 0.
     * @param encryptionIndex The index for the final encryption key derivation. Defaults to 0.
     * @param returnSecret If true, the derived extended spending key will be included in the result. Defaults to false.
     * @return A [ChannelKeys] object containing the derived address, viewing key, and optional spending key.
     */
    suspend fun getVerusEncryptionAddress(
        seed: ByteArray?,
        spendingKey: ByteArray?,
        fromId: ByteArray?,
        toId: ByteArray?,
        hdIndex: Int = 0,
        encryptionIndex: Int = 0,
        returnSecret: Boolean = false
    ): ChannelKeys

    /**
     * Encrypts data for a given z-address using Verus-specific encryption.
     *
     * @param address The recipient's z-address.
     * @param data The data to encrypt as a ByteArray.
     * @param returnSsk If true, the symmetric key used for encryption will be returned in the payload. Defaults to false.
     * @return An [EncryptedPayload] containing the ciphertext and public key material.
     */
    suspend fun encryptVerusData(
        address: String,
        data: ByteArray,
        returnSsk: Boolean = false
    ): EncryptedPayload

    /**
     * Decrypts Verus-specific encrypted data.
     *
     * @param ivkBytes The recipient's incoming viewing key bytes. Not needed if sskHex is provided.
     * @param epkHex The sender's hex-encoded ephemeral public key. Not needed if sskHex is provided.
     * @param ciphertextHex The hex-encoded encrypted data.
     * @param sskHex The hex-encoded symmetric session key. If provided, ivkBytes and epkHex are ignored.
     * @return The decrypted plaintext data as a String.
     */
    suspend fun decryptVerusData(
        ivkBytes: ByteArray?,
        epkHex: String?,
        ciphertextHex: String,
        sskHex: String?
    ): String

    /**
     * Encrypts data for a given z-address.
     *
     * @param address The recipient's z-address.
     * @param data The data to encrypt as a ByteArray.
     * @param returnSsk If true, the symmetric key will be returned.
     * @return An [EncryptedPayload] containing the encrypted data.
     */
    suspend fun encryptData(
        address: String,
        data: ByteArray,
        returnSsk: Boolean = false
    ): EncryptedPayload

    /**
     * Decrypts data using the provided parameters.
     *
     * @param params The [DecryptParams] containing all decryption parameters.
     * @return The decrypted plaintext data as a String.
     */
    suspend fun decryptData(
        params: DecryptParams
    ): String

    companion object {
        const val DEFAULT_NUMBER_OF_ACCOUNTS = Derivation.DEFAULT_NUMBER_OF_ACCOUNTS

        private val instance =
            SuspendingLazy<Unit, DerivationTool> {
                TypesafeDerivationToolImpl(RustDerivationTool.new())
            }

        suspend fun getInstance() = instance.getInstance(Unit)
    }
}
