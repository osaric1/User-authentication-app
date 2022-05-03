package com.example.zavrsni

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.nio.charset.Charset
import java.security.spec.AlgorithmParameterSpec
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec

class CryptoFragment: Fragment() {
    private lateinit var cryptographyManager: CryptographyManager
    private final var REQUEST_CODE: Int = 12
    private lateinit var button: Button
    private lateinit var plainText: EditText
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var keyName: String = "biometric_key"
    private lateinit var ciphertext:ByteArray
    private lateinit var iv: ByteArray

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceBundle: Bundle?): View?
    {
        var view = inflater.inflate(R.layout.crypto_fragment,container,false)
        cryptographyManager = CryptographyManager()
        biometricPrompt = createBiometricPrompt()
        plainText = view.findViewById(R.id.tekst)

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setConfirmationRequired(false)
            .build()

        button =  view.findViewById<Button>(R.id.button)
        button.setOnClickListener {
            if(button.text == "Encrypt"){
                val cipher = cryptographyManager.getCipher()
                val secretKey = cryptographyManager.generateSecretKey(keyName)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
            else{
                val cipher = cryptographyManager.getCipher()
                val secretKey = cryptographyManager.generateSecretKey(keyName)
                cipher.init(Cipher.DECRYPT_MODE,secretKey,IvParameterSpec(iv))
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }
        return view
    }

    private fun createBiometricPrompt(): BiometricPrompt{
        val executor = ContextCompat.getMainExecutor(requireContext())

        return BiometricPrompt(this, executor, object: BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                var authError = ""

                authError = if(errString == "Cancel"){
                    "Cancelled"
                } else{
                    "Greška: $errString"
                }

                Toast.makeText(context, authError, Toast.LENGTH_SHORT).show()
            }


            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                val data = if(button.text == "Encrypt"){
                    val encryptedText = cryptographyManager.encryptData(plainText.text.toString(),result.cryptoObject?.cipher!!)
                    ciphertext = encryptedText.ciphertext
                    iv = encryptedText.initializationVector
                    button.text = "Decrypt"

                    String(ciphertext, Charset.forName("UTF-8"))

                }
                else{
                    button.text = "Encrypt"
                    cryptographyManager.decryptData(ciphertext, result.cryptoObject?.cipher!!)

                }
                plainText.setText(data)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(context, "Autentifikacija neuspjela!",
                    Toast.LENGTH_SHORT)
                    .show()
            }

        })
    }

    companion object {
        fun newInstance(): CryptoFragment = CryptoFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}