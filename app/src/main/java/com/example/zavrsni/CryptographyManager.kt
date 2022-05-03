package com.example.zavrsni

import android.media.AudioMetadata
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.Charset
import java.security.Key
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class CryptographyManager {
    data class EncryptedData(val ciphertext: ByteArray, val initializationVector: ByteArray)

    fun generateSecretKey(keyName: String): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        keyStore.getKey(keyName, null)?.let { return it as SecretKey }

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(keyName, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .build()

        val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
    fun getCipher() : Cipher {
        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + '/' + KeyProperties.BLOCK_MODE_CBC + '/' + KeyProperties.ENCRYPTION_PADDING_PKCS7)
    }
    fun encryptData(plaintext: String, cipher: Cipher): EncryptedData {
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
        return EncryptedData(ciphertext,cipher.iv)
    }
    fun decryptData(cipherText: ByteArray, cipher: Cipher) : String {
        return String(cipher.doFinal(cipherText), Charset.forName("UTF-8"))
    }


}