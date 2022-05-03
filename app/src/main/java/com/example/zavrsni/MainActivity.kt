package com.example.zavrsni

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor
    private final var REQUEST_CODE: Int = 12



    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        executor = ContextCompat.getMainExecutor(this)
        val biometricManager = BiometricManager.from(this)

        checkAuthentication(biometricManager)
        biometricPrompt = createBiometricPrompt()

        val biometricLoginButton = findViewById<Button>(R.id.biometric_login)

        biometricLoginButton.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BIOMETRIC_STRONG  or BIOMETRIC_WEAK)
            .setConfirmationRequired(false)
            .build()

    }

    private fun createBiometricPrompt(): BiometricPrompt{
        return BiometricPrompt(this, executor, object: BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                var authError = ""

                authError = if(errString == "Cancel"){
                    "Cancelled"
                } else{
                    "Greška: $errString"
                }

                Toast.makeText(applicationContext, authError, Toast.LENGTH_SHORT).show()
            }


            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(applicationContext, "Autentifikacija uspješna!", Toast.LENGTH_SHORT).show()
                val cryptoFragment = CryptoFragment.newInstance()
                openFragment(cryptoFragment)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Autentifikacija neuspjela!",
                    Toast.LENGTH_SHORT)
                    .show()
            }

        })
    }

    private fun checkAuthentication(biometricManager: BiometricManager){
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d("Message", "Aplikacija moze raditi biometrijsku provjeru.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e("Message", "Hardver ne podrzava biometrijsku provjeru.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e("Message", "Provjera biometrije nije dostupna.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                }
                startActivityForResult(enrollIntent, REQUEST_CODE)
            }
        }
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        findViewById<Button>(R.id.biometric_login).visibility = View.GONE
        transaction.addToBackStack(null)
        transaction.commit()
    }



}